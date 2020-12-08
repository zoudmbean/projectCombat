package com.bjc.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bjc.common.utils.PageUtils;
import com.bjc.gulimall.product.entity.SkuImagesEntity;

import java.util.Map;

/**
 * sku图片
 *
 * @author zoudm
 * @email zoudmbean@163.com
 * @date 2020-12-08 22:27:02
 */
public interface SkuImagesService extends IService<SkuImagesEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

