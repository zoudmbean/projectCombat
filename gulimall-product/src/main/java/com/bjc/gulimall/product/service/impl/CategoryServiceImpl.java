package com.bjc.gulimall.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bjc.common.utils.PageUtils;
import com.bjc.common.utils.Query;

import com.bjc.gulimall.product.dao.CategoryDao;
import com.bjc.gulimall.product.entity.CategoryEntity;
import com.bjc.gulimall.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listTree() {

        // 1. 获取所有的分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        // 2. 获取所有的一级分类
        List<CategoryEntity> level1 = entities.parallelStream()
                .filter(c -> c.getParentCid() == 0)
                .map(menu -> {
                    menu.setChildren(getChildren(menu,entities));
                    return menu;
                })
                .sorted((m1,m2) -> Optional.ofNullable(m1.getSort()).orElse(0)-Optional.ofNullable(m2.getSort()).orElse(0))  // sort的值可能为null
                .collect(Collectors.toList());

        return level1;
    }

    /*
    * 逻辑删除
    * */
    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO  一些业务逻辑

        // 逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    /*
    * 根据当前菜单获取其子菜单
    * */
    private List<CategoryEntity> getChildren(CategoryEntity categoryEntity, List<CategoryEntity> entities) {
        List<CategoryEntity> children = entities.parallelStream()
                .filter(c -> c.getParentCid() == categoryEntity.getCatId())
                .map(menu -> {
                    menu.setChildren(getChildren(menu,entities));
                    return menu;
                })
                .sorted((m1,m2) -> Optional.ofNullable(m1.getSort()).orElse(0)-Optional.ofNullable(m2.getSort()).orElse(0))  // sort的值可能为null
                .collect(Collectors.toList());
        return children;
    }

}
