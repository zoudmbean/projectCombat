package com.bjc.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.bjc.common.to.mq.OrderTo;
import com.bjc.common.to.mq.StockDetailTo;
import com.bjc.common.to.mq.StockLockTo;
import com.bjc.common.utils.R;
import com.bjc.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.bjc.gulimall.ware.entity.WareOrderTaskEntity;
import com.bjc.gulimall.ware.feign.OrderFeignService;
import com.bjc.gulimall.ware.feign.ProductFeignService;
import com.bjc.gulimall.ware.service.WareOrderTaskDetailService;
import com.bjc.gulimall.ware.service.WareOrderTaskService;
import com.bjc.gulimall.ware.vo.OrderVo;
import com.bjc.gulimall.ware.vo.SkuHasStockVo;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntBinaryOperator;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bjc.common.utils.PageUtils;
import com.bjc.common.utils.Query;

import com.bjc.gulimall.ware.dao.WareSkuDao;
import com.bjc.gulimall.ware.entity.WareSkuEntity;
import com.bjc.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("wareSkuService")
// @RabbitListener(queues = {"stock.release.stock.queue"}) // stock.release.stock.queue
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private WareOrderTaskDetailService orderTaskDetailService;

    @Autowired
    private WareOrderTaskService orderTaskService;

    @Autowired
    private OrderFeignService orderFeignService;

    @Autowired
    private WareSkuDao wareSkuDao;

    @Override
    public void unLockStock(StockLockTo stockLockTo){
        System.out.println("收到解锁库存的信息：");
        Long id = stockLockTo.getId();  // 库存工作单Id
        StockDetailTo detail = stockLockTo.getDetail();
        Long skuId = detail.getSkuId();
        Long detailId = detail.getId();
        // 解锁
        // 1. 查询数据库关于这个订单的锁定库存信息
        //      有：证明库存锁定成功了
        //            解锁：要根据订单情况来看
        //                    1）没有这个订单，必须解锁
        //                    2）有这个订单，根据订单状态来判断需不需要解锁
        //      无：库存锁定失败，库存回滚，无需解锁
        WareOrderTaskDetailEntity detailEntity = orderTaskDetailService.getById(detailId);
        if(detailEntity != null){
            // 解锁
            WareOrderTaskEntity taskEntity = orderTaskService.getById(detailId);
            String orderSn = taskEntity.getOrderSn();
            // 根据订单号查询订单状态
            R r = orderFeignService.getOrderStatusByOrderSn(orderSn);
            if(r.getCode() == 0){
                // 订单服务返回成功
                OrderVo orderVo = r.getData(new TypeReference<OrderVo>() {});
                if(null == orderVo || orderVo.getStatus() == 4){   // 订单不存在 或者 订单转为为取消
                    // 解锁库存
                    if(detailEntity.getLockStatus() == 1){
                        unLockStock(detail.getSkuId(),detail.getWareId(),detail.getSkuNum(),detailId);
                    }
                }
            } else {
                // 远程服务失败
                throw new RuntimeException("远程服务失败！");
            }
        }
    }

    // 防止订单服务卡顿，导致订单状态消息一直改不了，库存消息优先到期，查询订单状态为新建状态，什么都不做就走了
    // 导致当前卡顿的订单会永远无法得到解锁库存
    @Override
    @Transactional
    public void unLockStock(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        // 可查可不查
        // R r = orderFeignService.getOrderStatusByOrderSn(orderSn);

        // 查询库存解锁状态，防止重复解锁
        WareOrderTaskEntity taskEntity = orderTaskService.getOrderTaskByOrderSn(orderSn);
        // 获取库存工作单Id
        Long id = taskEntity.getId();
        // 根据库存工作单id，找到所有未解锁的商品
        List<WareOrderTaskDetailEntity> list = orderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", id).eq("lock_status", 1));
        // 解锁
        list.forEach(item -> {
            unLockStock(item.getSkuId(),item.getWareId(),item.getSkuNum(),item.getId());
        });
    }

    private void unLockStock(Long skuId,Long wareId,Integer num,Long taskDetailId){
        // 1. 库存解锁
        wareSkuDao.unLockStock(skuId,wareId,num);

        // 2. 修改库存工作单的状态为已解锁
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(taskDetailId);
        entity.setLockStatus(2);// 已解锁
        orderTaskDetailService.updateById(entity);
    }


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageCondition(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();

        String skuId = (String)params.get("skuId");
        if(StringUtils.isNotEmpty(skuId)){
            wrapper.eq("sku_id",skuId);
        }
        String wareId = (String)params.get("wareId");
        if(StringUtils.isNotEmpty(wareId)){
            wrapper.eq("ware_Id",wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Autowired
    private ProductFeignService productFeignService;

    @Transactional
    @Override
    public void addStore(Long skuId, Long wareId, Integer skuNum) {
        WareSkuEntity entity = new WareSkuEntity();
        entity.setSkuId(skuId).setWareId(wareId).setStock(skuNum);
        QueryWrapper<WareSkuEntity> wraper = new QueryWrapper();
        wraper.eq("sku_id",skuId).eq("ware_id",wareId);
        List<WareSkuEntity> wareSkuEntities = this.baseMapper.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if(!CollectionUtils.isEmpty(wareSkuEntities)){
            IntBinaryOperator i;
            int stock = wareSkuEntities.stream()
                    .mapToInt(ware -> Optional.ofNullable(ware.getStock()).orElse(0))
                    .reduce(0,(l,r) -> l + r);
            entity.setStock(stock + entity.getStock());
            entity.setStockLocked(1);
            try{
                // TODO 远程查询sku的名称,如果失败，整个事务无需回滚
                // 1. 通过try-catch不处理方式
                // 2. 还有一种办法可以让异常出现不回滚的方法
                R info = productFeignService.info(skuId);
                if(info.getCode() == 0 ){
                    Map<String,Object> data = (Map<String,Object>) info.get("skuInfo");
                    entity.setSkuName((String) data.get("skuName"));
                }
            }catch (Exception e){}
        }
        this.saveOrUpdate(entity,wraper);
    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();
            // 查询当前sku的总库存量
            long count = Optional.ofNullable(this.baseMapper.getSkusHasStock(skuId)).orElse(0L);
            vo.setSkuId(skuId);
            vo.setHasStock(count > 0);
            return vo;
        }).collect(Collectors.toList());
        return collect;
    }

}
