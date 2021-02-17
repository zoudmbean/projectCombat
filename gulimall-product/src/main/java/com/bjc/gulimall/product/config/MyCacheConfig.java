package com.bjc.gulimall.product.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/*
* 缓存相关的配置类
* 将所有与缓存相关的配置都写在该类下，包括开启缓存的注解@EnableCaching也从启动类移动到这里
 * */
@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)  // 开启属性配置绑定功能
public class MyCacheConfig {

    // 方法中的参数都会去容器中进行查找确定，所以我们可以直接将CacheProperties指定在配置方法上，就可以使用CacheProperties了
    @Bean
    RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties){
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        // 设置key的序列化机制  使用默认的
        // 接收一个RedisSerializer类型的序列化器
        config = config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
        // 设置值的序列化器  使用fastJSON序列化器
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericFastJsonRedisSerializer()));
        /*
            设置配置文件中的设置
            将RedisCacheConfiguration中的代码直接拿过来
            注意：
                源代码中  Redis redisProperties = this.cacheProperties.getRedis();
                进入getRedis，可以看到CacheProperties跟配置文件ConfigurationProperties是绑定在一起的，
                    @ConfigurationProperties(prefix = "spring.cache")
                发现该配置类对象并未加入到spring容器中，所以，我们需要将该配置类(CacheProperties)加入到容器中

        */
        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive());
        }

        if (redisProperties.getKeyPrefix() != null) {
            config = config.prefixKeysWith(redisProperties.getKeyPrefix());
        }

        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }

        if (!redisProperties.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
        }

        return config;
    }
}
