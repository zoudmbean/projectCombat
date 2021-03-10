package com.bjc.gulimall.cart.to;

import lombok.Data;

/**
 * @描述：用户登录识别的传输对象
 * @创建时间: 2021/3/10
 */
@Data
public class UserInfoTo {
    private Long userid;
    private String userKey;

    private boolean tempUser = false;
}
