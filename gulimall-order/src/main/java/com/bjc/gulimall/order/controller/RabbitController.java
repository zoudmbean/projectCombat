package com.bjc.gulimall.order.controller;

import com.bjc.gulimall.order.entity.OrderEntity;
import com.bjc.gulimall.order.entity.OrderReturnReasonEntity;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

/**
 * @描述：RabbitMq测试类
 * @创建时间: 2021/3/16
 */
@RestController
public class RabbitController {
    @Autowired
    RabbitTemplate template;


    @GetMapping("/sendMq")
    public String seanMq(@RequestParam(value = "num",defaultValue = "10") Integer num){
        for(int i = 0 ; i < num ; i++){
            if(i%2 == 0){
                OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
                reasonEntity.setCreateTime(new Date());
                reasonEntity.setId(1L);
                reasonEntity.setName("嗨_" + i);
                // template.convertAndSend("hello-java-exchange","hello.java",reasonEntity);
                // 发送带消息的唯一ID的消息
                template.convertAndSend("hello-java-exchange","hello.java",reasonEntity,new CorrelationData(UUID.randomUUID().toString()));
            } else {
                OrderEntity entity = new OrderEntity();
                entity.setOrderSn(UUID.randomUUID().toString());
                // template.convertAndSend("hello-java-exchange","hello1.java",entity);
                template.convertAndSend("hello-java-exchange","hello1.java",entity,new CorrelationData(UUID.randomUUID().toString()));
            }
        }
        return "ok";
    }
}
