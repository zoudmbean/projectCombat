package com.bjc.gulimall.order.lister;

import com.bjc.common.to.mq.SeckillOrderTo;
import com.bjc.gulimall.order.entity.OrderEntity;
import com.bjc.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @描述：秒杀订单监听器
 * @创建时间: 2021/4/17
 */
@Component
@RabbitListener(queues = {"order.seckill.order.queue"})
@Slf4j
public class OrderSeckillListener {
    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void listener(Message msg, SeckillOrderTo seckillOrder, Channel channel) throws IOException {
        System.out.println("收到秒杀单：" + seckillOrder.getOrderSn());

        try{
            log.info("准备创建秒杀单独 详细信息");
            orderService.createSeckillOrder(seckillOrder);
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(msg.getMessageProperties().getDeliveryTag(),true);
        }

    }
}
