package com.bjc.gulimall.order.lister;

import com.bjc.gulimall.order.entity.OrderEntity;
import com.bjc.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @描述：关单监听器
 * @创建时间: 2021/3/31
 */
@Component
@RabbitListener(queues = {"order.release.order.queue"})
public class OrderCloseListener {

    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void listener(Message msg, OrderEntity entity, Channel channel) throws IOException {
        System.out.println("收到过期的订单信息，准备关闭订单：" + entity.getOrderSn());

        try{
            orderService.closeOrder(entity);

            channel.basicAck(msg.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(msg.getMessageProperties().getDeliveryTag(),true);
        }

    }
}
