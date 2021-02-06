package com.bjc.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.bjc.common.utils.R;
import com.bjc.gulimall.ware.feign.ProductFeignService;
import com.bjc.gulimall.ware.vo.SkuHasStockVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntBinaryOperator;
import java.util.stream.Collectors;

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

    @Autowired
    private ProductFeignService productFeignService;

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
            entity.setStockLocked(1);
            try{
                // TODO 远程查询sku的名称,如果失败，整个事务无需回滚
                // 1. 通过try-catch不处理方式
                // 2. 还有一种办法可以让异常出现不回滚的方法
                R info = productFeignService.info(skuId);
                if(info.getCode() == 0 ){
                    Map<String,Object> data = (Map<String,Object>) info.get("skuInfo");
                    entity.setSkuName((String) data.get("skuName"));
                }
            }catch (Exception e){}
        }
        this.saveOrUpdate(entity,wraper);
    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();
            // 查询当前sku的总库存量
            long count = Optional.ofNullable(this.baseMapper.getSkusHasStock(skuId)).orElse(0L);
            vo.setSkuId(skuId);
            vo.setHasStock(count > 0);
            return vo;
        }).collect(Collectors.toList());
        return collect;
    }

}
