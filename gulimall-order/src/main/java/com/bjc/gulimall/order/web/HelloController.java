package com.bjc.gulimall.order.web;

import com.bjc.gulimall.order.entity.OrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.UUID;

/**
 * @描述：页面跳转测试类
 * @创建时间: 2021/3/18
 */
@Controller
public class HelloController {
    @GetMapping("/to/{page}.html")
    public String listPage(@PathVariable("page") String page){
        return page;
    }

    @Autowired
    RabbitTemplate template;

    @GetMapping("/test/createOrder")
    @ResponseBody
    public String createOrderTest(){
        OrderEntity order = new OrderEntity();
        order.setOrderSn(UUID.randomUUID().toString());
        order.setCreateTime(new Date());
        // 给MQ发送消息(发给延时队列)
        template.convertAndSend("order-event-exchange","order.create.order",order);
        return "ok";
    }
}
