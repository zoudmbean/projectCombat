package com.pinyougou.sellergoods.service.impl;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationExample;
import com.pinyougou.pojo.TbSpecificationExample.Criteria;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import com.pinyougou.povo.Specification;
import com.pinyougou.sellergoods.service.SpecificationService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;
	
	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Specification specification) {
		
		if(null != specification){
			TbSpecification tbSpecification = specification.getSpecification();
			specificationMapper.insert(tbSpecification);
			
			List<TbSpecificationOption> list = specification.getSpecificationOptionalList();
			for(TbSpecificationOption o : list){
				o.setSpecId(tbSpecification.getId());
				specificationOptionMapper.insert(o);
			}
		}
		
	}
	
	/**
	 * 修改
	 */
	@Override
	public void update(Specification specification){
		// 修改规格
		specificationMapper.updateByPrimaryKey(specification.getSpecification());
		
		// 修改规格详情
		// 1. 先跟规格ID删除详情
		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		com.pinyougou.pojo.TbSpecificationOptionExample.Criteria createCriteria = example.createCriteria();
		
		createCriteria.andSpecIdEqualTo(specification.getSpecification().getId());
		specificationOptionMapper.deleteByExample(example);
		
		// 2. 然后在保存
		List<TbSpecificationOption> list = specification.getSpecificationOptionalList();
		for(TbSpecificationOption o : list){
			if(null == o.getSpecId()){
				o.setSpecId(specification.getSpecification().getId());
			}
			specificationOptionMapper.insert(o);
		}
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Specification findOne(Long id){
		
		Specification specification = new Specification();
		specification.setSpecification(specificationMapper.selectByPrimaryKey(id));
		
		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		com.pinyougou.pojo.TbSpecificationOptionExample.Criteria createCriteria = example.createCriteria();
		
		createCriteria.andSpecIdEqualTo(id);
		List<TbSpecificationOption> selectByExample = specificationOptionMapper.selectByExample(example);
		specification.setSpecificationOptionalList(selectByExample);
		
		return specification;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			// 删除规格信息
			specificationMapper.deleteByPrimaryKey(id);
			
			// 再删除规格详情
			TbSpecificationOptionExample example = new TbSpecificationOptionExample();
			com.pinyougou.pojo.TbSpecificationOptionExample.Criteria createCriteria = example.createCriteria();
			createCriteria.andSpecIdEqualTo(id);
			specificationOptionMapper.deleteByExample(example);
		}		
	}
	
	
	@Override
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSpecificationExample example=new TbSpecificationExample();
		Criteria criteria = example.createCriteria();
		
		if(specification!=null){			
			if(!StringUtils.isEmpty(specification.getSpecName())){
				criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
			}
		}
		
		Page<TbSpecification> page= (Page<TbSpecification>)specificationMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map<Long, String>> selectOptionList() {
		// TODO Auto-generated method stub
		return specificationMapper.selectOptionList();
	}
	
}
