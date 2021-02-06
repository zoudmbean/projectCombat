package com.bjc.gulimall.search.controller;

import com.bjc.common.enums.BizCodeEnume;
import com.bjc.common.to.es.SkuEsModel;
import com.bjc.common.utils.R;
import com.bjc.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/search/save")
@Slf4j
public class ElasticSearchController {

    @Autowired
    private ProductSaveService productSaveService;

    // 上架商品
    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels){
        boolean b = false;
        try {
           b = productSaveService.productStatusUp(skuEsModels);
        } catch (IOException e) {
            log.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg(),e);
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }
        if(!b){
            return R.ok();
        } else {
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }

    }
}
