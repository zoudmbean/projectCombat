package com.bjc.gulimall.product.vo;

import lombok.Data;

import java.util.List;

@Data
public class spuItemAttrGroupVo {
    private String groupName;
    private List<SpuBaseAttrVo> attrs;
}
