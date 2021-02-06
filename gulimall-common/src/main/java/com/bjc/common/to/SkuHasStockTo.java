package com.bjc.common.to;

import lombok.Data;
import lombok.experimental.Accessors;

/*
* sku是否有库存VO
* */
@Data
@Accessors(chain = true)
public class SkuHasStockTo {
    private Long skuId;
    private Boolean hasStock;
}
