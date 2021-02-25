package com.bjc.gulimall.product.web;

import com.bjc.gulimall.product.service.SkuInfoService;
import com.bjc.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ExecutionException;

@Controller
public class ItemController {

    @Autowired
    private SkuInfoService skuInfoService;

    /*
    * 展示当前Sku详情信息
    * */
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {
        System.out.printf("开始查询%d详情",skuId);
        SkuItemVo vo = skuInfoService.item(skuId);
        model.addAttribute("item",vo);
        return "item";
    }

}
