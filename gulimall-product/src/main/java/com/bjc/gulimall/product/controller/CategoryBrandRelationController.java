package com.bjc.gulimall.product.controller;

import java.util.*;
import java.util.stream.Collectors;

// import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.bjc.gulimall.product.entity.BrandEntity;
import com.bjc.gulimall.product.vo.BrandVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.bjc.gulimall.product.entity.CategoryBrandRelationEntity;
import com.bjc.gulimall.product.service.CategoryBrandRelationService;
import com.bjc.common.utils.PageUtils;
import com.bjc.common.utils.R;



/**
 * 品牌分类关联
 *
 * @author zoudm
 * @email zoudmbean@163.com
 * @date 2020-12-08 22:27:02
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("product:categorybrandrelation:list")  // shiro注解
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }

    /*
    * 根据品牌ID查询其下的分类关系数据
    * */
    @RequestMapping("/list/{brandId}")
    // @RequiresPermissions("product:categorybrandrelation:list")  // shiro注解
    public R listByBrandId(@PathVariable("brandId") Long brandId){
        return categoryBrandRelationService.queryPageByBrandId(brandId);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("product:categorybrandrelation:info")
    public R info(@PathVariable("id") Long id){
		CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:categorybrandrelation:save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.save(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:categorybrandrelation:update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:categorybrandrelation:delete")
    public R delete(@RequestBody Long[] ids){
		categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /*
    * /product/categorybrandrelation/brands/list
    * */
    @GetMapping("/brands/list")
    public R relationBrandList(@RequestParam(value = "catId",required = true) Long catId){
        List<BrandEntity> list = Optional.ofNullable(categoryBrandRelationService.getBrandsByCatId(catId)).orElseGet(ArrayList::new);
        List<BrandVo> brandVos = list.stream().map(item -> new BrandVo().setBrandId(item.getBrandId()).setBrandName(item.getName())).collect(Collectors.toList());
        return R.ok().put("data",brandVos);
    }

}
