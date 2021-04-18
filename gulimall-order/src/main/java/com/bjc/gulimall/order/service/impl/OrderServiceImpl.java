package com.bjc.gulimall.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.bjc.common.enums.OrderStatusEnum;
import com.bjc.common.exception.NoStockException;
import com.bjc.common.to.mq.OrderTo;
import com.bjc.common.to.mq.SeckillOrderTo;
import com.bjc.common.utils.R;
import com.bjc.common.vo.MemberResVo;
import com.bjc.gulimall.order.constant.OrderConstant;
import com.bjc.gulimall.order.dao.OrderItemDao;
import com.bjc.gulimall.order.entity.OrderItemEntity;
import com.bjc.gulimall.order.feign.CartFeignService;
import com.bjc.gulimall.order.feign.MemberFeignService;
import com.bjc.gulimall.order.feign.ProductFeignService;
import com.bjc.gulimall.order.feign.WmsFeignService;
import com.bjc.gulimall.order.interceptor.LoginUserInterceptor;
import com.bjc.gulimall.order.service.OrderItemService;
import com.bjc.gulimall.order.to.OrderCreateTo;
import com.bjc.gulimall.order.vo.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bjc.common.utils.PageUtils;
import com.bjc.common.utils.Query;

import com.bjc.gulimall.order.dao.OrderDao;
import com.bjc.gulimall.order.entity.OrderEntity;
import com.bjc.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Autowired
    private WmsFeignService wmsFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /*
    * 订单确认页需要的数据
    * */
    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberResVo memberResVo = LoginUserInterceptor.loginUser.get();

        // 获取请求对象 让每一个线程都共享该请求
        RequestAttributes oldRequestAtts = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            // 共享请求对象
            RequestContextHolder.setRequestAttributes(oldRequestAtts);
            // 1. 远程查询地址列表
            List<MemberAddressVo> address = memberFeignService.getAddress(memberResVo.getId());
            confirmVo.setAddress(address);
        }, executor);


        CompletableFuture<Void> cartItemsFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(oldRequestAtts);
            // 2. 购物车所有选中的购物项
            List<OrderItemVo> cartItems = cartFeignService.getCurrentCartItems();
            confirmVo.setItems(cartItems);
        },executor).thenRunAsync(() -> {
            List<OrderItemVo> items = confirmVo.getItems();
            // 批量查询每个商品的库存详情
            List<Long> skuIds = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            // 调用商品远程服务方法去查询数据
            R r = wmsFeignService.getSkusHasStock(skuIds);
            if(r.getCode() == 0){
                // 这里不想新建vo了，直接用JSONObject来接收也可以
                List<JSONObject> datas = r.getData("data", new TypeReference<List<JSONObject>>() {});
                // 方式一：双重循环查询
                /*datas.forEach(json -> {
                    items.stream().forEach(orderItem -> {
                        if(orderItem.getSkuId().longValue() == json.getLong("skuId")){
                            orderItem.setHasStock(json.getBoolean("hasStock"));
                        }
                    });
                });*/
                if(null != datas){
                    // 方式二：利用map
                    Map<Long, String> mapCollect = datas.stream().collect(Collectors.toMap(k -> k.getLong("skuId"), v -> v.getBoolean("hasStock")?"有货":"无货"));
                    confirmVo.setHasStockMap(mapCollect);
                }
            }

        },executor);

        // 3. 查询用户积分
        Integer integration = memberResVo.getIntegration();
        confirmVo.setIntegration(integration);


        // 4. 其他数据自动计算

        // TODO 5. 防止重令牌
        // 5.1 生成一个随机令牌
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        // 5.2 页面令牌
        confirmVo.setOrderToken(token);
        // 5.3 服务器令牌
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX+memberResVo.getId(),token,30,TimeUnit.MINUTES);

        // 阻塞
        CompletableFuture.allOf(addressFuture,cartItemsFuture).get();
        return confirmVo;
    }

    /* 下单 */
    // @GlobalTransactional    // 其实这里不适合使用AT模式，下单属于典型的高并发场景，不适合AT模式，AT模式适用于一些简单的分布式事务  所以注释掉，利用MQ使用最终一致性方案
