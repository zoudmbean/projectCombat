package com.bjc.gulimall.order;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class GulimallOrderApplicationTests {

    @Autowired
    RabbitTemplate template;

    @Autowired
    AmqpAdmin amqpAdmin;

    @Test
    public void sendTest(){
        // 发送消息
        String msg = "hello world";
        template.convertAndSend("hello-java-exchange","hello.java",msg);
        System.out.println("消息发送完成！" + msg);
    }

    /*
    *   如何创建Exchange、Queue、Binding
    *       1）使用AmqpAdmin进行创建
    *   如何收发消息
    * */
    @Test
    public void createExchange() {
        DirectExchange directExchange = new DirectExchange("hello-java-exchange",true,false);
        amqpAdmin.declareExchange(directExchange);
        System.out.println("exchange创建成功（hello-java-exchange）");
    }

    @Test
    public void createQueue(){
        Queue queue = new Queue("hello-java-queue",true,false,false);
        amqpAdmin.declareQueue(queue);
        System.out.println("queue创建成功（hello-java-queue）");
    }

    // 创建绑定
    @Test
    public void createBinding(){
        Binding binding = new Binding("hello-java-queue", Binding.DestinationType.QUEUE,"hello-java-exchange","hello.java",null);
        amqpAdmin.declareBinding(binding);
        System.out.println("binding创建成功（hello-java-bind）");
    }

}
