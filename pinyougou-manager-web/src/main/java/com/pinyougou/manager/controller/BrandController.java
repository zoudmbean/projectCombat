package com.pinyougou.manager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;
import entity.Result;

/**
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/brand")
public class BrandController {
	
	@Reference
	private BrandService brandService;
	
	/**
	 * 查询所有
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbBrand> findAll(){
		return brandService.findAll();
	}
	
	/**
	 * 分页查询
	 * @param page
	 * @param size
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult<TbBrand> findPage(@RequestBody TbBrand brand,int page,int size){
		return brandService.findPage(brand,page, size);
	}
	
	/**
	 * 添加品牌
	 * @param brand
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbBrand brand){
		try {
			brandService.add(brand);
			return new Result(true, "添加成功！");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "添加失败！");
		}
	}
	
	/** 根据ID查询
	 * @param id
	 * @return
	 */
	@RequestMapping("findOne")
	public TbBrand findOne(Long id){
		return brandService.findOne(id);
	}
	
	/**
	 * 添加品牌
	 * @param brand
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbBrand brand){
		try {
			brandService.update(brand);
			return new Result(true, "修改成功！");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败！");
		}
	}
	
	/**
	 * 批量删除品牌
	 * @param brand
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long[] ids){
		try {
			brandService.delete(ids);
			return new Result(true, "删除成功！");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败！");
		}
	}
	
	@RequestMapping("/selectOptionList")
	public List<Map<Long, String>> selectOptionList(){
		return brandService.selectOptions();
	}
	
}
