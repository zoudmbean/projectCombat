package com.bjc.gulimall.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
* web配置类  可以配置视图解析器、拦截器、过滤器等
* */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /* 视图映射：
    * 配置view-controller，直接把请求地址和视图名称关联起来，不必写handler方法了
    * 针对仅仅只需要进行页面跳转的
    * 注意：这种路径映射方式只支持GET请求，不支持POST请求
    * 一个值得注意的场景：
    *   当POST请求到某个controller的处理器方法，同时，该controller的方法转发，例如：return "forward:/reg.html"
    *   这时候，会报错：Request method  'POST' not supported
    * */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // registry.addViewController("/login.html").setViewName("login");
        registry.addViewController("/reg.html").setViewName("reg");
    }

}
