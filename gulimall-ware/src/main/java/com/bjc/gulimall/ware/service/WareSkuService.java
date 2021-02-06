package com.bjc.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bjc.common.utils.PageUtils;
import com.bjc.gulimall.ware.entity.WareSkuEntity;
import com.bjc.gulimall.ware.vo.SkuHasStockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author zoudm
 * @email zoudmbean@163.com
 * @date 2020-12-08 23:28:45
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageCondition(Map<String, Object> params);

    void addStore(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds);

}

