package com.bjc.gulimall.order.dao;

import com.bjc.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author zoudm
 * @email zoudmbean@163.com
 * @date 2020-12-08 23:26:55
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
