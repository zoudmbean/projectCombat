package com.bjc.gulimallseckill.controller;

import com.bjc.common.utils.R;
import com.bjc.gulimallseckill.service.SeckillService;
import com.bjc.gulimallseckill.to.SeckillSkuRedisTo;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @描述：秒杀
 * @创建时间: 2021/4/13
 */
@Controller
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    /*
    * 返回当前时间可以参与的秒杀商品信息
    * */
    @GetMapping("/currentSeckillSkus")
    @ResponseBody
    public R getCurrentSeckillSkus(){
        List<SeckillSkuRedisTo> list = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(list);
    }

    @GetMapping("/sku/seckill/{skuId}")
    @ResponseBody
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId){
        SeckillSkuRedisTo to = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(to);
    }

    /** 秒杀功能 */
    @GetMapping("/kill")
    public String seckill(@RequestParam(value="killId",required = true) String killId,
                     @RequestParam(value="code",required = true) String code,
                     @RequestParam(value="num",required = true,defaultValue = "1") Integer num,
                          Model model
    ){

        // 1. 判断是否登录
        // 整合SpringSession，获取登录信息

        // 2. 秒杀逻辑
        String orderSN = seckillService.kill(killId,code,num);
        model.addAttribute("orderSN",orderSN);
        return "success";
    }
}
