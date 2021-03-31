package com.bjc.gulimall.order.to;

import com.bjc.gulimall.order.entity.OrderEntity;
import com.bjc.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @描述：订单创建对象
 * @创建时间: 2021/3/21
 */
@Data
public class OrderCreateTo {
    private OrderEntity order;
    private List<OrderItemEntity> items;
    private BigDecimal payPrice;                // 订单计算的应付价格
    private BigDecimal fare;                    // 运费

}
