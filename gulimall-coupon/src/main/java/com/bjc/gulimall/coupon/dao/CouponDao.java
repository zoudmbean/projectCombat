package com.bjc.gulimall.coupon.dao;

import com.bjc.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author zoudm
 * @email zoudmbean@163.com
 * @date 2020-12-08 23:22:36
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
