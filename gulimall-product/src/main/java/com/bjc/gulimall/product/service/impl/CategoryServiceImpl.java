package com.bjc.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.bjc.gulimall.product.dao.CategoryBrandRelationDao;
import com.bjc.gulimall.product.entity.CategoryBrandRelationEntity;
import com.bjc.gulimall.product.vo.Category2Vo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bjc.common.utils.PageUtils;
import com.bjc.common.utils.Query;

import com.bjc.gulimall.product.dao.CategoryDao;
import com.bjc.gulimall.product.entity.CategoryEntity;
import com.bjc.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationDao categoryBrandRelationDao;

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
        List<CategoryEntity> level1 = entities.stream()
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

    @Override
    @Transactional
    public void updateCategory(CategoryEntity category) {
        // 1. 修改分类表
        baseMapper.updateById(category);
        // 2. 修改品牌分类关系表
        if(StringUtils.isNotEmpty(category.getName())){
            CategoryBrandRelationEntity entity = new CategoryBrandRelationEntity();
            entity.setCatelogId(category.getCatId());
            entity.setCatelogName(category.getName());
            QueryWrapper<CategoryBrandRelationEntity> wraper = new QueryWrapper();
            wraper.eq("catelog_id",category.getCatId());
            categoryBrandRelationDao.update(entity,wraper);
        }

        // 3. TODO 修改其他分类关系
    }

    /*
    * 根据当前菜单获取其子菜单
    * */
    private List<CategoryEntity> getChildren(CategoryEntity categoryEntity, List<CategoryEntity> entities) {
        List<CategoryEntity> children = entities.stream()
                .filter(c -> c.getParentCid().longValue() == categoryEntity.getCatId().longValue())  // 这里转成long型来比较值相等，否则值过大的时候就恒不等了
                .map(menu -> {
                    menu.setChildren(getChildren(menu,entities));
                    return menu;
                })
                .sorted((m1,m2) -> Optional.ofNullable(m1.getSort()).orElse(0)-Optional.ofNullable(m2.getSort()).orElse(0))  // sort的值可能为null
                .collect(Collectors.toList());
        return children;
    }

    //[2,25,225]
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);

        Collections.reverse(parentPath);


        return parentPath.toArray(new Long[parentPath.size()]);
    }

    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        List<CategoryEntity> entities = this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return entities;
    }

    @Override
    public Map<String, List<Category2Vo>> getCatagoryJson() {
        // 1. 查出所有1级分类
        List<CategoryEntity> levelaCategorys = getLevel1Categorys();
        // 封装数据
        Map<String, List<Category2Vo>> listMap = levelaCategorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 1. 每一个的一级分类   查到这个一级分类的二级分类
            List<CategoryEntity> entities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));

            // 2. 将查询到的list转成指定格式的list
            List<Category2Vo> category2Vos = Optional.ofNullable(entities).orElse(new ArrayList<>()).stream().map(l2 -> {
                Category2Vo category2Vo = new Category2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                // 当前2级分类的3级分类封装成VO
                List<Category2Vo.CateLog3Vo> cateLog3Vos = Optional.ofNullable(baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", l2.getCatId()))).orElse(new ArrayList<>())
                        .stream().map(l3 -> {
                            Category2Vo.CateLog3Vo log3Vo = new Category2Vo.CateLog3Vo(l2.getParentCid().toString(), l3.getCatId().toString(), l3.getName());
                            return log3Vo;
                        }).collect(Collectors.toList());
                category2Vo.setCatalog3List(cateLog3Vos);
                return category2Vo;
            }).collect(Collectors.toList());
            return category2Vos;
        }));

        return listMap;
    }

    //225,25,2
    private List<Long> findParentPath(Long catelogId,List<Long> paths){
        //1、收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if(byId.getParentCid()!=0){
            findParentPath(byId.getParentCid(),paths);
        }
        return paths;

    }

}
