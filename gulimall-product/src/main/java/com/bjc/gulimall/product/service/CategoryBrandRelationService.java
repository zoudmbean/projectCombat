package com.bjc.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bjc.common.utils.PageUtils;
import com.bjc.common.utils.R;
import com.bjc.gulimall.product.entity.BrandEntity;
import com.bjc.gulimall.product.entity.CategoryBrandRelationEntity;
import com.bjc.gulimall.product.vo.BrandVo;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author zoudm
 * @email zoudmbean@163.com
 * @date 2020-12-08 22:27:02
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    R saveCategorybrandrelation(CategoryBrandRelationEntity categoryBrandRelationEntity);

    R queryPageByBrandId(Long brandId);

    List<BrandEntity> getBrandsByCatId(Long catId);

}

