package com.pinyougou.sellergoods.service;
import java.util.List;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.povo.Goods;

import entity.PageResult;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface GoodsService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbGoods> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum,int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(Goods goods);
	
	
	/**
	 * 修改
	 */
	public void update(Goods goods);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public Goods findOne(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long [] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbGoods goods, int pageNum,int pageSize);


	/** 审核与驳回
	 * @param ids
	 * @param status
	 */
	public void updateAuditStatus(Long[] ids, String status);


	/** 删除商品
	 * @param ids
	 */
	public void deleGoods(Long[] ids);


	/** 上下架操作
	 * @param goods
	 */
	public void updateMaeketable(TbGoods goods);
	
	/**根据商品ID和状态查询Item表信息  
	 * @param ids
	 * @param status
	 * @return
	 */
	public List<TbItem> findItemListByGoodsIdandStatus(Long[] ids,String status);
	
}