@Transactional
@Override
public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
    // 保存到当前线程
    confirmVoThreadLocal.set(vo);

    SubmitOrderResponseVo resVo = new SubmitOrderResponseVo();
    resVo.setCode(0); // 默认设置0 表示锁定库存成功
    // 从session中获取当前用户
    MemberResVo memberResVo = LoginUserInterceptor.loginUser.get();

    // 1.验证令牌【 从redis中获取令牌与删除令牌必须保证原子性】
    // 注意；脚本返回的值是0或者1  0表示令牌删除了不存在或者删除失败  1表示调用了删除且删除成功了  也就是说0代表令牌校验失败
    String luaScript = "if redis.call('get',KEYS[1])==ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
    String token = vo.getOrderToken();

    // execute参数;  参数一：脚本对象  参数二：redis的key  参数三：要对比的值
    Long res = redisTemplate.execute(new DefaultRedisScript<Long>(luaScript,Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResVo.getId()),token);
    if(res == 1L){
        // 令牌验证成功
        // 去创建订单、验令牌、验价格、锁定库存
        OrderCreateTo order = createOrder();
        // 2. 验价
        BigDecimal orderPayprice = order.getOrder().getPayAmount();
        BigDecimal webPrice = vo.getPayprice();
        if(Math.abs(orderPayprice.subtract(webPrice).doubleValue()) < 0.01){
            // 金额对比成功 保存订单到数据库
            saveOrder(order);

            // 锁定库存  有异常，就回滚
            WareSkuLockVo wareLock = new WareSkuLockVo();
            wareLock.setOrderSn(order.getOrder().getOrderSn());
            List<OrderItemVo> locks = order.getItems().stream().map(item -> {
                OrderItemVo orderItemVo = new OrderItemVo();
                orderItemVo.setSkuId(item.getSkuId());
                orderItemVo.setCount(item.getSkuQuantity());
                orderItemVo.setTitle(item.getSkuName());
                return orderItemVo;
            }).collect(Collectors.toList());
            wareLock.setLocks(locks);

            // TODO 远程库存锁定
            // 库存成功了，但是因为网络超时，订单回滚了，库存还未回滚  可以考虑seata，但是其默认的AT模式不适合高并发场景
            // 为了保证高并发，库存服务自己回滚，可以发消息给库存服务。
            // 库存服务本身可以使用自动解锁模式
            R r = wmsFeignService.orderlock(wareLock);
            if(r.getCode() == 0){
                // 库存锁定成功
                resVo.setOrder(order.getOrder());
                // TODO 模拟远程异常
                // int a = 1/0;    // 订单回滚，库存没回滚
                // 订单创建成功，发送消息给MQ
                rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",order.getOrder());
                return resVo;
            } else {
                // 锁定失败
                resVo.setCode(3);  // 库存出现问题
                // return resVo;
                throw new NoStockException();
                // return resVo;
            }
        } else {
            resVo.setCode(2);   // 表示金额对比失败
            return resVo;
        }

    } else {
        resVo.setCode(1);
        return resVo;
    }
    /**  这种方式不能保证原子性，因此要使用上面的脚本方式以保证原子性操作
    String redisToken = redisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResVo.getId());
    if(StringUtils.equalsIgnoreCase(token,redisToken)){
        // 令牌验证通过

    } else{
        // 不通过

    }
     */
}

    // 根据订单号查询订单状态
    @Override
    public OrderEntity getOrderStatusByOrderSn(String orderSn) {
        OrderEntity one = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        return one;
    }

    @Override
    public void closeOrder(OrderEntity entity) {
        // 查询当前订单的状态
        OrderEntity orderEntity = this.getById(entity.getId());
        // 关单  待付款的才给关
        if(orderEntity.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()){
            OrderEntity order = new OrderEntity();
            order.setId(entity.getId());
            order.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(order);
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity,orderTo);
            try {
                // 解锁成功，将订单信息发给MQ
                // TODO 保证消息一定会发送出去,每一个消息做好记录（给数据库保存每一个消息的详细信息）
                // TODO 定期扫描数据库，将失败的消息重新发送
                rabbitTemplate.convertAndSend("order-event-exchange","order.release.other",orderTo);
            } catch (Exception e) {
                // TODO 出现问题，将没发送成功的消息进行重试发送

            }
        }
    }

    /* 秒杀单创建 */
    @Override
    public void createSeckillOrder(SeckillOrderTo seckillOrder) {
        // TODO 保存订单
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(seckillOrder.getOrderSn());
        orderEntity.setMemberId(seckillOrder.getMemberId());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        BigDecimal multiply = seckillOrder.getSeckillPrice().multiply(new BigDecimal(seckillOrder.getNum() + ""));
        orderEntity.setPayAmount(multiply);
        this.save(orderEntity);

        // TODO 保存订单项信息
        OrderItemEntity itemEntity = new OrderItemEntity();
        itemEntity.setOrderSn(seckillOrder.getOrderSn());
        itemEntity.setRealAmount(multiply);
        itemEntity.setSkuQuantity(seckillOrder.getNum());
        // TODO 获取当前sku详细信息  省略
        orderItemService.save(itemEntity);
    }

    // 保存订单数据
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);
        List<OrderItemEntity> items = order.getItems();
        // orderItemService.saveBatch(items);
        items.forEach(i -> {
            orderItemService.save(i);
        });
    }

    private OrderCreateTo createOrder(){
        OrderSubmitVo orderSubmitVo = confirmVoThreadLocal.get();
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        // 1. 生成订单号
        String orderSN = IdWorker.getTimeId();

        // 构建订单
        OrderEntity orderEntity = buildOrder(orderSN);

        // 2. 获取到所有的订单项
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSN);

        // 3. 计算价格相关
        computePrice(orderEntity,orderItemEntities);
        orderCreateTo.setOrder(orderEntity);
        orderCreateTo.setItems(orderItemEntities);

        return orderCreateTo;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        // 1. 订单总金额
        BigDecimal totalPrice = orderItemEntities.stream().map(OrderItemEntity::getRealAmount).reduce(new BigDecimal("0"), (t1, t2) -> t1.add(t2));
        BigDecimal couponAmount = orderItemEntities.stream().map(OrderItemEntity::getCouponAmount).reduce(new BigDecimal("0"), (t1, t2) -> t1.add(t2));
        BigDecimal integrationAmount = orderItemEntities.stream().map(OrderItemEntity::getIntegrationAmount).reduce(new BigDecimal("0"), (t1, t2) -> t1.add(t2));
        BigDecimal promotionAmount = orderItemEntities.stream().map(OrderItemEntity::getPromotionAmount).reduce(new BigDecimal("0"), (t1, t2) -> t1.add(t2));
        Integer gift = orderItemEntities.stream().map(OrderItemEntity::getGiftIntegration).reduce(0, (t1, t2) -> t1+t2);
        Integer grow = orderItemEntities.stream().map(OrderItemEntity::getGiftGrowth).reduce(0, (t1, t2) -> t1+t2);
        // 设置订单总金额
        orderEntity.setTotalAmount(totalPrice);
        // 设置应付总金额
        orderEntity.setPayAmount(totalPrice.add(orderEntity.getFreightAmount()));
        // 设置优惠总金额
        orderEntity.setCouponAmount(couponAmount);
        orderEntity.setIntegrationAmount(integrationAmount);
        orderEntity.setPromotionAmount(promotionAmount);

        orderEntity.setIntegration(gift);
        orderEntity.setGrowth(grow);

        // 设置删除状态  0表示未删除
        orderEntity.setDeleteStatus(0);
    }

    // 构建订单
    private OrderEntity buildOrder(String orderSN) {
        MemberResVo memberResVo = LoginUserInterceptor.loginUser.get();
        OrderSubmitVo orderSubmitVo = confirmVoThreadLocal.get();
        OrderEntity orderEntity = new OrderEntity();

        // 设置会员ID
        orderEntity.setMemberId(memberResVo.getId());

        // 2. 获取收货地址信息
        R r = wmsFeignService.getFace(orderSubmitVo.getAttrId());
        if(r.getCode() == 0){

            orderEntity.setOrderSn(orderSN);
            orderEntity.setCreateTime(new Date());
            JSONObject json = r.getData(new TypeReference<JSONObject>() {});
            String fare = json.getString("fare");
            // 运费
            orderEntity.setFreightAmount(new BigDecimal(fare));
            // 收货信息
            orderEntity.setReceiverCity(json.getString("city"));
            orderEntity.setReceiverDetailAddress(json.getString("detailAddress"));
            orderEntity.setReceiverPhone(json.getString("phone"));
            orderEntity.setReceiverName(json.getString("name"));
            orderEntity.setReceiverPostCode(Optional.ofNullable(json.getString("areacode")).orElse("000000"));
            orderEntity.setReceiverProvince(json.getString("province"));
            orderEntity.setReceiverRegion(json.getString("region"));

            // 设置订单状态
            orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
            orderEntity.setAutoConfirmDay(7);
        }
        return orderEntity;
    }

    // 构建所有订单项
    private List<OrderItemEntity> buildOrderItems(String orderSN) {
        List<OrderItemEntity> orderItems = null;
        //    2.1 获取当前用户的购物车数据
        List<OrderItemVo> currentCartItems = cartFeignService.getCurrentCartItems();
        if(!CollectionUtils.isEmpty(currentCartItems)){
            orderItems = currentCartItems.stream().map(cartItem -> {
                OrderItemEntity item = buildOrderItem(cartItem);
                item.setOrderSn(orderSN);
                return item;
            }).collect(Collectors.toList());
        }
        return orderItems;
    }

    // 根据购物车项构建订单项
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity itemEntity = new OrderItemEntity();
        // 1. spu信息
        Long skuId = cartItem.getSkuId();
        R r = productFeignService.getSpuInfoBySkuId(skuId);
        if(r.getCode() == 0){
            SpuInfoVo spuInfoVo = r.getData(new TypeReference<SpuInfoVo>() {
            });
            itemEntity.setSpuId(spuInfoVo.getId());
            itemEntity.setSpuBrand(spuInfoVo.getBrandId()+"");
            itemEntity.setSpuName(spuInfoVo.getSpuName());
            itemEntity.setCategoryId(spuInfoVo.getCatalogId());
        }

        // 2. sku信息
        itemEntity.setSkuId(cartItem.getSkuId());
        itemEntity.setSkuName(cartItem.getTitle());
        itemEntity.setSkuPic(cartItem.getImg());
        itemEntity.setSkuPrice(cartItem.getPrice());
        String skuAttrs = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        itemEntity.setSkuAttrsVals(skuAttrs);
        itemEntity.setSkuQuantity(cartItem.getCount());

        // 3. 优惠信息

        // 4. 积分信息
        itemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount()+"")).intValue());
        itemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount()+"")).intValue());

        // 5. 设置订单金额
        itemEntity.setPromotionAmount(new BigDecimal("0"));
        itemEntity.setCouponAmount(new BigDecimal("0"));
        itemEntity.setIntegrationAmount(new BigDecimal("0"));
        // 当前订单实际金额  订单金额-各种优惠信息
        BigDecimal totalPrice = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity() + ""));
        BigDecimal realPrice = totalPrice.subtract(itemEntity.getPromotionAmount()).subtract(itemEntity.getCouponAmount()).subtract(itemEntity.getIntegrationAmount());
        itemEntity.setRealAmount(realPrice);

        return itemEntity;
    }

    /*
    * 订单确认页需要的数据
    * */
    public OrderConfirmVo confirmOrder_general() {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberResVo memberResVo = LoginUserInterceptor.loginUser.get();
        // 1. 远程查询地址列表
        List<MemberAddressVo> address = memberFeignService.getAddress(memberResVo.getId());
        confirmVo.setAddress(address);

        // 2. 购物车所有选中的购物项
        List<OrderItemVo> cartItems = cartFeignService.getCurrentCartItems();
        confirmVo.setItems(cartItems);

        // 3. 查询用户积分
        Integer integration = memberResVo.getIntegration();
        confirmVo.setIntegration(integration);

        // 4. 其他数据自动计算

        // TODO 5. 防止重令牌

        return confirmVo;
    }

}
