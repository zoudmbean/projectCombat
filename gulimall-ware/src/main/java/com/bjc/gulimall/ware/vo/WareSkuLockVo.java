package com.bjc.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @描述：库存锁定VO
 * @创建时间: 2021/3/22
 */
@Data
public class WareSkuLockVo {
    private String orderSn;                 // 订单号
    private List<OrderItemVo> locks;        // 需要锁住的所有库存信息
}
