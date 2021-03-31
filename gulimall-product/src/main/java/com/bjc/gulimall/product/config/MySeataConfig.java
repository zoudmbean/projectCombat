package com.bjc.gulimall.product.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

/**
 * @描述：seata的配置文件
 * @创建时间: 2021/3/27
 */
@Configuration
public class MySeataConfig {

    /*
    * 因为DataSourceAutoConfiguration的配置依赖DataSourceProperties，该配置类定义了数据源的所有信息
    * */
    @Autowired
    DataSourceProperties dataSourceProperties;

    @Bean
    public DataSource dataSource(DataSourceProperties dataSourceProperties){
        /*
         * 打开boot的默认数据源Hikari，可以看到  HikariDataSource dataSource = createDataSource(properties, HikariDataSource.class);
         * 进入 createDataSource 方法，可以看到boot就是使用的dataSourceProperties来创建的数据源，
         * properties.initializeDataSourceBuilder().type(type).build();
         * */
        // 也使用boot默认的数据源
        //Class<? extends DataSource> dataSourceType = HikariDataSource.class;
        Class<DruidDataSource> dataSourceType = DruidDataSource.class;
        DruidDataSource dataSource = (DruidDataSource) dataSourceProperties.initializeDataSourceBuilder().type(dataSourceType).build();
        //HikariDataSource dataSource = (HikariDataSource) dataSourceProperties.initializeDataSourceBuilder().type(dataSourceType).build();
        if (StringUtils.hasText(dataSourceProperties.getName())) {
            // dataSource.setPoolName(dataSourceProperties.getName());
        }
        // 将数据源用seata代理包装，返回自定义的代理数据源
        DataSourceProxy dataSourceProxy = new DataSourceProxy(dataSource);
        return dataSourceProxy;
    }

}
