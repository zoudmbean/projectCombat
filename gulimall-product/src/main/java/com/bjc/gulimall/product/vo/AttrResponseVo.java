package com.bjc.gulimall.product.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/*
* 用来做响应的
* */
@Data
@Accessors(chain = true)
public class AttrResponseVo extends AttrVo{
    private String catelogName;     // 所属分类名称
    private String groupName;       // 所属分组名称

    private Long[] catelogPath;     // 分类ID
}
