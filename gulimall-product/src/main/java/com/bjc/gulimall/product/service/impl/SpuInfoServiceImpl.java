package com.bjc.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.bjc.common.constant.ProductConstant;
import com.bjc.common.to.SkuHasStockTo;
import com.bjc.common.to.SkuReductionTo;
import com.bjc.common.to.SpuBoundTo;
import com.bjc.common.to.es.SkuEsModel;
import com.bjc.common.utils.R;
import com.bjc.gulimall.product.entity.*;
import com.bjc.gulimall.product.feign.CouponFeignService;
import com.bjc.gulimall.product.feign.SearchFeignService;
import com.bjc.gulimall.product.feign.WarefeignService;
import com.bjc.gulimall.product.service.*;
import com.bjc.gulimall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
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

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private WarefeignService warefeignService;

    @Autowired
    private SearchFeignService searchFeignService;

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

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String)params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and(q -> {
               q.eq("id",key).or().like("spu_name",key);
            });
        }
        String status = (String)params.get("status");
        if(!StringUtils.isEmpty(status)){
            wrapper.eq("publish_status",status);
        }
        String brandId = (String)params.get("brandId");
        if(!StringUtils.isEmpty(brandId) && "0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id",brandId);
        }
        String catelogId = (String)params.get("catelogId");
        if(!StringUtils.isEmpty(catelogId)  && "0".equalsIgnoreCase(catelogId) ){
            wrapper.eq("catalog_id",catelogId);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    /*
    * 商品上架
    * */
    @Override
    public void up(Long spuId) {
        // 2.TODO 查询当前sku的所有可以被用来检索的规格属性
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrlistForSpu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
        List<Long> searchAttrIds = attrService.selectSerchAttrIds(attrIds);
        Set<Long> idSet = new HashSet<>(searchAttrIds);
        List<SkuEsModel.Attrs> attrsList = baseAttrs.stream()
                .filter(item -> idSet.contains(item.getAttrId()))
                .map(item -> {
                    SkuEsModel.Attrs attrs1 = new SkuEsModel.Attrs();
                    BeanUtils.copyProperties(item,attrs1);
                    return attrs1;
                })
                .collect(Collectors.toList());

        // 1. 组装需要的数据
        // 1.1 查询当前spuId对应的信息,品牌名称
        List<SkuInfoEntity> skus = skuInfoService.getSkusBuSpuId(spuId);

        // TODO 发送远程调用，库存系统查询是否有库存
        Map<Long, Boolean> stockMap = null;
        try {
            List<Long> skuIds = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
            R skusHasStock = warefeignService.getSkusHasStock(skuIds);
            // 这里因为TypeReference的构造器是protected，所以，需要使用内部内对象，因此加了{}
            TypeReference<List<SkuHasStockTo>> typeReference = new TypeReference<List<SkuHasStockTo>>() {};
            List<SkuHasStockTo> datas = skusHasStock.getData(typeReference);
            stockMap = datas.stream().collect(Collectors.toMap(SkuHasStockTo::getSkuId, SkuHasStockTo::getHasStock));
        } catch (Exception e) {
            log.error("库存服务查询异常：",e);
        }

        // 1.2 封装每个sku信息
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> uoProducts = skus.stream().map(sku -> {
            SkuEsModel esModel = new SkuEsModel();
            BeanUtils.copyProperties(sku,esModel);
            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());

            // 设置库存信息
            if(null == finalStockMap){      // 如果为空，那么表示库存服务出现了问题，因此，默认让其可搜索
                esModel.setHasStock(true);
            } else {
                esModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }

            // TODO 热度评分，刚上架的热度可以给0
            esModel.setHotScore(0L);

            // TODO 查询品牌的名称 分类名称
            BrandEntity brandEntity = brandService.getById(esModel.getBrandId());
            esModel.setBrandName(brandEntity.getName());
            esModel.setBrandImg(brandEntity.getLogo());

            // TODO 分类名称
            CategoryEntity categoryEntity = categoryService.getById(esModel.getCatalogId());
            esModel.setCatalogName(categoryEntity.getName());

            // 设置检索属性
            esModel.setAttrs(attrsList);

            return esModel;
        }).collect(Collectors.toList());


        // TODO 将数据发送给es进行保存  gulimall-search
        R r = searchFeignService.productStatusUp(uoProducts);
        if(r.getCode() == 0){
            // 远程调用成功
            // TODO 成功，修改当前SPU的状态
            this.baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        } else {
            // 远程调用失败
            // TODO 重复调用问题未解决 专业术语 接口幂等性  重试机制
            /*
            * feign的调用流程：
            *   1. 构造请求数据，将对象转成json（执行之前会构造一个请求模板RequestTemplate）
            *   2. 发送请求进行执行,执行成功会解码响应数据（executeAndDecode(template)）
            *   3. 执行请求会有重试机制（默认关闭状态）
            *       while(true){
            *           retryer.continueOrPropagate(...)
            *       }
            * */
        }
    }

}
