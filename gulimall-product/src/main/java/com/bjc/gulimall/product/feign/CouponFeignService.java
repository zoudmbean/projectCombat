package com.bjc.gulimall.product.feign;

import com.bjc.common.to.SkuReductionTo;
import com.bjc.common.to.SpuBoundTo;
import com.bjc.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/*
* 优惠券远程调用服务
* */
@FeignClient("gulimall-coupon")        // 1. 声明需要调用哪个远程服务
public interface CouponFeignService {

    /*
    *   1）CouponFeignService.saveSpuBounds(spuBoundTo),CouponFeignService对象调用saveSpuBounds方法
    *       1.1 @RequestBody 将参数对象转成json
    *       1.2 cloud在注册中心中，找到gulimall-coupon服务，给服务的/coupon/spubounds/save发送请求，并将上一步转吃json对象，放在请求体位置，然后发送请求
    *       1.3 对方服务（gulimall-coupon）接收到请求，请求体中有json数据，对方服务的对应请求方法的参数@RequestBody SpuBoundTo spuBoundTo，它将请求体中的json转成SpuBoundTo
    *           所以：只要两边传输对象属性有一一对应就可以封装，不必要两边是同一个类对象，因此，只要json数据模型是兼容的，双方服务无需使用同一个To
    *
    * */
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    @PostMapping("/coupon/skufullreduction/saveInfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
