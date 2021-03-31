package com.bjc.gulimall.order.vo;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @描述：订单提交确认VO
 * @创建时间: 2021/3/21
 */
@Data
@ToString
public class OrderSubmitVo {
    private Long attrId;        // 收货地址ID
    private Integer payType;    // 字符方式
    // 无需提交要购买的商品，去购物车再获取一遍商品即可

    // 优惠、发票等

    private String orderToken;  // 防重令牌
    private BigDecimal payprice;// 应付价格  验价
    private String note;        // 订单备注

    // 用户相关信息，从session中获取
}
