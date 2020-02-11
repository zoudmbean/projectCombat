package com.pinyougou.page.service.impl;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.mapper.TbTypeTemplateMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbItemExample.Criteria;
import com.pinyougou.pojo.TbTypeTemplate;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateNotFoundException;

/**商品详细页实现类
 * @author Administrator
 *
 */
@Service(timeout=50000)
public class ItemPageServiceImpl implements ItemPageService{
	
	@Resource
	private TbGoodsMapper goodsMapper;
	
	@Resource
	private TbGoodsDescMapper goodsDescMapper;
	
	@Resource
	private FreeMarkerConfigurer freeMarkerConfigurer;
	
	@Value("${pageDir}")
	private String pageDir;
	
	@Resource
	private TbItemCatMapper itemCatMapper;
	
	@Resource
	private TbItemMapper itemMapper;
	
	@Override
	public boolean genItemHtml(Long goodsId) {
		Writer out = null;
		try {
			// 获取配置对象
			Configuration configuration = freeMarkerConfigurer.getConfiguration();
			
			// 获取模板对象
			Template template = configuration.getTemplate("item.ftl");
			
			// 创建数据模型
			Map dataModel = new HashMap();
			// 1. 查询SPU信息
			TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
			dataModel.put("goods", goods);
			// 2. 查询SPU详情
			TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
			dataModel.put("goodsDesc", goodsDesc);
			
			// 3. 查询分类
			String category1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
			String category2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
			String category3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
			dataModel.put("category1", category1);
			dataModel.put("category2", category2);
			dataModel.put("category3", category3);
			
			// 4. 读取SKU列表数据
			TbItemExample example = new TbItemExample();
			Criteria criteria = example.createCriteria();
			criteria.andGoodsIdEqualTo(goodsId);
			criteria.andStatusEqualTo("1");// 状态有效
			example.setOrderByClause("is_default desc"); // 按是否默认降序排序，目的是让返回的第一条结果为默认的sku
			List<TbItem> itemList = itemMapper.selectByExample(example);
			dataModel.put("itemList", itemList);
			
			// 创建目标文件的输出流对象
			out = new FileWriter(pageDir + goodsId + ".html");
			
			// 写数据到目标文件
			template.process(dataModel,out);
			
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			if(null != out){
				try {
					out.close();
				} catch (IOException e) {
					System.out.println("关闭流文件失败");
				}
			}
		}
		
		return false;
	}
	
}
