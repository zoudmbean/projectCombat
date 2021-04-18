package com.bjc.gulimall.order.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.PostConstruct;

/**
 * @描述：rabbitMQ配置类
 * @创建时间: 2021/3/15
 */
@Configuration
public class MyRabbitConfig {

    /*
    * 如果一个类只有一个有参构造器，那么这个参数就会从容器中获取
    *   不用 @Autowired 解决循环依赖
    * */
    // @Autowired
    RabbitTemplate rabbitTemplate;

    // TODO  RabbitTemplate其他配置需要设置
    @Primary
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory factory){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(factory);
        this.rabbitTemplate = rabbitTemplate;
        rabbitTemplate.setMessageConverter(messageConverter());
        initRabbitTemplate();
        return rabbitTemplate;
    }

    /* 定制rabbitTemplate
    *       1. 服务收到消息就回调
    *           1.1 开启发送端确认模式：spring.rabbitmq.publisher-confirms=true
                1.2 设置确认回调confirmCallback
    *       2. 消息正确抵达队列进行回调
    *           2.1 开启发送端消息抵达队列确认：spring.rabbitmq.publisher-returns=true
                2.2 可选配置：只要抵达队列，就会以异步发送，优先回调这个returnConfirm
                    spring.rabbitmq.template.mandatory=true
                2.3 设置确认回调ReturnCallback
    *       3. 消费端确认（保证每个消息被正确消费，此时才可以broker删除这个消息）
    *           3.1 开启手动签收模式：spring.rabbitmq.listener.simple.acknowledge-mode=manual
    *               默认是自动确认的，只要消息接收到，客户端会自动确认，服务端就会移除这个消息。
    *               问题：我们收到很多消息，自动回复给服务器ack，只有一个消息处理成功，宕机了，发生消息丢失
    *                   消费者手动确认模式，只要我们没有明确告诉MQ，货物被签收，没有ACK，消息就一直是unacked状态，即使consumer宕机了，消息也不会丢失，会重新变成ready状态，下次有消费者连接进来继续消费
    *           3.2 如何签收：
    *               channel.basicAck(deliveryTag,false);        签收，业务成功完成，签收
    *               channel.basicNack(deliveryTag,false,true);  拒签，业务失败，拒签
    *
    * */
    // @PostConstruct  // 该注解的意思是，在MyRabbitConfig的构造器创建完成之后，调用注解所在的方法
    public void initRabbitTemplate(){
        // 设置确认回调
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            // correlationData：当前消息的唯一关联数据（这个是消息的唯一ID）
            // ack：代表消息是成功收到
            // cause：如果失败，失败的原因
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                // 服务器收到消息了
                // 修改消息的状态
                System.out.println(correlationData + "   ack = " + ack + "  cause = " + cause);
            }
        });

        // 设置消息抵达队列的确认回调
        // 触发时机，只要消息没有投递到指定队列，就触发这个失败回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
            * @Param: message       投递失败的消息详细信息
            * @Param: replyCode     回复的状态码
            * @Param: replyText     回复的文本内容
            * @Param: exchange      当时这个消息发送的交换器
            * @Param: routingkey    当时这个消息发送的时候用的路由键
            */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingkey) {
                // 报错误了，修改数据库，当前消息的状态
                System.out.println("fail: message = " + message + "   replyCode = " + replyCode + "  replyText = " + replyText + "   exchange = " + exchange + "   routingkey = " + routingkey);
            }
        });

    }

    /* 使用JSON序列化对象 */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

}
