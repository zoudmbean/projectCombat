/**
  * Copyright 2021 bejson.com
  */
package com.bjc.gulimall.product.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * Auto-generated: 2021-01-10 22:50:50
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
@Accessors(chain = true)
public class MemberPrice {

    private Long id;
    private String name;
    private BigDecimal price;

}
