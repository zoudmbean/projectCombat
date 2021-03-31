package com.bjc.gulimall.ware.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.alibaba.fastjson.JSONObject;
import com.bjc.common.enums.BizCodeEnume;
import com.bjc.gulimall.ware.vo.LockStockResult;
import com.bjc.gulimall.ware.vo.WareSkuLockVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.bjc.gulimall.ware.entity.WareInfoEntity;
import com.bjc.gulimall.ware.service.WareInfoService;
import com.bjc.common.utils.PageUtils;
import com.bjc.common.utils.R;



/**
 * 仓库信息
 *
 * @author zoudm
 * @email zoudmbean@163.com
 * @date 2020-12-08 23:28:45
 */
@RestController
@RequestMapping("ware/wareinfo")
@Slf4j
public class WareInfoController {
    @Autowired
    private WareInfoService wareInfoService;

    @PostMapping("/lock/order")
    public R orderlock(@RequestBody WareSkuLockVo vo){
        try {
            Boolean isLocked = wareInfoService.orderlock(vo);
            return R.ok();
        } catch (Exception e) {
            log.error("库存锁定失败",e);
            return R.error(BizCodeEnume.NO_STOCK_EXCEPTION.getCode(),BizCodeEnume.NO_STOCK_EXCEPTION.getMsg());
        }
    }

    @GetMapping("/getFace/{id}")
    public R getFace(@PathVariable("id") Long id){
        JSONObject fare = wareInfoService.getFace(id);
        return R.ok().setData(fare);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("ware:wareinfo:list")  // shiro注解
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareInfoService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("ware:wareinfo:info")
    public R info(@PathVariable("id") Long id){
		WareInfoEntity wareInfo = wareInfoService.getById(id);

        return R.ok().put("wareInfo", wareInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("ware:wareinfo:save")
    public R save(@RequestBody WareInfoEntity wareInfo){
		wareInfoService.save(wareInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("ware:wareinfo:update")
    public R update(@RequestBody WareInfoEntity wareInfo){
		wareInfoService.updateById(wareInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("ware:wareinfo:delete")
    public R delete(@RequestBody Long[] ids){
		wareInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
