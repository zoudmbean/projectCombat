package com.bjc.gulimall.product.service.impl;

import com.bjc.common.utils.R;
import com.bjc.gulimall.product.dao.BrandDao;
import com.bjc.gulimall.product.dao.CategoryDao;
import com.bjc.gulimall.product.entity.BrandEntity;
import com.bjc.gulimall.product.entity.CategoryEntity;
import com.bjc.gulimall.product.service.BrandService;
import com.bjc.gulimall.product.vo.BrandVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bjc.common.utils.PageUtils;
import com.bjc.common.utils.Query;

import com.bjc.gulimall.product.dao.CategoryBrandRelationDao;
import com.bjc.gulimall.product.entity.CategoryBrandRelationEntity;
import com.bjc.gulimall.product.service.CategoryBrandRelationService;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Autowired
    private BrandDao brandDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private CategoryBrandRelationDao categoryBrandRelationDao;

    @Autowired
    private BrandService brandService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public R queryPageByBrandId(Long brandId) {
        QueryWrapper<CategoryBrandRelationEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("brand_id",brandId);
        List<CategoryBrandRelationEntity> list = baseMapper.selectList(wrapper);

        return R.ok().put("data",list);

    }

    @Override
    public List<BrandEntity> getBrandsByCatId(Long catId) {
        List<CategoryBrandRelationEntity> catelogId = categoryBrandRelationDao.selectList(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
        List<BrandEntity> collect = catelogId.stream().map(item -> {
            Long brandId = item.getBrandId();
            BrandEntity byId = brandService.getById(brandId);
            return byId;
        }).collect(Collectors.toList());
        return collect;
    }

    /*
    * 新增品牌与分类关联关系
    * */
    @Override
    public R saveCategorybrandrelation(CategoryBrandRelationEntity categoryBrandRelationEntity) {
        Long brandId = categoryBrandRelationEntity.getBrandId();
        Long cateId = categoryBrandRelationEntity.getCatelogId();

        if(null == brandId || null == cateId){
            return R.error(500,"品牌ID或者分类ID不能为空！");
        }

        BrandEntity brandEntity = brandDao.selectById(brandId);
        CategoryEntity categoryEntity = categoryDao.selectById(cateId);
        if(null == brandEntity || null == categoryEntity){
            return R.error(500,"品牌ID或者分类ID不存在！");
        }

        categoryBrandRelationEntity.setBrandName(brandEntity.getName());
        categoryBrandRelationEntity.setCatelogName(categoryEntity.getName());

        try {
            baseMapper.insert(categoryBrandRelationEntity);
        }catch (Exception e) {
            throw new RuntimeException("数据库异常，保存品牌分类关联信息出错！");
        }

        return R.ok();
    }

}
