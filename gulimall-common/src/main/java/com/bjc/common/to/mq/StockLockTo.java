package com.bjc.common.to.mq;

import lombok.Data;

import java.util.List;

/**
 * @描述：库存锁定TO
 * @创建时间: 2021/3/29
 */
@Data
public class StockLockTo {
    private Long id;    // 库存工作单id
    private StockDetailTo detail;   // 工作单详情
}
