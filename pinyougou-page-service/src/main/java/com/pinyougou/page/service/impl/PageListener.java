package com.pinyougou.page.service.impl;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.stereotype.Component;

import com.pinyougou.page.service.ItemPageService;

/**页面生成监听器
 * @author Administrator
 *
 */
@Component
public class PageListener implements MessageListener {

	@Resource
	private ItemPageService itemPageService;
	
	@Override
	public void onMessage(Message message) {
		System.out.println("页面监听器接收到有消息发送");
		TextMessage textMessage = (TextMessage)message;
		try {
			String text = textMessage.getText();
			System.out.println("页面监听器接收到消息：" + text);
			Long id = Long.valueOf(text);
			boolean html = itemPageService.genItemHtml(id);
			if(html){
				System.out.println("页面生成成功！");
			} else {
				System.out.println("页面生成失败！");
			}
		} catch (JMSException e) {
			System.out.println("页面生成出错！");
			e.printStackTrace();
		}

	}

}
