package com.bjc.gulimall.product.vo;

import com.bjc.gulimall.product.entity.SkuImagesEntity;
import com.bjc.gulimall.product.entity.SkuInfoEntity;
import com.bjc.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {
    // 1. sku基本信息获取 sku_Info
    private SkuInfoEntity info;
    // 2. sku图片信息   sku_images
    private List<SkuImagesEntity> images;
    // 3. 获取spu的销售属性组合
    List<ItemSaleAttrVo> saleAttr;

    // 4. 获取spu介绍
    private SpuInfoDescEntity desp;

    // 5.获取spu的规格参数信息
    private List<spuItemAttrGroupVo> groupAttrs;

    // 6. 是否有货
    private boolean hasStock = true;
}
