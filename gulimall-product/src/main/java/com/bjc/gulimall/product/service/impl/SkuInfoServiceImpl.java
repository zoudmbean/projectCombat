package com.bjc.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.bjc.common.utils.R;
import com.bjc.gulimall.product.entity.SkuImagesEntity;
import com.bjc.gulimall.product.entity.SpuInfoDescEntity;
import com.bjc.gulimall.product.feign.SeckillFeignService;
import com.bjc.gulimall.product.service.*;
import com.bjc.gulimall.product.vo.ItemSaleAttrVo;
import com.bjc.gulimall.product.vo.SeckillSkuRedisVo;
import com.bjc.gulimall.product.vo.SkuItemVo;
import com.bjc.gulimall.product.vo.spuItemAttrGroupVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bjc.common.utils.PageUtils;
import com.bjc.common.utils.Query;

import com.bjc.gulimall.product.dao.SkuInfoDao;
import com.bjc.gulimall.product.entity.SkuInfoEntity;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private SkuImagesService imagesService;

    @Autowired
    private SpuInfoDescService descService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Autowired
    private SeckillFeignService seckillFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and(q -> {
               q.eq("sku_id",key).or().like("sku_name",key);
            });
        }
        String catelogId = (String) params.get("catelogId");
        if(!StringUtils.isEmpty(catelogId) && !StringUtils.equalsIgnoreCase("0",catelogId)){
            wrapper.eq("catelog_id",catelogId);
        }
        String brandId = (String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId) && !StringUtils.equalsIgnoreCase("0",brandId) ){
            wrapper.eq("brand_id",brandId);
        }
        String min = (String) params.get("min");
        if(!StringUtils.isEmpty(key)){
            wrapper.ge("price",min);
        }
        String max = (String) params.get("max");
        if(!StringUtils.isEmpty(max)){
            try {
                BigDecimal maxBigDecimal = new BigDecimal(max);
                if(maxBigDecimal.compareTo(new BigDecimal("0")) == 1){  // 大于0的时候才执行
                    wrapper.le("price",max);
                }
            } catch (Exception e) {
                log.error("最大价格转换BigDecimal异常！");
            }
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBuSpuId(Long spuId) {
        return this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id",spuId));
    }

    /*
    * 查询详情
    * */
    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo vo = new SkuItemVo();
        // 1. sku基本信息获取 sku_Info
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity skuInfoEntity = this.getById(skuId);
            vo.setInfo(skuInfoEntity);
            return skuInfoEntity;
        }, executor);

        // 3 4 5 需要等待info执行完毕才能执行
        CompletableFuture<Void> saleFuture = infoFuture.thenAcceptAsync(res -> {
            if (null != res) {
                Long spuId = res.getSpuId();
                Long catalogId = res.getCatalogId();
                // 3. 获取spu的销售属性组合
                List<ItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(spuId);
                vo.setSaleAttr(saleAttrVos);
            }
        }, executor);

        CompletableFuture<Void> desFuture = infoFuture.thenAcceptAsync(res -> {
            if (null != res) {
                Long spuId = res.getSpuId();
                Long catalogId = res.getCatalogId();
                // 4. 获取spu介绍
                SpuInfoDescEntity descEntity = descService.getById(spuId);
                vo.setDesp(descEntity);
            }
        }, executor);

        CompletableFuture<Void> groupsFuture = infoFuture.thenAcceptAsync(res -> {
            if (null != res) {
                Long spuId = res.getSpuId();
                Long catalogId = res.getCatalogId();
                // 5.获取spu的规格参数信息
                List<spuItemAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(spuId, catalogId);
                vo.setGroupAttrs(attrGroupVos);
            }
        }, executor);


        // 2. sku图片信息   sku_images  独立的线程，不需要等待info执行完毕
        CompletableFuture<Void> imagesFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> skuImages = imagesService.list(new QueryWrapper<SkuImagesEntity>().eq("sku_id", skuId));
            vo.setImages(skuImages);
        }, executor);

        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
            // 查询当前sku是否参与秒杀优惠
            R r = seckillFeignService.getSkuSeckillInfo(skuId);
            if (r.getCode() == 0) {
                SeckillSkuRedisVo seckill = r.getData(new TypeReference<SeckillSkuRedisVo>() {
                });
                vo.setSeckillInfo(seckill);
            }
        }, executor);


        // 等待所有任务结束 infoFuture可以不用写
        CompletableFuture.allOf(imagesFuture,saleFuture,desFuture,groupsFuture,seckillFuture).get();

        return vo;
    }

}
