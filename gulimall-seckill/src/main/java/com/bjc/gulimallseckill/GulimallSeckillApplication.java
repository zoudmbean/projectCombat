package com.bjc.gulimallseckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/*
* 1. 整合Sentinel步骤：
*   1）引入Sentinel依赖
*   2）下载对应版本的sentinel的控制台
*   3）配置控制台地址等信息
*   4）在控制台可以调整所有的参数（流控规则等）
*       注意：默认所有的流控设置保存在内存中，重启失效
* 2. sentinel显示监控信息：每一个微服务都导入actuator监控模块（一般是web项目的标配），并配置Endpoint暴露信息
* 3.自定义流控返回：修改控制之后的返回显示值 默认是 Blocked by Sentinel (flow limiting)
*
* 4. 使用Sentinel保护Feign远程调用（熔断）
*   4.1 调用方的熔断保护：
*       4.1.1 配置文件打开Sentinel对Feign的支持：feign.sentinel.enabled=true（给调用方配置）
*       4.1.2 引入Feign依赖
*       4.1.3 定义对应的Feign接口的熔断类
*       4.1.4 在Feign接口上指定FallBack熔断类
*   4.2 调用方在控制台手动指定远程服务的降级策略
*       远程服务一旦被降级处理，就会触发熔断回调函数。
*   4.3 被调用方（远程服务）：
*       在超大并发的时候，必须牺牲一些远程服务，在服务提供方（远程服务）指定服务降级策略，提供方在运行，但是不运行自己的业务逻辑，返回的是默认的降级数据
* 5. 自定义受保护的资源
*     方式一：使用异常模式
*       5.1 将需要受保护的代码用try-catch包裹起来，并捕获BlockException
*       5.2 使用SphU.entry("seckillSkus")指定资源名称
*           try(Entry entry = SphU.entry("seckillSkus")){
*                  / 业务逻辑
*           }catch(BlockException e){
*               log.error("资源被限流：" + e);
*           }
*     方式二：基于注解（@SentinelResource）
*       5.1 @SentinelResource(value="getCurrentSeckillSkusResource",blockHandler = "getCurrentSeckillSkusFallBack")
*       注意：除了使用blockHandler  还可以使用fallBack，详见官方demo
*       如果是方式一还是方式二，一定要配置被限流以后的默认返回，但是针对UrlBlockhandler可以设置统一的返回，例如：SentinelConfig定义的统一的返回
* 6. 整合网关限流步骤
*   6.1 引入依赖
*   `
*       <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-alibaba-sentinel-gateway</artifactId>
            <version>2.1.0.RELEASE</version>
        </dependency>
*   `
*   6.2 控制台版本1.7及以上
*   6.3 在控制台有各种流控操作
* 7. 网关自定义返回类型，见SentinelGatewayConfig
*
* */

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
@EnableFeignClients
// @EnableRabbit 如果不需要监听rabbitMQ，这个注解不需要加，直接用template发送消息即可
public class GulimallSeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallSeckillApplication.class, args);
    }

}
