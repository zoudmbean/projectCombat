package com.bjc.gulimall.cart.config;

import com.bjc.gulimall.cart.intercepter.CartIntercepter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @描述：web配置
 * @创建时间: 2021/3/10
 */
@Component
public class WebConfig implements WebMvcConfigurer {

    /* 添加拦截器 */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 拦截所有请求
        registry.addInterceptor(new CartIntercepter()).addPathPatterns("/**");
    }
}
