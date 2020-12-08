package com.bjc.gulimall.product.dao;

import com.bjc.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author zoudm
 * @email zoudmbean@163.com
 * @date 2020-12-08 22:27:02
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
