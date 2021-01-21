package com.bjc.gulimall.ware.vo;

import lombok.Data;

/*
* 采购项
* */
@Data
public class PurchaseItemDoneVo {
    private Long itemId;
    private Integer status;     //
    private String reason;
}
