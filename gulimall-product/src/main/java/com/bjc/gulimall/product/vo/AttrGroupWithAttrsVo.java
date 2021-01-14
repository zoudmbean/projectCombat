package com.bjc.gulimall.product.vo;

import com.bjc.gulimall.product.entity.AttrEntity;
import com.bjc.gulimall.product.entity.AttrGroupEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class AttrGroupWithAttrsVo extends AttrGroupEntity {
    private List<AttrEntity> attrs;
}
