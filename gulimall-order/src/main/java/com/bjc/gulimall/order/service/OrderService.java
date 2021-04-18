package com.bjc.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bjc.common.to.mq.SeckillOrderTo;
import com.bjc.common.utils.PageUtils;
import com.bjc.gulimall.order.entity.OrderEntity;
import com.bjc.gulimall.order.vo.OrderConfirmVo;
import com.bjc.gulimall.order.vo.OrderSubmitVo;
import com.bjc.gulimall.order.vo.SubmitOrderResponseVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author zoudm
 * @email zoudmbean@163.com
 * @date 2020-12-08 23:26:55
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    SubmitOrderResponseVo submitOrder(OrderSubmitVo orderSubmitVo);

    OrderEntity getOrderStatusByOrderSn(String orderSn);

    void closeOrder(OrderEntity entity);

    void createSeckillOrder(SeckillOrderTo seckillOrder);

}

