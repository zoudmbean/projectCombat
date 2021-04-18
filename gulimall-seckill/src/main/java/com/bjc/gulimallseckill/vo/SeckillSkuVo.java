package com.bjc.gulimallseckill.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @描述：sku
 * @创建时间: 2021/4/6
 */
@Data
public class SeckillSkuVo implements Serializable {
    private Long id;
    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private BigDecimal seckillCount;
    /**
     * 每人限购数量
     */
    private BigDecimal seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;
}
