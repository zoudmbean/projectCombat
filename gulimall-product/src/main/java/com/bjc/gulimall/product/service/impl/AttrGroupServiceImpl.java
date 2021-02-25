package com.bjc.gulimall.product.service.impl;

import com.bjc.gulimall.product.entity.AttrEntity;
import com.bjc.gulimall.product.service.AttrService;
import com.bjc.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.bjc.gulimall.product.vo.spuItemAttrGroupVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bjc.common.utils.PageUtils;
import com.bjc.common.utils.Query;

import com.bjc.gulimall.product.dao.AttrGroupDao;
import com.bjc.gulimall.product.entity.AttrGroupEntity;
import com.bjc.gulimall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 根据分类ID查询列表
     * */
    @Override
    public PageUtils queryPageByCatId(Map<String, Object> params, Long catId) {
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        // 1. 如果分类ID为空，那么查询所有
        if(null != catId && catId != 0){
            // 2. 否则，拼接条件
            wrapper = new QueryWrapper<AttrGroupEntity>().eq("catelog_id",catId);
        }

        // 如果有查询条件
        String key = (String)params.get("key");
        if(StringUtils.isNotEmpty(key)){
            wrapper.and(con -> {
                con.eq("attr_group_id",key).or().like("attr_group_name",key);
            });
        }
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    /**
     * 根据分类id查出所有的分组以及这些组里面的属性
     * */
    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatlogId(Long catlogId) {
        // 1. 查询分组信息
        List<AttrGroupEntity> attrGroupEntitys = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catlogId));
        // 2. 查询所有属性
        List<AttrGroupWithAttrsVo> collect = attrGroupEntitys.stream().map(group -> {
            AttrGroupWithAttrsVo vo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(group,vo);
            // 根据分组ID找到所有的属性实体
            List<AttrEntity> relationAttr = attrService.getRelationAttr(group.getAttrGroupId());
            vo.setAttrs(relationAttr);
            return vo;
        }).collect(Collectors.toList());
        return collect;
    }

    /*
    * 查出当前SPU对应的所有属性的分组信息以及当前分组下的所有属性对应的值
    * */
    @Override
    public List<spuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
        // 1.
        AttrGroupDao baseMapper = this.getBaseMapper();
        List<spuItemAttrGroupVo> vos = baseMapper.getAttrGroupWithAttrsBySpuId(spuId,catalogId);
        return vos;
    }

}
