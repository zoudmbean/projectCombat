package com.bjc.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
* 整合myBatis-plus
*   1） 导入依赖（放在common工程了）
*   2） 配置（参照官网配置）
*       2.1 数据源
*       2.2 mybatis-plus相关配置
*           2.2.1 首先在启动类加上注解MapperScan
*           2.2.2 配置sql映射文件位置
* */
@MapperScan("com.bjc.gulimall.product.dao")
@SpringBootApplication
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
