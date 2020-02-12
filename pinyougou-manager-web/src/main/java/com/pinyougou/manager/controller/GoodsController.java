package com.pinyougou.manager.controller;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.commons.collections.ListUtils;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.povo.Goods;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.sellergoods.service.ItemService;

import entity.PageResult;
import entity.Result;
import redis.clients.jedis.JedisMonitor;
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
	
	@Resource
	private JmsTemplate jmsTemplate;
	
	@Resource
	private Destination queueSolrDestination;
	
	@Resource
	private Destination topicPageDestination;
	
	@Resource
	private Destination topicPageDeleDestination;
	
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
	
	
	@Resource
	private Destination queueSolrDeleDestination;
	
	@Reference
	private ItemService itemService;
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/deleGoods")
	public Result deleGoods(final Long [] ids){
		try {
			goodsService.deleGoods(ids);
			try {
				// 删除索引库
				// itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
				
				final List<TbItem> items = itemService.getIdsByGoodsId(Arrays.asList(ids));
				
				// 消息队列发送删除索引库信息
				jmsTemplate.send(queueSolrDeleDestination, new MessageCreator() {
					@Override
					public Message createMessage(Session session) throws JMSException {
						List iIds = new ArrayList<>();
						for(TbItem t : items){
							iIds.add(t.getId());
						}
						return session.createTextMessage(JSON.toJSONString(iIds));
					}
				});
				
				/*
				 * 删除页面
				 * */
				jmsTemplate.send(topicPageDeleDestination, new MessageCreator() {
					@Override
					public Message createMessage(Session session) throws JMSException {
						return session.createObjectMessage(ids);
					}
				});
				
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
				if("1".equals(status)){
					/* 1. 审核成功之后，更新索引库*/
					// 1.1 查询
					List<TbItem> itemList = goodsService.findItemListByGoodsIdandStatus(ids, status);
					// 1.2 导入索引库
					if(!CollectionUtils.isEmpty(itemList)){
						// itemSearchService.importList(itemList);
						// 使用activeMQ来实现
						// 注意：L这里需要转成json字符串，因为数据源是list集合，list集合没有实现可序列化接口不能再网络传递数据
						final String jsonString = JSON.toJSONString(itemList);   
						jmsTemplate.send(queueSolrDestination, new MessageCreator() {
							@Override
							public Message createMessage(Session session) throws JMSException {
								return session.createTextMessage(jsonString);
							}
						});
						
					}
					
					/* 2. 审核成功之后，生成商品详情页*/
					for(final Long goodsId : ids){
						// boolean flag = itemPageService.genItemHtml(goodsId);
						jmsTemplate.send(topicPageDestination, new MessageCreator() {
							@Override
							public Message createMessage(Session session) throws JMSException {
								return session.createTextMessage(goodsId+"");
							}
						});
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
