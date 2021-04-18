package com.bjc.common.to.mq;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @描述：秒杀订单
 * @创建时间: 2021/4/17
 */
@Data
public class SeckillOrderTo {
    private String orderSn;                 // 订单号
    private Long promotionSessionId;        // 活动场次ID
    private Long skuId;                     // 商品ID
    private BigDecimal seckillPrice;        // 秒杀价格
    private Integer num;                 // 秒杀数量
    private Long memberId;              // 会员id
}
