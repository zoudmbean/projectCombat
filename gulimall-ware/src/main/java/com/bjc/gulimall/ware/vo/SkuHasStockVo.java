package com.bjc.gulimall.ware.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/*
* sku是否有库存VO
* */
@Data
@Accessors(chain = true)
public class SkuHasStockVo {
    private Long skuId;
    private Boolean hasStock;
}
