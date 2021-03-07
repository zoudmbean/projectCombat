package com.bjc.gulimall.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * @描述：解决子域session共享问题——Cookie序列化器CookieSerializer
 * @创建时间: 2021/3/7
 */
@Configuration
public class SessionConfig {

    // https://docs.spring.io/spring-session/docs/2.2.1.RELEASE/reference/html5/#api-cookieserializer
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        // serializer.setCookieMaxAge(Integer.MAX_VALUE); // 设置cookie最大有效期
        serializer.setCookieName("GULI_JSESSION");  // 设置cookie名称
        // serializer.setCookiePath("/");           // 设置cookie路径
        // serializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$");
        serializer.setDomainName("gulimall.com");       // 指定domain名称  这里直接设置成子域名即可
        return serializer;
    }

    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer(){
        return new GenericJackson2JsonRedisSerializer();
    }
}
