package com.bjc.gulimall.product.vo;

import com.bjc.gulimall.product.entity.AttrEntity;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AttrVo extends AttrEntity {
    private Long attrGroupId;
}
