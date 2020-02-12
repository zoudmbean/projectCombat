package com.pinyougou.search.service.impl;

import java.util.List;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Component
public class ItemSearchListener implements MessageListener{
	
	@Resource
	private ItemSearchService itemSearchService;

	@Override
	public void onMessage(Message message) {
		System.out.println("添加索引库开始接受消息：");
		TextMessage textMessage = (TextMessage)message;
		try {
			// 获取消息队列中的数据
			String itemListStr = textMessage.getText();
			System.out.println("接收到的消息是：" + itemListStr);
			List<TbItem> itemList = JSON.parseArray(itemListStr, TbItem.class);
			
			// 执行方法，存入solr库
			itemSearchService.importList(itemList);
			System.out.println("保存索引库成功！");
		} catch (JMSException e) {
			System.out.println("搜索服务接收消息失败！");
			e.printStackTrace();
		}
		
	}

}
