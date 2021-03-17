package com.bjc.gulimall.order.service.impl;

import com.bjc.gulimall.order.entity.OrderEntity;
import com.bjc.gulimall.order.entity.OrderReturnReasonEntity;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bjc.common.utils.PageUtils;
import com.bjc.common.utils.Query;

import com.bjc.gulimall.order.dao.OrderItemDao;
import com.bjc.gulimall.order.entity.OrderItemEntity;
import com.bjc.gulimall.order.service.OrderItemService;

@RabbitListener(queues = {"hello-java-queue"})
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    // queues 用于声明要监听的队列
    // class org.springframework.amqp.core.Message
    // 参数可以写以下类型
    //      1）原生的message
    //      2）消息的实际类型,例如：OrderEntity orderEntity
    //      3）通道Channel：当前传输数据的通道
    /*
    * Queue 可以多人监听，只要收到消息，队列就会删除消息，而且只能有一个收到此消息
    * 场景：
    *   1）订单服务启动多个，同一个消息，只能有一个客户端收到
    *   2）只有一个消息完全处理完，方法运行结束，我们就可以接收到下一个消息
    * */
    // @RabbitListener(queues = {"hello-java-queue"})
    @RabbitHandler
    public void recievwMsg(Message msg, OrderReturnReasonEntity orderReturnReasonEntity, Channel channel) {
        System.out.println("接收到的消息：" + msg + "  类型：" + msg.getClass() + "   orderReturnReasonEntity = " + orderReturnReasonEntity);

        // deliveryTag：当前消息的标签，channel内按顺序自增
        long deliveryTag = msg.getMessageProperties().getDeliveryTag();
        try {
            if(deliveryTag % 2 == 0){
                // 签收  参数1：当前消息的标签  参数2：是否批量
                channel.basicAck(deliveryTag,false);
            } else {
                // 拒签(两种方式)

                // 方式一：
                // 参数：long deliveryTag  当前消息的标签
                //      boolean multiple   是否批量模式
                //      boolean requeue    是否重新入队  true 表示拒收之后发回服务器重新入队  false 表示拒收之后直接丢弃消息
                channel.basicNack(deliveryTag,false,true);

                // 方式二：
                // 参数：long deliveryTag,  boolean requeue
                //channel.basicReject(deliveryTag,true);
            }
        } catch (Exception e) {
            // 网络断开
        }
    }

    @RabbitHandler
    public void recievwMsg(Message msg, OrderEntity orderEntity, Channel channel){
        System.out.println("接收到的消息：" + msg + "  类型：" + msg.getClass() + "   orderEntity = " + orderEntity);
    }

}
