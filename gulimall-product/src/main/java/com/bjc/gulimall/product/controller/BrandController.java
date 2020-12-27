package com.bjc.gulimall.product.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

// import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.bjc.common.validation.UpdateGroup;
import com.bjc.common.validation.UpdateStatusGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bjc.gulimall.product.entity.BrandEntity;
import com.bjc.gulimall.product.service.BrandService;
import com.bjc.common.utils.PageUtils;
import com.bjc.common.utils.R;

import javax.validation.Valid;


/**
 * 品牌
 *
 * @author zoudm
 * @email zoudmbean@163.com
 * @date 2020-12-08 22:27:02
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("product:brand:list")  // shiro注解
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    // @RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:brand:save")
    public R save(@Valid @RequestBody BrandEntity brand, BindingResult result){
        // 这里的处理可以去掉了，因为写了全局异常处理类的
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

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:brand:update")
    public R update(@Validated(value={UpdateGroup.class}) @RequestBody BrandEntity brand){
		brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 修改状态
     */
    @RequestMapping("/update/status")
    // @RequiresPermissions("product:brand:update")
    public R updateStatus(@Validated(value={UpdateStatusGroup.class}) @RequestBody BrandEntity brand){
		brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
