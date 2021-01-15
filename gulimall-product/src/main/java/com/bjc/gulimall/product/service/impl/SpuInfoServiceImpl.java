package com.bjc.gulimall.product.service.impl;

import com.bjc.common.to.SkuReductionTo;
import com.bjc.common.to.SpuBoundTo;
import com.bjc.common.utils.R;
import com.bjc.gulimall.product.entity.*;
import com.bjc.gulimall.product.feign.CouponFeignService;
import com.bjc.gulimall.product.service.*;
import com.bjc.gulimall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bjc.common.utils.PageUtils;
import com.bjc.common.utils.Query;

import com.bjc.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    // TODO 还有事务相关高级用法没做
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        // 1. 保存spu基本信息     pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo,spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);
        // 2. 保存spu的描述图片    pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",",decript));
        spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);

        // 3. 保存spu的图片集     pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(),images);

        // 4. 保存spu的基本属性（规格参数）  pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        if(!CollectionUtils.isEmpty(baseAttrs)){
            List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
                ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
                productAttrValueEntity.setAttrId(attr.getAttrId());
                AttrEntity attrEntity = attrService.getById(attr.getAttrId());
                productAttrValueEntity.setAttrName(attrEntity.getAttrName());
                productAttrValueEntity.setAttrValue(attr.getAttrValues());
                productAttrValueEntity.setQuickShow(attr.getShowDesc());
                productAttrValueEntity.setSpuId(spuInfoEntity.getId());
                return productAttrValueEntity;
            }).collect(Collectors.toList());
            productAttrValueService.saveProductAttr(collect);
        }


        // 5. 保存spu的积分信息（跨库）      sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds,spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if(r.getCode() != 0){
            log.error("远程保存SPU积分信息失败！");
        }

        // 6. 保存当前spu对应的所有sku信息
        //  6.1 sku基本信息     pms_sku_info
        List<Skus> skus = vo.getSkus();
        if(!CollectionUtils.isEmpty(skus)){
            skus.forEach(sku -> {
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku,skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);// 销量默认为0
                skuInfoEntity.setSpuId(spuInfoEntity.getId());

                List<Images> skuImages = sku.getImages();
                List<SkuImagesEntity> skuImagesEntityList = null;
                if(!CollectionUtils.isEmpty(skuImages)){
                    // 设置skuInfo的默认图片
                    List<String> imgs = skuImages.stream().filter(img -> img.getDefaultImg() == 1).map(Images::getImgUrl).collect(Collectors.toList());
                    if(!CollectionUtils.isEmpty(imgs)){
                        String defaultImg = imgs.get(0);
                        skuInfoEntity.setSkuDefaultImg(defaultImg);
                    }

                    //  6.2 sku的图片信息    pms_sku_images
                    skuImagesEntityList = skuImages.stream().map(img -> {
                        SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                        // skuImagesEntity.setSkuId(skuId);
                        skuImagesEntity.setImgUrl(img.getImgUrl());
                        img.setDefaultImg(img.getDefaultImg());
                        return skuImagesEntity;
                    }).collect(Collectors.toList());

                }

                // 保存sku信息
                skuInfoService.saveSkuInfo(skuInfoEntity);
                Long skuId = skuInfoEntity.getSkuId();

                // 6.2 sku的图片信息    pms_sku_images
                if(!CollectionUtils.isEmpty(skuImagesEntityList)){
                    List<SkuImagesEntity> imgEntities = skuImagesEntityList.stream().map(img -> {
                        SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                        skuImagesEntity.setSkuId(skuId)
                                .setImgUrl(img.getImgUrl())
                                .setDefaultImg(img.getDefaultImg());
                        return skuImagesEntity;
                    }).filter(item -> !StringUtils.isEmpty(item.getImgUrl())).collect(Collectors.toList());
                    // 没有图片路径的无需保存
                    skuImagesService.saveBatch(imgEntities);
                }

                //  6.3 sku的销售属性信息    pms_sku_sale_attr_value
                List<Attr> attrs = sku.getAttr();
                if(!CollectionUtils.isEmpty(attrs)){
                    List<SkuSaleAttrValueEntity> collect = attrs.stream().map(attr -> {
                        SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                        BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                        skuSaleAttrValueEntity.setSkuId(skuId);
                        return skuSaleAttrValueEntity;
                    }).collect(Collectors.toList());
                    skuSaleAttrValueService.saveBatch(collect);
                }

                //  6.4 sku的优惠满减信息（跨库）  sms_sku_ladder   sms_sku_full_reduction  sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(sku,skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if(skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1){
                    R r1 =couponFeignService.saveSkuReduction(skuReductionTo);
                    if(r1.getCode() != 0){
                        log.error("远程保存优惠信息失败！");
                    }
                }
            });
        }
    }

    /** 保存spu基本信息 */
    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

}
