package com.bjc.gulimall.search.feign;

import com.bjc.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/*
* 商品服务远程调用
* */
@FeignClient("gulimall-product")
public interface ProductServiceFeign {

    @GetMapping("/product/attr/info/{attrId}")
    // @RequiresPermissions("product:attr:info")
    public R attrInfo(@PathVariable("attrId") Long attrId);

    @GetMapping("/product/brand/infos")
    public R brandsInfos(@RequestParam("brandIds") List<Long> brandIds);
}
