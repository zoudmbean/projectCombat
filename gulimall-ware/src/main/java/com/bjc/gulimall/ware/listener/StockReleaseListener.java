package com.bjc.gulimall.ware.listener;

import com.alibaba.fastjson.TypeReference;
import com.bjc.common.to.mq.OrderTo;
import com.bjc.common.to.mq.StockDetailTo;
import com.bjc.common.to.mq.StockLockTo;
import com.bjc.common.utils.R;
import com.bjc.gulimall.ware.dao.WareSkuDao;
import com.bjc.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.bjc.gulimall.ware.entity.WareOrderTaskEntity;
import com.bjc.gulimall.ware.feign.OrderFeignService;
import com.bjc.gulimall.ware.service.WareOrderTaskDetailService;
import com.bjc.gulimall.ware.service.WareOrderTaskService;
import com.bjc.gulimall.ware.service.WareSkuService;
import com.bjc.gulimall.ware.vo.OrderVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @描述：库存释放RabbitMQ监听器
 * @创建时间: 2021/3/30
 */
@RabbitListener(queues = {"stock.release.stock.queue"}) // stock.release.stock.queue
@Component
public class StockReleaseListener {

    @Autowired
    private WareSkuService WareSkuService;

    /*
     * 添加解锁库存的功能
     * 库存解锁的场景：
     *   1）下单成功，但是订单过期未支付，被系统自动（用户主动）取消了
     *   2）下单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚，之前锁定的库存需要解锁（使用seata分布式事务太慢了，我们希望可以自动解锁）
     *
     *   注意：只要解锁库存的消息失败，一定要告诉mq服务器，此次解锁失败，消息不要删除，因此，需要设置消息的ACK为手动确认方式
     * */
    @RabbitHandler
    public void handleLockStockRelease(Message msg, StockLockTo stockLockTo, Channel channel) throws IOException {
        System.out.println("收到解锁库存的信息：");
        try{
            WareSkuService.unLockStock(stockLockTo);
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            channel.basicReject(msg.getMessageProperties().getDeliveryTag(),true);
        }
    }

    @RabbitHandler
    public void handlerOrderCloseRelease(Message msg, OrderTo orderTo, Channel channel) throws IOException {
        System.out.println("收到订单取消消息：" + orderTo);
        try{
            WareSkuService.unLockStock(orderTo);
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            channel.basicReject(msg.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
