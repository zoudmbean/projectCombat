package com.bjc.gulimall.ware.vo;

import lombok.Data;

/**
 * @描述：库存锁定成功vo
 * @创建时间: 2021/3/22
 */
@Data
public class LockStockResult {
    private Long skuId;
    private Integer num;
    private Boolean locked;
}
