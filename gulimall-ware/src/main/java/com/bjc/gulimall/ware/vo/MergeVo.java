package com.bjc.gulimall.ware.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/*
* 合并采购单
* */
@Data
@Accessors(chain = true)
public class MergeVo {
    // 整单ID
    private Long purchaseId;
    // 合并项集合
    private List<Long> items;
}
