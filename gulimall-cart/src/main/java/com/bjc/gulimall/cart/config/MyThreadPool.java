package com.bjc.gulimall.cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
// @EnableConfigurationProperties(ThreadPoolConfigProperties.class)
// 通过EnableConfigurationProperties将ThreadPoolConfigProperties注入到容器
// 因为ThreadPoolConfigProperties添加了@Components，所以这里不需要添加@EnableConfigurationProperties(ThreadPoolConfigProperties.class)
// 如果ThreadPoolConfigProperties没有添加@Components注解，那么这里需要添加@EnableConfigurationProperties(ThreadPoolConfigProperties.class)
// 才能在方法上注入容器中的组件ThreadPoolConfigProperties
public class MyThreadPool {
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolConfigProperties config){
        return new ThreadPoolExecutor(config.getCoreSize(),config.getMaxSize(),config.getKeepAliveTime(), TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>(100000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
    }
}
