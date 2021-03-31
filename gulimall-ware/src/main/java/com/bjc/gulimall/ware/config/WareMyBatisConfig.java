package com.bjc.gulimall.ware.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.optimize.JsqlParserCountOptimize;
import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

@EnableTransactionManagement// 开启事务管理器
@Configuration
@MapperScan("com.bjc.gulimall.ware.dao")
public class WareMyBatisConfig {
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        // 设置请求的页面大于最大页后操作， true调回到首页，false 继续请求  默认false
        paginationInterceptor.setOverflow(true);
        // 设置最大单页限制数量，默认 500 条，-1 不受限制
        paginationInterceptor.setLimit(5000);
        // 开启 count 的 join 优化,只针对部分 left join
        paginationInterceptor.setCountSqlParser(new JsqlParserCountOptimize(true));
        return paginationInterceptor;
    }

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
            //dataSource.setPoolName(dataSourceProperties.getName());
        }
        // 将数据源用seata代理包装，返回自定义的代理数据源
        DataSourceProxy dataSourceProxy = new DataSourceProxy(dataSource);
        return dataSourceProxy;
    }
}
