package com.bjc.gulimall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * @描述：feign配置
 * @创建时间: 2021/3/20
 */
@Configuration
public class FeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                System.out.println("feign远程调用之前执行RequestInterceptor.apply");
                // 1. 使用RequestContextHolder拿到访问的请求对象
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                // 可能feign调用远程接口的时候没有请求，这里可能nullPointException，因此这里加上判断
                if(null != requestAttributes){
                    HttpServletRequest request = requestAttributes.getRequest();
                    if(null != request){
                        // 2. 同步请求头数据
                        // 注意：一般都是同步cookie  所以这里可以直接将cookie设置进去即可，不必要全部设置
                        Enumeration<String> headerNames = request.getHeaderNames();
                        if(null != headerNames && headerNames.hasMoreElements()){
                            while(headerNames.hasMoreElements()){
                                String headName = headerNames.nextElement();
                                // 跳过 content-length
                                /*
                                * 因为服务之间调用需要携带一些用户信息之类的 所以实现了Feign的RequestInterceptor拦截器复制请求头，
                                * 复制的时候是所有头都复制的,可能导致Content-length长度跟body不一致. 所以只需要判断如果是Content-length就跳过
                                * 否则可能会报错：feign.RetryableException: too many bytes written executing POST http://gulimall-ware/ware/wareinfo/lock/order
                                * */
                                if (headName.equals("content-length")){
                                    continue;
                                }
                                requestTemplate.header(headName,request.getHeader(headName));
                            }
                        }
                    }
                }
            }
        };
    }

}
