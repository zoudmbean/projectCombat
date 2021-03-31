package com.bjc.gulimall.order.vo;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * @描述：订单确认页VO
 * @创建时间: 2021/3/19
 */
@Data
public class OrderConfirmVo {
    // 收货地址表 ums_member_receive_address表
    private List<MemberAddressVo> address;

    // 所有选中的购物项
    List<OrderItemVo> items;

    // 商品总数量
    public Integer getCount(){
        Integer reduce = Optional.ofNullable(items).orElseGet(ArrayList::new).stream().map(OrderItemVo::getCount).reduce(0, (n1, n2) -> n1 + n2);
        return reduce;
    }

    // 发票信息

    // 优惠券信息

    /**
     * 积分
     */
    private Integer integration;

    // 订单总金额
    BigDecimal total;

    public BigDecimal getTotal() {
        if(!CollectionUtils.isEmpty(items)){
            return items.stream()
                    .map(item -> item.getPrice().multiply(new BigDecimal(item.getCount().toString())))
                    .reduce(new BigDecimal("0"),(item1,item2) -> item1.add(item2));
        }
        return total;
    }

    // 应付金额
    BigDecimal payPrice;
    public BigDecimal getPayPrice() {
        if(!CollectionUtils.isEmpty(items)){
            return items.stream()
                    .map(item -> item.getPrice().multiply(new BigDecimal(item.getCount().toString())))
                    .reduce(new BigDecimal("0"),(item1,item2) -> item1.add(item2));
        }
        return payPrice;
    }

    // 订单防重令牌
    String orderToken;

    // 是否有库存
    private Map<Long,String> hasStockMap ;
}
