package com.bjc.gulimall.cart.vo;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * @描述：整个购物车
 * @创建时间: 2021/3/8
 * 需要计算的属性，必须重写它的get方法，保证每次获取属性都会进行计算
 */
@Data
public class Cart {
    private List<CartItem> items;
    private Integer countNum;           // 总数量
    private Integer countType;          // 同类型商品数量
    private BigDecimal totalAmount;     // 商品总价
    private BigDecimal reduce = new BigDecimal("0");          // 优惠了多少

    public Integer getCountNum() {
        if(!CollectionUtils.isEmpty(items)){
            return items.stream().mapToInt(item -> item.getCount()).sum();
        }
        return 0;
    }

    public Integer getCountType() {
        if(!CollectionUtils.isEmpty(items)){
            long count = items.stream().mapToInt(item -> item.getCount()).count();
            return Integer.valueOf(count+"");
        }
        return 0;
    }

    public BigDecimal getTotalAmount() {
        if(!CollectionUtils.isEmpty(items)){
            return items.stream().map(CartItem::getTotalPrice).reduce(new BigDecimal("0"),(item1, item2) -> item1.add(item2));
        }
        return new BigDecimal("0");
    }

    public BigDecimal getReduce() {
        return reduce;
    }
}
