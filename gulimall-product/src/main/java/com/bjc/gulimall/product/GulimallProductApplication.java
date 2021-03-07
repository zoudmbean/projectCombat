package com.bjc.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

/*
* 整合myBatis-plus
*   1） 导入依赖（放在common工程了）
*   2） 配置（参照官网配置）
*       2.1 数据源
*       2.2 mybatis-plus相关配置
*           2.2.1 首先在启动类加上注解MapperScan
*           2.2.2 配置sql映射文件位置
*
* myBatis-plus的逻辑删除：
*   1）在application.yml中配置如下（如果表中的逻辑删除字段的值就是与下面的值一致，那么可以不配置，因为mybatis-plus默认就加了以下配置）
*       mybatis-plus:
            global-config:
                db-config:
                    id-type: auto # 配置主键自增
                    logic-delete-value: 0         # 逻辑删除值
                    logic-not-delete-value: 1     # 逻辑不删除值
    2）在实体bean中指定逻辑删除字段
        @TableLogic(value = "1",delval="0")  // 是否显示[0-不显示，1显示]
	    private Integer showStatus;

 JSR303数据校验：
    1）给Bean添加校验注解(在包 javax.validation.constraints 下)，例如：@NotNull
    2）在Controller层，给需要校验的地方加上校验注解@Valid，开启校验功能
    例如：
    ·
        @RequestMapping("/save")
        // @RequiresPermissions("product:brand:save")
        public R save(@Valid @RequestBody BrandEntity brand){
            brandService.save(brand);
            return R.ok();
        }
    ·
        加上校验注解之后，在提交请求的时候，就会对提交的数据进行数据校验
    3）如果要修改校验不通过错误信息提示，可以在校验注解上加上message注解，例如：@NotBlank(message = "品牌名必须不能为空")
    4）如果要获取校验的结果，可以使用BindingResult，BindingResult封装了校验结果，定义紧跟在@Valid修饰的实体bean的后面。例如：
        ·
            @RequestMapping("/save")
            public R save(@Valid @RequestBody BrandEntity brand, BindingResult result){
                if(result.hasErrors()){
                    Map<String,String> map = new HashMap<>();
                    result.getFieldErrors().stream().forEach(item -> {
                        map.put(item.getField(),item.getDefaultMessage());  // 如果有自定义消息，那么getDefaultMessage获取的是自定义的，否则就是默认的消息
                    });
                    return R.error(400,"提交的数据不合法").put("data",map);
                }
                brandService.save(brand);
                return R.ok();
            }
        ·
 统一异常处理
    对于数据校验，几乎每个controller都需要做校验，如果每个controller都写一遍校验异常处理，那太麻烦了。
    我们可以使用统一异常处理注解@RestControllerAdvice(basePackages="com.bjc.*.controller")
    使用步骤：
       1）新建一个异常处理器类
       2）在类上加上注解@RestControllerAdvice，并制定basePackages
       3）定义一个异常处理方法，并在方法上加上注解@ExceptionHandler并制定需要处理的异常类型，例如：MethodArgumentNotValidException
       例如：
       ·
            @RestControllerAdvice(basePackages="com.bjc.*.controller")
            @Slf4j
            public class GulimallExceptionControllerAdvice {

                //处理JSR303数据校验抛出的异常
                @ExceptionHandler(value = MethodArgumentNotValidException.class)
                public R handleValidException(MethodArgumentNotValidException e){
                    // 打印日志
                    log.error("提交的数据不合法",e);
                    BindingResult bindingResult = e.getBindingResult();
                    Map<String,String> errMap = new HashMap<>();
                    bindingResult.getFieldErrors().stream().forEach(item -> {
                        errMap.put(item.getField(),item.getDefaultMessage());
                    });
                    return R.error(400,"提交的数据不合法").put("data",errMap);
                }

                @ExceptionHandler(value = Throwable.class)
                public R handleException(Throwable e){
                    log.error("出现异常：",e);
                    return R.error(500,"服务器异常！");
                }

            }
       ·
  Feign的使用步骤：
  *     1）在需要远程调用的服务的主启动类上添加注解@EnableFeignClients，开启feign功能
  *     2）定义feign接口，在接口上标注注解@FeignClient并指定要调用的服务名。例如；@FeignClient("gulimall-coupon")
  *     3）在需要调用的地方注入feign接口。
  *     例如；
  *     ·
  *         @Autowired
            private CouponFeignService couponFeignService;
  *     ·
  *     4）在feign接口中编写调用方法，并指定访问路径
  *     例如：
  *         ·
  *             @PostMapping("/coupon/spubounds/save")
                R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);
  *         ·
  * 模板引擎
  *     1）thymeleaf-starter引入
  *     2）关闭thymeleaf缓存（开发中用到，方便调试）
  *     3）静态资源放在static文件夹下，就可以按照路径直接访问
  *     4）页面放在templates目录下，就可以直接访问，默认的页面是index.html
  *     5）页面修改不重启服务器实时更新
  *         5.1 引入依赖
  *         `
  *             <dependency>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-devtools</artifactId>
                    <!-- 可选的不能忘记了 -->
                    <optional>true</optional>
                </dependency>
  *         `
  *         5.2 修改完页面，使用ctrl+f9（ctrl+shift+f9） 重新编译页面
  *
  * 整合redis
  *     1）pom坐标引入
  *     2）简单配置redis配置信息
  *     3）使用boot自动配置好的StringRedisTemplate来操作redis
  *
  * 整合redisson作为分布式锁功能框架
  *     1）引入依赖
  *     2）
* */
@MapperScan("com.bjc.gulimall.product.dao")
@EnableFeignClients(basePackages = "com.bjc.gulimall.product.feign")        // 商品服务开启远程调用服务功能并指定feign接口位置（注意：即使不指定也可以扫描到有@FeignClient注解的远程服务接口）
@EnableDiscoveryClient
@SpringBootApplication
@EnableRedisHttpSession
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
