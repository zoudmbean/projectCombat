package com.bjc.gulimall.ware.dao;

import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.bjc.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 商品库存
 *
 * @author zoudm
 * @email zoudmbean@163.com
 * @date 2020-12-08 23:28:45
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    @Select("select sum(stock - stock_locked) from wms_ware_sku where sku_id = #{skuId}")
    Long getSkusHasStock(@Param("skuId") Long skuId);
}
