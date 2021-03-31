package com.bjc.gulimall.thirdparty;

import com.alibaba.alicloud.context.oss.OssContextAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient              // 开启服务注册与发现
@SpringBootApplication(exclude = OssContextAutoConfiguration.class)  // 不需要OSS了，，排除掉
public class GulimallThirdPartyApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallThirdPartyApplication.class, args);
    }

}
