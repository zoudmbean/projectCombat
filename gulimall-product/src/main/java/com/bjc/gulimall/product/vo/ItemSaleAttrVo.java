package com.bjc.gulimall.product.vo;

import lombok.Data;

import java.util.List;

@Data
public class ItemSaleAttrVo {
    private Long attrId;
    private String attrName;
    private List<AttrValueWithSkuIdVo> attrValues;
}
