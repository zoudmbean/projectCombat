谷粒商城项目

分布式技术方案：
1）注册中心：SpringCloud Alibaba-Nacos
    使用步骤：(https://github.com/alibaba/spring-cloud-alibaba/blob/master/spring-cloud-alibaba-examples/nacos-example/nacos-discovery-example/readme.md)
        1.1 引入nacos依赖（见common工程）
        1.2 下载Nacos Server（这里使用1.1.3 Windows版本），并启动nacos Server
        1.3 在每个微服务的配置文件中配置nacos  Server的地址（ spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848 ）
        1.4 使用@EnableDiscoveryClient注解开启服务注册与发现功能
    注意：需要给每个需要注册到注册中心的服务定义微服务名称
2）声明式Http客户端（调用远程服务）：SpringCloud OpenFeign
    2.1 简介：
        1）Feign是一个声明式的HTTP客户端，它的目的就是让远程调用更加简单。Feign提供了HTTP请求的模板，通过编写简单的接口和插入注解，
            就可以定义好HTTP请求的参数、格式、地址等信息。
        2）Feign整合了Ribbon（负载均衡）和Hystrix(服务熔断)，可以让我们不再需要显示地使用这两个组件。
        3）SpringCloudFeign在NetflixFeign的基础上扩展了对SpringMVC注解的支持，在其实现下，我们只需要创建一个接口并用注解的方式来配置它，
            即可完成对服务提供方的接口绑定。简化了SpringCloudRibbon自行封装服务调用客户端的开发量。
    2.2 使用步骤：
        例如：会员服务需要调用优惠券服务，那么只需要给会员服务引入openFeign的依赖，会员服务就具有了远程调用其他服务的能力了。
        1）引入openFeign依赖
        `
            <dependency>
                 <groupId>org.springframework.cloud</groupId>
                 <artifactId>spring-cloud-starter-openfeign</artifactId>
             </dependency>
         `
         2) 编写一个feign接口，告诉cloud这个接口需要调用远程服务。
            声明接口的每一个方法都是调用哪个远程服务的哪个请求
            2.1 接口需要添加注解@FeignClient("微服务名称A")  微服务名称A表示需要调用的服务
            2.2 将服务A的Controller的方法签名拷贝到该接口即可
            `
            @FeignClient("gulimall-coupon")
            public interface CouponFeignService {
                @GetMapping("/member/list")
                public R memberCoupons();
            }
            `
        3）在会员服务的启动类上添加注解@EnableFeignClients开启远程调用功能并指定basePackages为feign接口的全包名
            例如：@EnableFeignClients(basePackages = "com.bjc.gulimall.member.feign")
            这样只要服务一起动，就会自动扫描feign包下的所有标有@FeignClient注解的接口，每一个接口都有说明调用哪个服务
        测试：
            1）远程服务参看gulimall-coupon工程的CouponController类的memberCoupons方法
            2）feign接口参看gulimall-member工程的feign包下的CouponFeignService类下的memberCoupons方法
            3）调用参看MemberController类的test()方法
3）配置中心：SpringCloud Alibaba-Nacos
4）负载均衡：SpringCloud Ribbon
5）服务容错（限流、降级、熔断）：SpringCloud Alibaba Sentinel
6）API网关（Webflux编程模式）：SpringCloud Gateway
7）调用链监控：SpringCloud Sleuth
8）分布式事务解决方案：SpringCloud Alibaba Seata(原Fesca)
