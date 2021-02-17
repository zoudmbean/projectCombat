package com.bjc.gulimall.product;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallProductRedisTest {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Test
    public void test2(){
        System.out.println(redissonClient);
    }

    @Test
    public void test1(){
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();

        // 保存
        valueOperations.set("hello","word_"+ UUID.randomUUID().toString());

        // 查询
        String value = valueOperations.get("hello");
        System.out.println(value);
    }
}
