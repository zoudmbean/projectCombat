package com.bjc.gulimall.product.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.bjc.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.bjc.gulimall.product.entity.AttrEntity;
import com.bjc.gulimall.product.entity.CategoryEntity;
import com.bjc.gulimall.product.service.AttrAttrgroupRelationService;
import com.bjc.gulimall.product.service.AttrService;
import com.bjc.gulimall.product.service.CategoryService;
import com.bjc.gulimall.product.vo.AttrGroupRelationVo;
import com.bjc.gulimall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService relationService;

    // 获取某个分类下所有的属性分组以及关联的所有属性
    // product/attrgroup/1111133/withattr
    @GetMapping("/{catlogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("catlogId") Long catlogId){
        // 1. 查出当前分类下的所有属性分组
        // 2. 查出每个属性分组的所有属性
        // AttrGroupWithAttrsVo
        List<AttrGroupWithAttrsVo> attrGroupWithAttrsVos = attrGroupService.getAttrGroupWithAttrsByCatlogId(catlogId);
        return R.ok().put("data",attrGroupWithAttrsVos);
    }

    // 新增关联关系
    // product/attrgroup/attr/relation
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVo> list){
        if(CollectionUtils.isEmpty(list)){
            return R.error("服务器未收到数据！");
        }
        relationService.saveBatch(list.stream()
                .map(item -> new AttrAttrgroupRelationEntity().setAttrGroupId(item.getAttrGroupId()).setAttrId(item.getAttrId())).collect(Collectors.toList()));//AttrAttrgroupRelationEntity
        return R.ok();
    }

    // 删除关联关系
    // product/attrgroup/attr/relation/delete
    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody List<AttrGroupRelationVo> vos){
        attrService.deleteRelation(vos);
        return R.ok();
    }


    // attrgroup/1/attr/relation
    @GetMapping("/{attrGroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrGroupId") Long attrGroupId){
        List<AttrEntity> attrList = attrService.getRelationAttr(attrGroupId);
        return R.ok().put("data", attrList);
    }

    // product/attrgroup/1/noattr/relation
    @GetMapping("/{attrGroupId}/noattr/relation")
    public R attrNoRelation(
            @RequestParam Map<String, Object> params,
            @PathVariable("attrGroupId") Long attrGroupId){
        PageUtils page = attrService.getNoRelationAttr(params,attrGroupId);
        return R.ok().put("page", page);
    }

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
