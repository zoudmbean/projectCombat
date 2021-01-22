package com.bjc.gulimall.ware.feign;

import com.bjc.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/*
*   注意：feign接口也可以走网关，这时候，@FeignClient("gulimall-product")就变成@FeignClient("gulimall-gateway")了，
*           同时，请求路径地址开头需要带api，例如/api/product/skuinfo/info/{skuId}，这样就让feign接口也走网关了
* */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    @RequestMapping("/product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);

}
