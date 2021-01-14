package com.bjc.gulimall.product.service.impl;

import com.bjc.common.constant.ProductConstant;
import com.bjc.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.bjc.gulimall.product.dao.AttrGroupDao;
import com.bjc.gulimall.product.dao.CategoryDao;
import com.bjc.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.bjc.gulimall.product.entity.AttrGroupEntity;
import com.bjc.gulimall.product.entity.CategoryEntity;
import com.bjc.gulimall.product.service.CategoryService;
import com.bjc.gulimall.product.vo.AttrGroupRelationVo;
import com.bjc.gulimall.product.vo.AttrResponseVo;
import com.bjc.gulimall.product.vo.AttrVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bjc.common.utils.PageUtils;
import com.bjc.common.utils.Query;

import com.bjc.gulimall.product.dao.AttrDao;
import com.bjc.gulimall.product.entity.AttrEntity;
import com.bjc.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationDao relationDao;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private CategoryDao categoryDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity entity = new AttrEntity();
        BeanUtils.copyProperties(attr,entity);
        // 保存基本信息
        baseMapper.insert(entity);
        if(attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attr.getAttrGroupId() != null){        // 基础属性才需要保存关联关系
            // 保存关联分组信息
            AttrAttrgroupRelationEntity relation = new AttrAttrgroupRelationEntity();
            relation.setAttrGroupId(attr.getAttrGroupId());
            relation.setAttrId(entity.getAttrId());
            relationDao.insert(relation);
        }

    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("attr_type","base".equalsIgnoreCase(type)?ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode():ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());// 基础属性与销售属性
        if(catelogId != 0){
            wrapper.eq("catelog_id",catelogId);
            String key = (String)params.get("key");
            if(StringUtils.isNotEmpty(key)){
                wrapper.and(w -> w.eq("attr_id",key).or().like("attr_name",key));
            }
        }

        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params),wrapper);
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();
        List<AttrResponseVo> attrResponseVos = records.stream().map(attrEntity -> {
            AttrResponseVo attrResponseVo = new AttrResponseVo();
            BeanUtils.copyProperties(attrEntity, attrResponseVo);

            // 设置分组的名称
            if("base".equalsIgnoreCase(type)){      // 基础属性才需要设置分组
                AttrAttrgroupRelationEntity relationEntity = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
                if (!ObjectUtils.isEmpty(relationEntity)) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());
                    if(null != attrGroupEntity && attrGroupEntity.getAttrGroupId() != null){
                        attrResponseVo.setGroupName(attrGroupEntity.getAttrGroupName());
                    }
                }
            }

            // 设置分类名称
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (null != categoryEntity) {
                attrResponseVo.setCatelogName(categoryEntity.getName());
            }

            return attrResponseVo;
        }).collect(Collectors.toList());

        // 将处理之后的结果设置到返回对象
        pageUtils.setList(attrResponseVos);

        return pageUtils;
    }

    @Autowired
    CategoryService categoryService;

    @Override
    public AttrResponseVo getAttrInfo(Long attrId) {
        AttrResponseVo responseVo = new AttrResponseVo();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity,responseVo);

        if(attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {        // 基础属性才需要保存关联关系
            // 根据attrId查询关联信息
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if(null != attrAttrgroupRelationEntity){
                responseVo.setAttrGroupId(attrAttrgroupRelationEntity.getAttrGroupId());
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
                if(null != attrGroupEntity){
                    responseVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }

        // 设置分类信息
        Long catelogId = attrEntity.getCatelogId();
        Long[] catelogPath = categoryService.findCatelogPath(catelogId);
        responseVo.setCatelogPath(catelogPath);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        if(null != categoryEntity){
            responseVo.setCatelogName(categoryEntity.getName());
        }

        return responseVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attr) {
        AttrEntity entity = new AttrEntity();
        BeanUtils.copyProperties(attr,entity);
        // 修改基本信息
        this.updateById(entity);

        if(entity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()){        // 基础属性才需要保存关联关系
            // 修改关联分组信息
            AttrAttrgroupRelationEntity relation = new AttrAttrgroupRelationEntity();
            relation.setAttrGroupId(attr.getAttrGroupId());
            relation.setAttrId(attr.getAttrId());
            Integer count = relationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            if(count > 0){
                relationDao.update(relation,new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",attr.getAttrId()));
            } else {
                relationDao.insert(relation);
            }
        }
    }

    /** 根据分组ID查询关联的基本属性 */
    @Transactional
    @Override
    public List<AttrEntity> getRelationAttr(Long attrGroupId) {
        // 思路：根据attrGroupId在关联表pms_attr_attrgroup_relation中找到所有的attr_id，在去attr表中查询即可
        List<AttrAttrgroupRelationEntity> entityList = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrGroupId));
        List<Long> attrIds = entityList.stream().map(attr -> attr.getAttrId()).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(attrIds)){
            return new ArrayList<>();
        }
        Collection<AttrEntity> list = this.listByIds(attrIds);
        return (List<AttrEntity>)list;
    }

    @Override
    public void deleteRelation(List<AttrGroupRelationVo> vos) {
        // relationDao.delete(new QueryWrapper<>().eq("attr_id",1L).eq("attr_group_id",1L));

        // 希望可以一次性执行批量操作，只发送一次请求
        // List<AttrAttrgroupRelationEntity> collect = vos.stream().map(item -> new AttrAttrgroupRelationEntity().setAttrId(item.getAttrId()).setAttrGroupId(item.getAttrGroupId())).collect(Collectors.toList());

        List<AttrAttrgroupRelationEntity> collect = vos.stream().map(item -> {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item,attrAttrgroupRelationEntity);
            return attrAttrgroupRelationEntity;
        }).collect(Collectors.toList());
        QueryWrapper<AttrAttrgroupRelationEntity> query = new QueryWrapper();
        // query.apply("1=1");
        collect.stream().forEach(item -> {
            query.or(q1 -> q1.eq("attr_id",item.getAttrId()).eq("attr_group_id",item.getAttrGroupId()));
        });

        relationDao.delete(query);
    }

    /** 获取当前分组没有关联的所有属性 */
    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrGroupId) {
        // 1. 当前分组只能关联自己所属的分类里面的属性
        //      1.1 查询当前分组的信息
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
        //      1.2 获取当前分类ID
        Long catelogId = attrGroupEntity.getCatelogId();

        // 2. 当前分组只能关联别的分组没有引用的属性
        //      2.1 当前分类下的其他分组
        List<AttrGroupEntity> groups = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        //      2.2 这些分组关联的属性
        List<Long> sttrGroupsids = groups.stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());
        List<AttrAttrgroupRelationEntity> groupids = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", sttrGroupsids));
        List<Long> attrIds = groupids.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        //      2.3 从当前分类的所有属性中移除这些属性
        QueryWrapper<AttrEntity> wraper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).eq("attr_type",ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if(!CollectionUtils.isEmpty(attrIds)){
            wraper.notIn("attr_id", attrIds);
        }
        String key = (String) params.get("key");
        if(StringUtils.isNotEmpty(key)){
            wraper.and(w->{
                w.eq("attr_id",key).or().like("attr_name",key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wraper);
        PageUtils pageUtils = new PageUtils(page);
        return pageUtils;
    }

}
