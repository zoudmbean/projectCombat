package com.bjc.gulimall.ware.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.bjc.common.to.mq.StockDetailTo;
import com.bjc.common.to.mq.StockLockTo;
import com.bjc.common.utils.R;
import com.bjc.gulimall.ware.dao.WareSkuDao;
import com.bjc.common.exception.NoStockException;
import com.bjc.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.bjc.gulimall.ware.entity.WareOrderTaskEntity;
import com.bjc.gulimall.ware.feign.MemberFeignService;
import com.bjc.gulimall.ware.service.WareOrderTaskDetailService;
import com.bjc.gulimall.ware.service.WareOrderTaskService;
import com.bjc.gulimall.ware.vo.OrderItemVo;
import com.bjc.gulimall.ware.vo.WareSkuLockVo;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bjc.common.utils.PageUtils;
import com.bjc.common.utils.Query;

import com.bjc.gulimall.ware.dao.WareInfoDao;
import com.bjc.gulimall.ware.entity.WareInfoEntity;
import com.bjc.gulimall.ware.service.WareInfoService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private WareSkuDao wareSkuDao;

    @Autowired
    private WareOrderTaskDetailService orderTaskDetailService;

    @Autowired
    private WareOrderTaskService orderTaskService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        String key = (String)params.get("key");
        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(key)){
            wrapper.and(q -> {
                q.eq("id",key).or().like("name",key).or().like("address",key).or().like("areacode",key);
            });
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    /**
     * 根据收货地址计算运费
     *
     * @return*/
    @Override
    public JSONObject getFace(Long id) {
        R info = memberFeignService.info(id);
        if(info.getCode() == 0){
            JSONObject json = info.getData("memberReceiveAddress",new TypeReference<JSONObject>(){});
            // String province = json.getString("province");
            // 简单点，使用手机号码的最后一位来做为运费
            String phoneNum = Optional.ofNullable(json.getString("phone")).orElse("8");
            String phone = phoneNum.charAt(phoneNum.length()-1) + "";
            // 设置运费
            json.put("fare",phone);
            return json;
        }
        return new JSONObject();
    }

    /*
    * 库存解锁的场景：
    *   1）下单成功，但是订单过期未支付，被系统自动（用户主动）取消了
    *   2）下单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚，之前锁定的库存需要解锁（使用seata分布式事务太慢了，我们希望可以自动解锁）
    *
    * */
    // 为某个订单锁定库存
    @Override
    @Transactional(rollbackFor = NoStockException.class)        // 指定回滚条件，只要抛出NoStockException异常就回滚
    public Boolean orderlock(WareSkuLockVo vo) {

        /*
        * 保存库存工作单详情
        * 用于追溯
        * */
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        orderTaskService.save(taskEntity);

         // 1 按照订单的收货地址，找到就近仓库，锁定库存

        // 2. 找到每个商品在哪个仓库有库存
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            // 查询这个商品在哪里有库存
            List<Long> wareIds = wareSkuDao.listWareIdHasSkuTock(skuId);
            stock.setWareId(wareIds);
            return stock;
        }).collect(Collectors.toList());

        // 3. 锁定库存
        for (SkuWareHasStock stock : collect) {
            Boolean skuStocked = false;
            Long skuId = stock.getSkuId();
            List<Long> wareIds = stock.getWareId();
            if(CollectionUtils.isEmpty(wareIds)){
                // 如果商品没有库存，直接抛异常
                throw new NoStockException(skuId);
            }
            // 1、如果每一个商品都锁定成功，将当前商品锁定了几件的工作单记录发给MQ
            // 2、锁定失败，前面保存的工作单信息就回滚了，这样发送出去的消息，即使要解锁记录，由于数据库查询不到id，所以，就不需要解锁了，但是，这样设计是不合理的
            for (Long wareId : wareIds) {
                // 不成功返回0
                Long count = wareSkuDao.lockSkuStock(skuId,wareId,stock.getNum());
                if(count > 0){
                    // 当前商品锁定成功
                    skuStocked = true;
                    // TODO 告诉MQ库存锁定成功
                    WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity(null,skuId,"",stock.getNum(),taskEntity.getId(),wareId,1);
                    orderTaskDetailService.save(entity);
                    // 给rabbitMq发送消息
                    StockLockTo stockLockTo = new StockLockTo();
                    stockLockTo.setId(taskEntity.getId());
                    StockDetailTo stockDetail = new StockDetailTo();
                    // 属性对拷
                    BeanUtils.copyProperties(entity,stockDetail);
                    // 只发Id 不行，防止回滚以后找不到数据
                    stockLockTo.setDetail(stockDetail);
                    rabbitTemplate.convertAndSend("stock-event-exchange","stock.locked",stockLockTo);
                    break;
                } else {
                    // 当前仓库锁定失败，重试下一个仓库
                    continue;
                }
            }
            if(!skuStocked){
                // 当前商品没锁住
                throw new NoStockException(skuId);
            }
        }
        return true;
    }

    @Data
    class SkuWareHasStock {
         private Long skuId;
         private Integer num;
         private List<Long> wareId;
    }

}
