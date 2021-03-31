package com.bjc.gulimall.ware.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @描述：rabbitMQ配置类
 * @创建时间: 2021/3/15
 */
@Configuration
public class MyRabbitConfig {

    /* 使用JSON序列化对象 */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }
/*
    @RabbitListener(queues = {"stock.release.stock.queue"})
    public void handle(Message msg){
        System.out.println(Thread.currentThread().getName() + "\t" + "" + msg);
    }
*/
    /*
    * 配置绑定关系等信息
    * */
    // 1. 库存服务默认交换机（topic类型）
    @Bean
    public Exchange stockEventExchange(){
        // public TopicExchange(String name, boolean durable, boolean autoDelete, Map<String, Object> arguments)
        TopicExchange topicExchange = new TopicExchange("stock-event-exchange", true, false);
        return topicExchange;
    }

    // 2. 普通队列
    @Bean
    public Queue stockReleaseStockQueue(){
        // public Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)
        return new Queue("stock.release.stock.queue",true,false,false);
    }

    // 3. 创建延时队列
    @Bean
    public Queue stockDeadStockQueue(){
        // public Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","stock-event-exchange");     // 指定死信路由
        arguments.put("x-dead-letter-routing-key","stock.release");         // 指定死信路由键（消息死了，要通过哪个路由键交出去）
        arguments.put("x-message-ttl",120000);                               // 指定消息过期时间 单位毫秒
        return new Queue("stock.delay.queue",true,false,false,arguments);
    }

    // 4. 创建2个绑定关系
    //      4.1 绑定死信队列与交换机的关系
    @Bean
    public Binding deadBind(){
        // Binding(String destination, Binding.DestinationType destinationType, String exchange, String routingKey, Map<String, Object> arguments)
        return new Binding("stock.delay.queue", Binding.DestinationType.QUEUE,"stock-event-exchange","stock.locked",null);
    }

    //      4.2 绑定普通队列与交换机的关系
    @Bean
    public Binding releaseBind(){
        // Binding(String destination, Binding.DestinationType destinationType, String exchange, String routingKey, Map<String, Object> arguments)
        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE,"stock-event-exchange","stock.release.#",null);
    }

}
