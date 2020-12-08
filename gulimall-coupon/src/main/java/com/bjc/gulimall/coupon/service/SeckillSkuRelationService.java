package com.bjc.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bjc.common.utils.PageUtils;
import com.bjc.gulimall.coupon.entity.SeckillSkuRelationEntity;

import java.util.Map;

/**
 * 秒杀活动商品关联
 *
 * @author zoudm
 * @email zoudmbean@163.com
 * @date 2020-12-08 23:22:35
 */
public interface SeckillSkuRelationService extends IService<SeckillSkuRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

