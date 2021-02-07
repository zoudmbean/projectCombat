package com.bjc.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bjc.common.utils.PageUtils;
import com.bjc.gulimall.product.entity.CategoryEntity;
import com.bjc.gulimall.product.vo.Category2Vo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author zoudm
 * @email zoudmbean@163.com
 * @date 2020-12-08 22:27:02
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listTree();

    void removeMenuByIds(List<Long> asList);

    void updateCategory(CategoryEntity category);

    Long[] findCatelogPath(Long catelogId);

    List<CategoryEntity> getLevel1Categorys();

    Map<String, List<Category2Vo>> getCatagoryJson();
}

