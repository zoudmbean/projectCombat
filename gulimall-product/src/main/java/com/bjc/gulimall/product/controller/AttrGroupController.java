package com.bjc.gulimall.product.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.bjc.gulimall.product.entity.CategoryEntity;
import com.bjc.gulimall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bjc.gulimall.product.entity.AttrGroupEntity;
import com.bjc.gulimall.product.service.AttrGroupService;
import com.bjc.common.utils.PageUtils;
import com.bjc.common.utils.R;



/**
 * 属性分组
 *
 * @author zoudm
 * @email zoudmbean@163.com
 * @date 2020-12-08 22:27:02
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 列表
     */
    @RequestMapping("/list/{catId}")
    // @RequiresPermissions("product:attrgroup:list")  // shiro注解
    public R list(@RequestParam Map<String, Object> params,@PathVariable("catId") Long catId){
        // PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPageByCatId(params,catId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    // @RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

		// 根据当前三级分类ID，查询分类路径
		List<Long> paths = new ArrayList<>();
        findParents(attrGroup.getCatelogId(),paths);
        attrGroup.setCatePath(paths.toArray(new Long[paths.size()]));

        return R.ok().put("attrGroup", attrGroup);
    }

    // 根据分类ID查询父分类ID
    private List<Long> findParents(Long catelogId,List<Long> paths){
        paths.add(0,catelogId);
        CategoryEntity byId = categoryService.getById(catelogId);
        if(null != byId.getParentCid() && byId.getParentCid() != 0){
            findParents(byId.getParentCid(),paths);
        }
        return paths;
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
