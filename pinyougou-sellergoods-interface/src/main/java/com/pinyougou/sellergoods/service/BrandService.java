package com.pinyougou.sellergoods.service;

import java.util.List;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;
import entity.Result;

/**
 * 品牌接口
 * @author Administrator
 *	
 */
public interface BrandService {
	/**
	 * 返回品牌列表
	 * @return
	 */
	List<TbBrand> findAll();
	
	/**分页查询品牌列表
	 * @param pageNum  当前页
	 * @param pagesize 每页记录数
	 * @return
	 */
	PageResult<TbBrand> findPage(TbBrand brand,int pageNum,int pagesize);
	
	/**
	 * 添加品牌
	 * @param brand
	 */
	void add(TbBrand brand);
	
	/**
	 * 根据ID查询品牌信息
	 * @param id
	 * @return
	 */
	TbBrand findOne(Long id);
	
	/**修改品牌信息
	 * @param brand
	 * @return
	 */
	void update(TbBrand brand);
	
	/** 根据id批量删除
	 * @param ids
	 */
	void delete(Long[] ids);
	
	
}
