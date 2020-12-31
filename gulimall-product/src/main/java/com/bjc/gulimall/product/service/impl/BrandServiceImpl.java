package com.bjc.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.bjc.common.utils.R;
import com.bjc.gulimall.product.dao.CategoryBrandRelationDao;
import com.bjc.gulimall.product.entity.CategoryBrandRelationEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bjc.common.utils.PageUtils;
import com.bjc.common.utils.Query;

import com.bjc.gulimall.product.dao.BrandDao;
import com.bjc.gulimall.product.entity.BrandEntity;
import com.bjc.gulimall.product.service.BrandService;
import org.springframework.transaction.annotation.Transactional;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    private CategoryBrandRelationDao categoryBrandRelationDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<BrandEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(StringUtils.isNotEmpty(key)){
            queryWrapper.eq("brand_id",key).or().like("name",key).or().like("descript",key);
        }
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void updateBrand(BrandEntity brand) {
        // 1. 修改品牌表
        baseMapper.updateById(brand);
        // 2. 修改品牌分类关联关系表
        if(StringUtils.isNotEmpty(brand.getName())){
            CategoryBrandRelationEntity entity = new CategoryBrandRelationEntity();
            entity.setBrandId(brand.getBrandId());
            entity.setBrandName(brand.getName());
            QueryWrapper<CategoryBrandRelationEntity> wrapper = new QueryWrapper();
            wrapper.eq("brand_id",brand.getBrandId());
            categoryBrandRelationDao.update(entity,wrapper);
        }
        // 3. TODO 修改其他品牌关系
    }

}
