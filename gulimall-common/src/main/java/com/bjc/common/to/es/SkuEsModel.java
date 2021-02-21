package com.bjc.common.to.es;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/*
* es商品上架模型
* */
@Data
@Accessors(chain = true)
public class SkuEsModel {
    private Long skuId;
    private Long spuId;
    private String skuTitle;
    private BigDecimal skuPrice;
    private String skuImg;
    private Long saleCount;
    private Boolean hasStock;
    private Long hotScore;
    private Long brandId;
    private Long catalogId;
    private String brandName;
    private String brandImg;
    private String catalogName;

    private List<Attrs> attrs;


    @Data
    @Accessors(chain = true)
    public static class Attrs {
        private Long attrId;
        private String attrName;
        private String attrValue;
    }
}
