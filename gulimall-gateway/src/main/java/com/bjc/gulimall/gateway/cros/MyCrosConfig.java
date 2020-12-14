package com.bjc.gulimall.gateway.cros;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;

@Configuration
public class MyCrosConfig {

    @Bean
    public CorsWebFilter getCorsWebFilter(){

        // UrlBasedCorsConfigurationSource是CorsConfigurationSource的实现类
        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();


        CorsConfiguration corsConfiguration = new CorsConfiguration();

        corsConfiguration.addAllowedHeader("*");            // 允许所有的头跨域
        corsConfiguration.addAllowedMethod("*");            // 允许所有的请求方式跨域
        corsConfiguration.addAllowedOrigin("*");            // 允许所有的请求来源跨域
        corsConfiguration.setAllowCredentials(true);        // 允许携带cookie跨域  如果为false，跨域请求可能会丢失cookie信息

        // 注册跨域配置  参数1 路径 ； 参数2 跨域信息配置类
        // /** 表示任意路径
        configSource.registerCorsConfiguration("/**",corsConfiguration);

        return new CorsWebFilter(configSource);
    }

}
