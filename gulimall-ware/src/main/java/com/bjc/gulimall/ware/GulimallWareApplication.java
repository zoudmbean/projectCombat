package com.bjc.gulimall.ware;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/*
* 引入RabbitMQ步骤：
*   1）pom依赖场景引入
*   2）配置文件，配置rabbitMQ的连接等基础信息
*   3）开启MQ，启动类上加注解：@EnableRabbit
* */
@EnableRabbit
@EnableFeignClients(basePackages = "com.bjc.gulimall.ware.feign")
@EnableDiscoveryClient              // 开启服务注册与发现
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class GulimallWareApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallWareApplication.class, args);
    }

}
