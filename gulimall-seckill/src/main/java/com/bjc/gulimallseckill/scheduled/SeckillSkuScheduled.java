package com.bjc.gulimallseckill.scheduled;

import com.bjc.gulimallseckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @描述：秒杀商品定时上架
 * @创建时间: 2021/4/6
 */
@Component
@Slf4j
public class SeckillSkuScheduled {

    @Autowired
    SeckillService seckillService;

    @Autowired
    RedissonClient redissonClient;

    private final String UPLOAD_LOCK = "seckill:upload:lock";

    // TODO 幂等性处理（已上架的商品不能重复上架）
    @Scheduled(cron = "1 3 * * * ?")
    public void uploadSeckillLatest3Days(){
        log.info("上架秒杀商品。。。");
        // 添加分布式锁  锁的业务执行完成，状态已经更新完成，释放锁以后，其他人获取到就会拿到最新的状态
        RLock lock = redissonClient.getLock(UPLOAD_LOCK);
        try {
            lock.lock(10, TimeUnit.SECONDS);
            seckillService.uploadSeckillLatest3Days();
        }finally {
            lock.unlock();
        }

    }
}
