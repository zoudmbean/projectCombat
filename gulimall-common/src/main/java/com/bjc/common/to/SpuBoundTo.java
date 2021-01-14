package com.bjc.common.to;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/*
* Spu积分TO
* To用于做远程数据传输用的，由于可能多个服务要用到，所以放在common工程
* */
@Data
@Accessors(chain = true)
public class SpuBoundTo {
    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
