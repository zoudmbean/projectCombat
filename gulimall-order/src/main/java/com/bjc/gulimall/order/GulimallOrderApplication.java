package com.bjc.gulimall.order;

import com.alibaba.cloud.seata.GlobalTransactionAutoConfiguration;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/*
* RabbitMq使用步骤
*   1）引入依赖
*       <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>
    2）开启rabbit注解@EnableRabbit
    3）配置rabbitMQ
       spring:
            rabbitmq:
            host: 192.168.56.10
            port: 5672
            virtual-host: /
    4）使用AmqpAdmin创建交换机、队列、绑定关系
    5）使用RabbitTemplate，发送消息
    6）监听消息，两个注解
        6.1 @RabbitListener注解。该注解必须开启@EnableRabbit
        // queues 用于声明要监听的队列
        @RabbitListener(queues = {"hello-java-queue"})
        public void recievwMsg(Object msg){
            System.out.println("接收到的消息：" + msg + "  类型：" + msg.getClass());
        }
        注意：
            1. @RabbitListener注解声明的方法所在的类必须在spring容器中。
            2. 该注解可以标注在方法上，也可以标注在类上
        6.2 @RabbitHandler接收消息，与@RabbitListener注解配合使用，@RabbitListener注解标注在类上，@RabbitHandler标注在方法上，可以处理多类型的消息
            例如：生产者发送的消息类型不确定，可以用多个@RabbitHandler标注多个方法分别接收处理
            @RabbitHandler
            public void recievwMsg(Message msg, OrderEntity orderEntity, Channel channel){
                System.out.println("接收到的消息：" + msg + "  类型：" + msg.getClass());
            }

            @RabbitHandler
            public void recievwMsg(Message msg, OrderItemEntity orderItemEntity, Channel channel){
                System.out.println("接收到的消息：" + msg + "  类型：" + msg.getClass());
            }
            注意；该注解只能标注在方法上

    seata控制分布式事务使用步骤：
    *   1） 每一个微服务先必须创建undo_log表
    *   2）安装事务协调器：seata-server:https://github.com/seata/seata/releases
    *   3）引入seata依赖
    *   4）启动seata-server
    *       registry.conf：注册中心配置  修改type为nacos
    *   5）开启全局事务：只需要使用一个 @GlobalTransactional 注解在业务方法上
    *   6）所有想要用到分布式事务的微服务使用seata DataSourceProxy代理自己数据源
    *       因为 Seata 通过代理数据源实现分支事务，如果没有注入，事务无法成功回滚
    *   7）每个微服务都需要导入seata两个配置文件
    *
* */
@EnableRabbit
@EnableRedisHttpSession
@EnableDiscoveryClient
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, GlobalTransactionAutoConfiguration.class})
@EnableFeignClients("com.bjc.gulimall.order.feign")
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
