package com.bjc.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bjc.common.utils.PageUtils;
import com.bjc.gulimall.product.entity.AttrGroupEntity;
import com.bjc.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.bjc.gulimall.product.vo.SkuItemVo;
import com.bjc.gulimall.product.vo.spuItemAttrGroupVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author zoudm
 * @email zoudmbean@163.com
 * @date 2020-12-08 22:27:02
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageByCatId(Map<String, Object> params, Long catId);

    List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatlogId(Long catlogId);

    List<spuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId);

}

