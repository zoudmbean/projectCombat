package com.bjc.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/*
* 整合myBatis-plus
*   1） 导入依赖（放在common工程了）
*   2） 配置（参照官网配置）
*       2.1 数据源
*       2.2 mybatis-plus相关配置
*           2.2.1 首先在启动类加上注解MapperScan
*           2.2.2 配置sql映射文件位置
*
* myBatis-plus的逻辑删除：
*   1）在application.yml中配置如下（如果表中的逻辑删除字段的值就是与下面的值一致，那么可以不配置，因为mybatis-plus默认就加了以下配置）
*       mybatis-plus:
            global-config:
                db-config:
                    id-type: auto # 配置主键自增
                    logic-delete-value: 0         # 逻辑删除值
                    logic-not-delete-value: 1     # 逻辑不删除值
    2）在实体bean中指定逻辑删除字段
        @TableLogic(value = "1",delval="0")  // 是否显示[0-不显示，1显示]
	    private Integer showStatus;
* */
@MapperScan("com.bjc.gulimall.product.dao")
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
