package com.bjc.gulimall.member.exception;

/**
 * @描述：手机号存在异常
 * @创建时间: 2021/3/1
 */
public class PhoneExistException extends RuntimeException{
    public PhoneExistException() {
        super("手机号已存在！");
    }
}
