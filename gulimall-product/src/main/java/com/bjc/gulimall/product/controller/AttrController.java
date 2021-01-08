package com.bjc.gulimall.product.controller;

import java.util.Arrays;
import java.util.Map;

// import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.bjc.gulimall.product.vo.AttrGroupRelationVo;
import com.bjc.gulimall.product.vo.AttrResponseVo;
import com.bjc.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.bjc.gulimall.product.entity.AttrEntity;
import com.bjc.gulimall.product.service.AttrService;
import com.bjc.common.utils.PageUtils;
import com.bjc.common.utils.R;



/**
 * 商品属性
 *
 * @author zoudm
 * @email zoudmbean@163.com
 * @date 2020-12-08 22:27:02
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("product:attr:list")  // shiro注解
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    // @RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId){
		// AttrEntity attr = attrService.getById(attrId);
        // AttrResponseVo
        AttrResponseVo responseVo = attrService.getAttrInfo(attrId);
        return R.ok().put("attr", responseVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrVo attr){
		attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrVo attr){
		// attrService.updateById(attr);
		attrService.updateAttr(attr);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@PathVariable("attrType") String type,@PathVariable Long catelogId,@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryBaseAttrPage(params,catelogId,type);
        return R.ok().put("page",page);
    }

}
