package com.bjc.gulimall.member;

import com.alibaba.cloud.seata.GlobalTransactionAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 1、想要远程调用别的服务
 *      1）引入open feign
 *      2）编写一个接口，告诉SpringCloud这个接口需要调用远程服务
 *          2.1 声明接口的每一个方法都是调用哪个远程服务的请求
 * 2. 开启远程调用功能（@EnableFeignClients(basePackages = "com.bjc.gulimall.member.feign")）
 * */
@EnableDiscoveryClient      // 开启服务注册与发现
@EnableFeignClients(basePackages = "com.bjc.gulimall.member.feign")
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class GulimallMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallMemberApplication.class, args);
    }

}
