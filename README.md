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
    3.1 操作步骤：
        3.1.1 引入nacos config启动器依赖
        `
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
            </dependency>
        `
        3.1.2 在应用的src/main/resources/bootstrap.properties配置文件，配置Nacos Config元数据
            `
                ## 配置当前应用名称
                spring.application.name=gulimall-coupon
                ## 指定Nacos配置中心的地址（就是Nacos Server的地址）
                spring.cloud.nacos.config.server-addr=127.0.0.1:8848
            `
            注意：bootstrap.properties文件会优先于application配置文件加载
        3.1.3 在Nacos配置中心中新建数据集（Data Id）并填写配置内容
            默认规则：应用名.properties  
            例如：gulimall-coupon.properties
        3.1.4 在使用配置的类上加上注解 @RefreshScope (注：该注解的作用是动态获取并刷新配置，也就是说只要配置中心的配置内容作了改变，页面都能实时体现)   
            例如：com.bjc.gulimall.coupon.controller.CouponController上的注解@RefreshScope
        注意：如果配置中心和应用的配置中都配置了相同的项，那么优先使用配置中心的配置内容
    3.2 命名空间：配置（环境）隔离
        3.2.1 概念：用户进行租户粒度的配置隔离，不同的命名空间下，可以存在相同的group或Data Id的配置。NameSpace的常用场景之一是不同环境的配置的分区隔离。
              例如：开发测试生产环境的资源（如配置、服务）隔离等。
        3.2.2 默认空间：public(保留空间)
            如果不指定命名空间，默认新增的所有配置都位于该空间public下
        3.2.3 指定命名空间：只需要在bootstrap.properties配置文件中添加如下配置即可
        `
            ## 指定命名空间
            spring.cloud.nacos.config.namespace=ea5cda76-855c-4e4c-bfae-dfe74167e869
        `
    3.3 配置集：所有配置的集合
        一组相关或者不相关的配置项的集合称为配置集。在系统中，一个配置文件通常就是一个配置集，包含了系统各个方面的配置。
        例如；一个配置集可能包含了数据源、线程池、日志级别等配置项
    3.4 配置集ID：类似文件名
        Nacos中的某个配置集的ID。配置集ID是组织划分配置的维度之一。Data Id通常用于组织划分系统的配置集。一个系统或者应用可以包含多个配置集，每个
        配置集都可以被一个有意义的名称标识。Data Id通常采用类java包（如 com.taobao.tc.refund.log.level）的命名规则保证全局唯一性。此命名规则非强制
    3.5 配置分组：默认所有的配置都属于组DEFAULT_GROUP
        Nacos中的一组配置集，是组织配置的维度之一，通过一个有意义的字符串（如Buy或者Trade）对配置进行分组，从而区分Data Id相同的配置集。当在Nacos上传统将一个
        配置时，如果未填写配置分组的名称，则配置分组的名称默认采用 DEFAULT_GROUP。配置分组的常见场景，不同的应用或组件使用了相同的配置类型，如：database_url
        配置和MQ_topic配置。
        `
            ## 指定 配置分组（不指定默认是DEFAULT_GROUP）
            spring.cloud.nacos.config.group=gulimall
        `
4）API网关（Webflux编程模式）：SpringCloud Gateway
    4.1 简介
        网关作为流量的入口，常用功能包括路由转发、权限校验、限流控制等。而SpringCloud gateway作为SpringCloud官方推出的第二代网关框架，取代了Zuul网关
        网关提供API全托管服务，丰富的API管理功能，辅助企业管理大规模的API，以降低管理成本和安全风险，包括协议适配、协议转发、安全策略、防刷、流量、监控日志等功能。
        spring cloud gateway旨在提供一种简单而有效的方式来对API进行路由，并为他们提供切面。例如：安全性，监控/指标和弹性等。
    4.2 执行流程
        当请求到达网关，网关先利用断言（predicate）判定本次请求是否符合某个路由（Route）规则，
        如果符合了就按照该路由规则路由到指定的地方，去该指定地方需要经过一系列的过滤器（filter）进行过滤
    4.3 使用步骤：
        4.3.1 新建网关工程引入相关依赖（见gateway工程）
        4.3.2 在启动类上开启服务注册发现@EnableDiscoveryClient
        4.3.3 配置配置中心（Nacos）服务的地址
        4.3.4 排除数据源相关配置，简单的做法是在@SpringBootApplication注解上加上exclude = {DataSourceAutoConfiguration.class}
        4.3.5 配置路由规则(配置可以参看gateway工程的application.yml配置文件)
5）负载均衡：SpringCloud Ribbon
6）服务容错（限流、降级、熔断）：SpringCloud Alibaba Sentinel
7）调用链监控：SpringCloud Sleuth
8）分布式事务解决方案：SpringCloud Alibaba Seata(原Fesca)


PubSub的使用：
    1）安装
    2）全局使用
        2.1 在main.js中引入并注册
        `
            import PubSub from 'pubsub-js'
            Vue.prototype.PubSub = PubSub   //组件发布订阅消息
        `
        2.2 在组件中通过this.PubSub即可订阅和发布消息了。
        例如：
            1）在category-cascader.vue中发布消息
            `
                this.PubSub.publish("catPath",v);
            `
            2）在brand-select中订阅消息
            `
                mounted() {
                //监听三级分类消息的变化
                this.subscribe = this.PubSub.subscribe("catPath", (msg, val) => {
                  this.catId = val[val.length - 1];
                  this.getCatBrands();
                });
              }
            `
    3）局部使用
        3.1 在需要使用PubSub的组件中引入PubSub
        `
            import PubSub from 'pubsub-js'
        `
        3.2 然后可以根据全局方式订阅和发布消息了
