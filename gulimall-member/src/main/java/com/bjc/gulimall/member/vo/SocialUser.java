package com.bjc.gulimall.member.vo;

import lombok.Data;

/**
 * @描述：社交用户信息
 * @创建时间: 2021/3/5
 */
@Data
public class SocialUser {
    private String access_token;
    private String remind_in;
    private String expires_in;
    private String uid;
    private boolean isRealName;
}
