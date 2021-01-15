package com.bjc.gulimall.coupon.service.impl;

import com.bjc.common.to.MemberPrice;
import com.bjc.common.to.SkuReductionTo;
import com.bjc.gulimall.coupon.entity.MemberPriceEntity;
import com.bjc.gulimall.coupon.entity.SkuLadderEntity;
import com.bjc.gulimall.coupon.service.MemberPriceService;
import com.bjc.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bjc.common.utils.PageUtils;
import com.bjc.common.utils.Query;

import com.bjc.gulimall.coupon.dao.SkuFullReductionDao;
import com.bjc.gulimall.coupon.entity.SkuFullReductionEntity;
import com.bjc.gulimall.coupon.service.SkuFullReductionService;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    private SkuLadderService skuLadderService;

    @Autowired
    private MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        // 1. 保存阶梯价格
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuReductionTo.getSkuId());
        skuLadderEntity.setFullCount(skuReductionTo.getFullCount());
        skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
        skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
        if(skuReductionTo.getFullCount() > 0){
            skuLadderService.save(skuLadderEntity);
        }

        // 2.保存满减打折
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo,skuFullReductionEntity);
        if(skuFullReductionEntity.getFullPrice().compareTo(new BigDecimal("0")) > 0){
            this.save(skuFullReductionEntity);
        }

        // 3. 会员价格
        List<MemberPrice> memberPrice = skuReductionTo.getMemberPrice();
        List<MemberPriceEntity> collect = memberPrice.stream().map(item -> {
            MemberPriceEntity entity = new MemberPriceEntity();
            return entity.setSkuId(skuReductionTo.getSkuId())
                    .setMemberLevelId(item.getId())
                    .setMemberLevelName(item.getName())
                    .setMemberPrice(item.getPrice())
                    .setAddOther(1);
        }).filter(item -> item.getMemberPrice().compareTo(new BigDecimal("0")) == 1).collect(Collectors.toList());
        memberPriceService.saveBatch(collect);
    }

}
