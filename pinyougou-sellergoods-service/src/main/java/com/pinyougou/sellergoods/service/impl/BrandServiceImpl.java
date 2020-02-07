package com.pinyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.pojo.TbBrandExample.Criteria;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;
import entity.Result;

/**
 * 品牌服务实现类
 * @author Administrator
 *
 */
@Service
@Transactional
public class BrandServiceImpl implements BrandService{
	
	@Resource
	private TbBrandMapper brandMapper;

	@Override
	public List<TbBrand> findAll() {
		return brandMapper.selectByExample(null);
	}

	@Override
	public PageResult<TbBrand> findPage(TbBrand brand,int pageNum, int pagesize) {
		PageHelper.startPage(pageNum, pagesize); // 分页
		TbBrandExample example = null;
		if(null != brand){
			example = new TbBrandExample();
			Criteria createCriteria = example.createCriteria();
			if(!StringUtils.isEmpty(brand.getName())){
				createCriteria.andNameLike("%" + brand.getName() + "%");
			}
			if(!StringUtils.isEmpty(brand.getFirstChar())){
				createCriteria.andFirstCharEqualTo(brand.getFirstChar());
			}
		}
		Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(example);
		return new PageResult<>(page.getTotal(), page.getResult());
	}

	@Override
	public void add(TbBrand brand) {
		brandMapper.insert(brand);
	}

	@Override
	public TbBrand findOne(Long id) {
		return brandMapper.selectByPrimaryKey(id);
	}

	@Override
	public void update(TbBrand brand) {
		brandMapper.updateByPrimaryKey(brand);
	}

	@Override
	public void delete(Long[] ids) {
		brandMapper.delByIds(ids);
	}

	@Override
	public List<Map<Long, String>> selectOptions() {
		return brandMapper.selectOptions();
	}

}
