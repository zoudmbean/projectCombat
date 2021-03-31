package com.bjc.gulimall.order.vo;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 * @描述：购物车商品
 * @创建时间: 2021/3/19
 */
@Data
@ToString
public class OrderItemVo {
    private Long skuId;     // 商品Id
    private String title;   // 商品标题
    private String img;     // 商品默认图片
    private List<String> skuAttr;    // 商品套餐信息
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;

    // TODO 查询库存状态
    // private boolean hasStock = false;  // 是否有货  true有货  false无货

    private BigDecimal weight = new BigDecimal("0.1");  // 默认0.1kg
}
