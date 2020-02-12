package com.pinyougou.search.service.impl;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.pinyougou.search.service.ItemSearchService;

/**
 * 删除索引库 jms监听器
 * @author Administrator
 *
 */
@Component
public class ItemSearchDeleListener implements MessageListener {

	@Resource
	private ItemSearchService itemSearchService;
	
	@Override
	public void onMessage(Message message) {
		TextMessage textMessage = (TextMessage)message;
		try {
			String str = textMessage.getText();
			System.out.println("接收到了消息：");
			if(!StringUtils.isEmpty(str)){
				//Long[] goodsIds = (Long[]) objectMessage.getObject();
				List<Long> ids = JSON.parseArray(str, Long.class);
				System.out.println("接收到的消息：" + ids.toString());
				itemSearchService.deleteByGoodsIds(ids);
				System.out.println("删除索引库成功！");
			}
		} catch (JMSException e) {
			System.out.println("删除索引库失败！");
			e.printStackTrace();
		}
	}

}
