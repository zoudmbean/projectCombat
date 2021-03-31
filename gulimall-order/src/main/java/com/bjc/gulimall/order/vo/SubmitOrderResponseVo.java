package com.bjc.gulimall.order.vo;

import com.bjc.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @描述：下单确认VO
 * @创建时间: 2021/3/21
 */
@Data
public class SubmitOrderResponseVo {
    private OrderEntity order;
    private Integer code;       // 状态码  0=成功
}
