package com.bjc.gulimall.product.dao;

import com.bjc.common.constant.ProductConstant;
import com.bjc.gulimall.product.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * spu信息
 *
 * @author zoudm
 * @email zoudmbean@163.com
 * @date 2020-12-08 22:27:02
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

    @Update("update pms_spu_info set publish_status=#{code},update_time=NOW() where id=#{spuId}")
    void updateSpuStatus(@Param("spuId") Long spuId, @Param("code") int code);
}
