package com.bjc.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntBinaryOperator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bjc.common.utils.PageUtils;
import com.bjc.common.utils.Query;

import com.bjc.gulimall.ware.dao.WareSkuDao;
import com.bjc.gulimall.ware.entity.WareSkuEntity;
import com.bjc.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageCondition(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();

        String skuId = (String)params.get("skuId");
        if(StringUtils.isNotEmpty(skuId)){
            wrapper.eq("sku_id",skuId);
        }
        String wareId = (String)params.get("wareId");
        if(StringUtils.isNotEmpty(wareId)){
            wrapper.eq("ware_Id",wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void addStore(Long skuId, Long wareId, Integer skuNum) {
        WareSkuEntity entity = new WareSkuEntity();
        entity.setSkuId(skuId).setWareId(wareId).setStock(skuNum);
        QueryWrapper<WareSkuEntity> wraper = new QueryWrapper();
        wraper.eq("sku_id",skuId).eq("ware_id",wareId);
        List<WareSkuEntity> wareSkuEntities = this.baseMapper.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if(!CollectionUtils.isEmpty(wareSkuEntities)){
            IntBinaryOperator i;
            int stock = wareSkuEntities.stream()
                    .mapToInt(ware -> Optional.ofNullable(ware.getStock()).orElse(0))
                    .reduce(0,(l,r) -> l + r);
            entity.setStock(stock + entity.getStock());
        }
        this.saveOrUpdate(entity,wraper);
    }

}
