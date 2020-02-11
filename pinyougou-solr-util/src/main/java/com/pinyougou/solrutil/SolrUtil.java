package com.pinyougou.solrutil;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbItemExample.Criteria;

@Component
public class SolrUtil {
	
	@Resource
	private TbItemMapper itemMapper;
	
	@Resource
	private SolrTemplate solrTemplate;
	
	/**
	 *  批量导入数据
	 */
	public void importItemData(){
		TbItemExample example = new TbItemExample();
		Criteria criteria = example.createCriteria();
		
		// 已审核
		criteria.andStatusEqualTo("1");
		List<TbItem> itemList = itemMapper.selectByExample(example);
		for(TbItem item : itemList){
			// 获取规格字符串
			String spec = item.getSpec();
			// 规格字符串转成map集合
			Map specMap = JSON.parseObject(spec, Map.class);
			item.setSpecMap(specMap);
		}
		
		// 保存
		solrTemplate.saveBeans(itemList);
		// 提交
		solrTemplate.commit();
	}
	
	/**
	 * 删除所有数据
	 */
	public void deleAll(){
		SolrDataQuery query = new SimpleQuery("*:*");
		solrTemplate.delete(query);
		solrTemplate.commit();
	}
	
	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
		SolrUtil solrUtil = (SolrUtil) context.getBean("solrUtil");
		// 导入数据
		 solrUtil.importItemData();
		// 删除所有数据
		//solrUtil.deleAll();
	}
	
}
