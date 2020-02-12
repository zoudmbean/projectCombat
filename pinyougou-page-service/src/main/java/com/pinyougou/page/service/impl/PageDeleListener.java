package com.pinyougou.page.service.impl;

import java.io.Serializable;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.springframework.stereotype.Component;

import com.pinyougou.page.service.ItemPageService;

/**
 * 删除HTML监听器
 * @author Administrator
 *
 */
@Component
public class PageDeleListener implements MessageListener {

	@Resource
	private ItemPageService itemPageService;
	
	@Override
	public void onMessage(Message message) {
		System.out.println("页面删除监听开始工作。");
		ObjectMessage om =  (ObjectMessage)message;
		System.out.println("页面删除监听接收到消息。");
		try {
			Long[] ids = (Long[]) om.getObject();
			itemPageService.deleItemHtml(ids);
			System.out.println("接收到的消息是：" + ids[0]);
		} catch (JMSException e) {
			System.out.println("删除页面监听器出错！");
			e.printStackTrace();
		}
	}

}
