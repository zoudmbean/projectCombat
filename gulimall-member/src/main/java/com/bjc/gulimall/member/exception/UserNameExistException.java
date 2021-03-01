package com.bjc.gulimall.member.exception;

/**
 * @描述：用户名存在异常
 * @创建时间: 2021/3/1
 */
public class UserNameExistException extends RuntimeException {
    public UserNameExistException() {
        super("用户名已存在！");
    }
}
