package com.bjc.gulimallseckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @描述：定时任务测试
 * @创建时间: 2021/4/5
 * 使用步骤：
 *      1）开启定时任务 @EnableScheduling
 *      2）定义定时任务 @Scheduled
 */
//@EnableScheduling       // 开启定时任务
//@EnableAsync
//@Component
@Slf4j
public class HelloScheduled {
    /*
    * 1）spring中cron只有6位  最后的7位的年份不能写
    * */
    //@Scheduled(cron = "* * * * * ?")
    //@Async
    public void hello(){
        log.info("hello...");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
