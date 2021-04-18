package com.bjc.gulimallseckill.service.impl;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.bjc.common.to.mq.SeckillOrderTo;
import com.bjc.common.utils.R;
import com.bjc.gulimallseckill.feign.CouponFeignService;
import com.bjc.gulimallseckill.feign.ProductFeignService;
import com.bjc.gulimallseckill.interceptor.LoginUserInterceptor;
import com.bjc.gulimallseckill.service.SeckillService;
import com.bjc.gulimallseckill.to.SeckillSkuRedisTo;
import com.bjc.gulimallseckill.vo.SeckillSessionsWithSkusVo;
import com.bjc.gulimallseckill.vo.SeckillSkuVo;
import com.bjc.gulimallseckill.vo.SkuInfoVo;
import com.sun.deploy.security.BlockedException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @描述：秒杀商品上架
 * @创建时间: 2021/4/6
 */
@Service
@Slf4j
public class SeckillServiceImpl implements SeckillService {

    private final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";
    private final String SKUKILL_CACHE_PREFIX = "seckill:skus";
    private final String SKU_STORE_SEMAPHORE = "seckill:stock:";        // + 商品随机码

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public void uploadSeckillLatest3Days() {
        // 1. 扫描最近三天需要参与秒杀的活动
        R session = couponFeignService.getLatest3DaysSession();
        if(session.getCode() == 0){
            // 上架
            List<SeckillSessionsWithSkusVo> sessionDatas = session.getData(new TypeReference<List<SeckillSessionsWithSkusVo>>() {
            });
            // 缓存到redis
            // 1. 缓存活动信息
            if(!CollectionUtils.isEmpty(sessionDatas)){
                saveSessionInfos(sessionDatas);
                // 2. 缓存活动的商品信息
                saveSessionSkuInfos(sessionDatas);
            } else {
                log.info("获取不到秒杀商品。。。");
            }
        }
    }

    /*
    * 降级方法：
    *   参数1：原方法的参数
    *   参数2：异常
    * 参数都可以省略
    * */
    public List<SeckillSkuRedisTo> getCurrentSeckillSkusFallBack(BlockException ex){
        log.error("原方法被限流了: " + ex);
        return null;
    }

