package com.bjc.gulimall.cart.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * @描述：购物项内容
 * @创建时间: 2021/3/8
 */
@Data
@Accessors(chain = true)
public class CartItem {
    private Long skuId;     // 商品Id
    private Boolean check = true;   // 是否选中
    private String title;   // 商品标题
    private String img;     // 商品默认图片
    private List<String> skuAttr;    // 商品套餐信息
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;

    // 计算当前项的总价
    public BigDecimal getTotalPrice() {
        BigDecimal decimal = Optional.ofNullable(this.price).orElse(new BigDecimal("0")).multiply(new BigDecimal(Optional.ofNullable(this.count).orElse(0) + ""));
        return decimal;
    }
}
