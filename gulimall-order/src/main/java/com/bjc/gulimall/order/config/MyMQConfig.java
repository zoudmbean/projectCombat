package com.bjc.gulimall.order.config;

import com.bjc.gulimall.order.entity.OrderEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @描述：创建交换器、队列的组件
 * @创建时间: 2021/3/28
 */
@Configuration
public class MyMQConfig {
    //                         order.release.order.queue  移动到OrdercloseListener监听器
    /*@RabbitListener(queues = {"order.release.order.queue"})
    public void listener(Message msg,OrderEntity entity, Channel channel) throws IOException {
        System.out.println("收到过期的订单信息，准备关闭订单：" + entity.getOrderSn());
        channel.basicAck(msg.getMessageProperties().getDeliveryTag(),false);
    }*/

    // @Bean Binding Queue Exchange

    /**
     * 容器中的这些组件都会自动创建（RabbitMQ中没有的时候）
     * */
    @Bean
    public Queue orderDelayQueue(){     // 延时队列（死信队列）
        // public Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)
        // 设置死信队列参数
        /*
        *   x-dead-letter-exchange:order-event-exchange
            x-dead-letter-router-key:order.release.order
            x-message-ttl:60000
        * */
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","order-event-exchange");     // 指定死信路由
        arguments.put("x-dead-letter-routing-key","order.release.order");     // 指定死信路由键（消息死了，要通过哪个路由键交出去）
        arguments.put("x-message-ttl",60000);                               // 指定消息过期时间 单位毫秒
        // 参数1：队列名称
        // 参数2：是否持久化
        // 参数3：是否排他
        // 参数4：是否自动删除
        // 参数5：队列的属性
        Queue queue = new Queue("order.delay.queue", true, false, false,arguments);
        return queue;
    }

    @Bean
    public Queue orderReleaseOrderQueue(){      // 普通队列
        Queue queue = new Queue("order.release.order.queue", true, false, false);
        return queue;
    }

    /*
    * 一个微服务只设定一个交换机，交换机绑定多个队列，就需要将交换机设置成topic模式
    * */
    @Bean
    public Exchange orderEventExchange(){
        // public TopicExchange(String name, boolean durable, boolean autoDelete, Map<String, Object> arguments)
        // 参数一；交换机名称
        // 参数2：是否持久化
        // 参数3：是否自动删除
        // 参数4：交换机属性参数
        TopicExchange topicExchange = new TopicExchange("order-event-exchange", true, false);
        return topicExchange;
    }

    // 绑定延时队列  绑定的路由键为延时队列到交换机的路由键
    @Bean
    public Binding orderCreateOrderBinding(){
        // public Binding(String destination, Binding.DestinationType destinationType, String exchange, String routingKey, Map<String, Object> arguments)
        // 参数1：目的地
        // 参数2：目的地类型
        // 参数3：待绑定的交换机名称
        // 参数4：路由键
        // 参数5：属性参数
        return new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order",
                null);
    }

    // 普通队列与交换机的绑定关系
    @Bean
    public Binding orderReleaseOrderBinding(){
        return new Binding("order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order",
                null);
    }

    // 订单交换机与库存系统的队列进行绑定，当订单取消订单的时候，可以实时通知库存进行解锁库存
    @Bean
    public Binding orderReleaseOtherBinding(){
        // 明确绑定的队列  库存的解锁队列
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.other.#",
                null);
    }
}