    // value :资源名
    // blockHandler：降级方法，针对这个资源的回调
    @Override
    @SentinelResource(value="getCurrentSeckillSkusResource",blockHandler = "getCurrentSeckillSkusFallBack")
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        log.info("******************************************************");
        log.info("******************     获取秒杀商品  *******************");
        log.info("******************************************************");
        // 1. 确定当前时间属于哪个秒杀场次
        Long time = new Date().getTime();
        // 使用Sentinel自定义受保护的资源   seckillSkus:自定义资源名
        try(Entry entry = SphU.entry("seckillSkus")){
            Set<String> keys = redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
            for (String key : keys){
                String[] s = key.replace(SESSIONS_CACHE_PREFIX, "").split("_");
                Long start = Long.parseLong(s[0]);
                Long end = Long.parseLong(s[1]);
                if(time >= start && time <= end){
                    // 2. 获取这个秒杀场次需要的所有商品信息
                    List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                    BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                    List<String> list = hashOps.multiGet(range);
                    if(!CollectionUtils.isEmpty(list)){
                        List<SeckillSkuRedisTo> collect = list.stream().map(item -> {
                            SeckillSkuRedisTo redisTo = JSON.parseObject(item, SeckillSkuRedisTo.class);
                            // 将随机码设置为null
                            // redisTo.setRandomCode(null);
                            return redisTo;
                        }).collect(Collectors.toList());
                        return collect;
                    }
                    break;
                }
            }
        } catch (BlockException e){
            log.error("资源被限流：" + e);
        }
        return null;
    }

    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        // 1. 找到所有需要参与秒杀的商品的key信息
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> keys = hashOps.keys();
        if(!CollectionUtils.isEmpty(keys)){
            // 6_4
            String regx = "\\d_"+skuId;
            for (String key : keys) {
                if(Pattern.matches(regx,key)){
                    String str = hashOps.get(key);
                    SeckillSkuRedisTo redisTo = JSONObject.parseObject(str,SeckillSkuRedisTo.class);
                    // 处理随机码
                    long nowTime = new Date().getTime();
                    Long startTime = redisTo.getStartTime();
                    Long endTime = redisTo.getEndTime();
                    if(nowTime < startTime || nowTime > endTime){
                        redisTo.setRandomCode("");
                    }
                    return redisTo;
                }
            }
        }

        return null;
    }

    // TODO 上架秒杀商品的时候，每一个数据都有过期时间
    // TODO 秒杀后续的流程，简化了收货地址等信息
    @Override
    public String kill(String killId, String code, Integer num) {
        // 1. 判断登录没（拦截器已经做了）
        // 2. 获取当前秒杀商品的详情信息
        BoundHashOperations<String, String, String> hashOp = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        String s = hashOp.get(killId);
        if(StringUtils.isEmpty(s)){
            return null;
        }
        SeckillSkuRedisTo redisTo = JSONObject.parseObject(s, SeckillSkuRedisTo.class);
        // 3. 校验合法性
        //      3.1 校验秒杀时间
        Long start = redisTo.getStartTime();
        Long endTime = redisTo.getEndTime();
        long nowTime = new Date().getTime();
        if(nowTime < start || nowTime > endTime){
            return null;
        }
        //      3.2 校验随机码和商品ID
        String randomCode = redisTo.getRandomCode();
        Long skuId = redisTo.getSkuId();
        Long promotionSessionId = redisTo.getPromotionSessionId();
        if(!randomCode.equalsIgnoreCase(code) || !killId.equalsIgnoreCase(promotionSessionId+"_"+skuId)){
            return null;
        }
        //      3.3 验证购物数量是否合理  购买数量不能大于限制数量
        BigDecimal limit = redisTo.getSeckillLimit();
        if(num > limit.intValue()){
            return null;
        }
        //      3.4 验证这个人是否已经购买过了（幂等性），秒杀成功，就在redis中占位。  user_ID_sessionID_skuID
        Long userId = LoginUserInterceptor.loginUser.get().getId();

        String key = userId+"_"+promotionSessionId+skuId;
        // 过期时间就是endTime-nowTime
        Long ttl = endTime - nowTime;
        Boolean ifAbsent = redisTemplate.opsForValue().setIfAbsent(key,num.toString(),ttl, TimeUnit.MILLISECONDS);
        if(!ifAbsent){   // 为true，表示没买过
            return null;
        }

        // 分布式信号量做库存的减操作
        RSemaphore semaphore = redissonClient.getSemaphore(SKU_STORE_SEMAPHORE + randomCode);
        try {
            boolean tryAcquire = semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);
            if(!tryAcquire){
                return null;
            }
            // 秒杀成功
            // 快速下单。发送MQ消息
            String orderSN = IdWorker.getTimeId();
            SeckillOrderTo seckillOrderTo = new SeckillOrderTo();
            seckillOrderTo.setOrderSn(orderSN);
            seckillOrderTo.setNum(num);
            seckillOrderTo.setMemberId(userId);
            seckillOrderTo.setPromotionSessionId(promotionSessionId);
            seckillOrderTo.setSkuId(skuId);
            seckillOrderTo.setSeckillPrice(redisTo.getSeckillPrice());
            // 发送消息，订单服务监控这个消息，进行订单的创建流程
            rabbitTemplate.convertAndSend("order-event-exchange","order.seckill.order",seckillOrderTo);
            return orderSN;
        } catch (Exception e) { // 信号量没拿到，秒杀失败，直接返回null
            return null;
        } finally {
            semaphore.release();    // 最后需要释放
        }
    }

    private void saveSessionInfos(List<SeckillSessionsWithSkusVo> sessionDatas){
        sessionDatas.forEach(session -> {
            Long startTime = session.getStartTime().getTime();
            Long endTime = session.getEndTime().getTime();

            String key = SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;
            // 方法的幂等  redis中存在该key，就不需要保存了
            Boolean hasKey = redisTemplate.hasKey(key);
            if(!hasKey){
                List<String> skuids = session.getRelationSkus().stream().map(item -> item.getPromotionSessionId() + "_" + item.getSkuId().toString()).collect(Collectors.toList());
                // 缓存活动信息
                if(!CollectionUtils.isEmpty(skuids)){
                    redisTemplate.opsForList().leftPushAll(key,skuids);
                }
            }

        });
    }

    private void saveSessionSkuInfos(List<SeckillSessionsWithSkusVo> sessionDatas){
        sessionDatas.forEach(session -> {
            BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            session.getRelationSkus().stream().forEach(seckillSkuVo -> {
                // 保存到redis
                Boolean skuIdHasKey = ops.hasKey(seckillSkuVo.getPromotionSessionId() + "_" + seckillSkuVo.getSkuId().toString());
                // 4. 设置随机码
                String randomCode = UUID.randomUUID().toString().replaceAll("-", "");
                if(!skuIdHasKey){
                    // 缓存商品
                    SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();

                    // 1. sku的基本信息
                    R r = productFeignService.getSkuInfo(seckillSkuVo.getSkuId());
                    if(r.getCode() == 0){
                        SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        redisTo.setSkuInfo(skuInfo);
                    }

                    // 2. sku的秒杀信息
                    BeanUtils.copyProperties(seckillSkuVo,redisTo);

                    // 3.设置上下架时间
                    redisTo.setStartTime(session.getStartTime().getTime());
                    redisTo.setEndTime(session.getEndTime().getTime());


                    redisTo.setRandomCode(randomCode);

                    ops.put(seckillSkuVo.getPromotionSessionId() + "_" + seckillSkuVo.getSkuId().toString(), JSON.toJSONString(redisTo));

                    // 如果当前这个场次的商品的库存信息已经上架就不需要再次上架
                    // 使用库存作为分布式信号量（限流）
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STORE_SEMAPHORE + randomCode);
                    // 商品可以秒杀的数量作为信号量
                    semaphore.trySetPermits(seckillSkuVo.getSeckillCount().intValue());
                }
            });
        });
    }
}
