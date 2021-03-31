package com.bjc.gulimall.ware.dao;

import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.bjc.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

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

    @Select("SELECT ware_id FROM `wms_ware_sku` WHERE sku_id=#{skuId} AND stock-stock_locked > 0")
    List<Long> listWareIdHasSkuTock(@Param("skuId") Long skuId);

    @Update("UPDATE `wms_ware_sku` SET stock_locked = stock_locked+#{num} WHERE sku_id=#{skuId} AND ware_id=#{wareId} AND stock-stock_locked > #{num}")
    Long lockSkuStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("num") Integer num);

    @Update("UPDATE `wms_ware_sku` SET `stock_locked` = `stock_locked` - #{num} WHERE `sku_id` = #{skuId} AND `ware_id` = #{wareId}")
    void unLockStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("num") Integer num);
}
