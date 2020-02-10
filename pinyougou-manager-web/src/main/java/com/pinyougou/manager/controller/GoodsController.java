package com.pinyougou.manager.controller;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.povo.Goods;
import com.pinyougou.search.service.ItemSearchService;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;
/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;
	
	@Reference(timeout=100000)
	private ItemSearchService itemSearchService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(@RequestBody TbGoods goods,int page,int rows){	
		return goodsService.findPage(goods,page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
		try {
			String name = SecurityContextHolder.getContext().getAuthentication().getName();
			// 设置店铺名称
			goods.getGoods().setSellerId(name);
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			goodsService.delete(ids);
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/deleGoods")
	public Result deleGoods(Long [] ids){
		try {
			goodsService.deleGoods(ids);
			try {
				itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("删除索引失败！");
			}
			
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
	/** 审核与驳回
	 * @param ids
	 * @param status
	 * @return
	 */
	@RequestMapping("/updateAuditStatus")
	public Result updateAuditStatus(Long [] ids,String status){
		try {
			goodsService.updateAuditStatus(ids,status);
			
			try {
				// 审核成功之后，更新索引库
				if("1".equals(status)){
					// 1. 查询
					List<TbItem> itemList = goodsService.findItemListByGoodsIdandStatus(ids, status);
					// 2. 导入索引库
					if(!CollectionUtils.isEmpty(itemList)){
						itemSearchService.importList(itemList);
					}
				}
			} catch (Exception e) {
				System.out.println("更新索引库失败！");
				e.printStackTrace();
			}
			return new Result(true, "操作成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "操作失败");
		}
	}
	
}
