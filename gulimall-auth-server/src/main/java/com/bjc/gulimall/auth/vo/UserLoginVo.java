package com.bjc.gulimall.auth.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @描述：用户登录VO
 * @创建时间: 2021/3/3
 */
@Data
public class UserLoginVo {
    @NotEmpty(message="账号不能为空")
    private String loginacct;

    @NotEmpty(message = "密码不能为空")
    private String password;
}
