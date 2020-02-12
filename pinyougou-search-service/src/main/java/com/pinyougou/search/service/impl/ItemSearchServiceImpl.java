package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.search.service.ItemSearchService;


/**
 * Solr搜索服务层
 * @author Administrator
 *
 */
@Service(timeout=50000)
public class ItemSearchServiceImpl implements ItemSearchService{

	@Resource
	private SolrTemplate solrTemplate;
	
	/* 搜索功能  主函数
	 * 注意：参数searchMap  我们定义好格式为["keywords",""]
	 */
	@Override
	public Map<String, Object> search(Map searchMap) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		// 关键字空格处理
		if(null == searchMap.get("keywords")){
			return map;
		}
		String keyWords = (String) searchMap.get("keywords");
		searchMap.put("keywords", keyWords.replaceAll(" ", ""));
		
		// 1. 查询商品列表
		map.putAll(searchList(searchMap));
		
		// 2. 分组查询商品分类列表
		List<String> categoryGroupList = searchCategoryList(searchMap);
		map.put("categoryList", categoryGroupList);
		
		// 3. 根据商品分类名称查询品牌和规格
		if(!StringUtils.isEmpty(searchMap.get("category"))){  // 当用户有选择分类的时候，根据选择的分类查询
			
			map.putAll(searchBrandAndSpecList((String)searchMap.get("category")));
			
		} else { // 否则 查询第一个分类
			
			if(!CollectionUtils.isEmpty(categoryGroupList)){
				// 默认显示第一个商品分类下的品牌和规格
				map.putAll(searchBrandAndSpecList(categoryGroupList.get(0)));
			}
			
		}
		
		return map;
	}
	
	@Resource
	private RedisTemplate redisTemplate;
	
	/**
	 * 根据商品分类名称查询品牌和规格
	 * @param category
	 * @return
	 */
	private Map searchBrandAndSpecList(String category){
		Map map = new HashMap();
		// 根据商品分类名称得到模板ID
		Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
		if(null != typeId){
			// 1. 根据模板id查询品牌列表
			List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
			map.put("brandList", brandList);
			// 2. 根据模板id查询规格列表
			List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
			map.put("specList", specList);
		}
		return map;
	}
	
	/**分组查询
	 * @param searchMap
	 * @return
	 */
	private List<String> searchCategoryList(Map searchMap) {
		
		List<String> list = new ArrayList<>();
		
		// 创建查询对象
		Query query = new SimpleQuery("*:*");
		
		// 创建一个 item_keywords 域的条件
		Criteria criteria = new Criteria("item_keywords");
		// 设置搜索条件
		criteria.is(searchMap.get("keywords"));
		
		/*
		 * 下面开始设置分组
		 * */
		GroupOptions options = new GroupOptions();
		options.addGroupByField("item_category");
		
		query.setGroupOptions(options);
		
		// 添加查询条件到查询对象
		query.addCriteria(criteria );
		
		// 执行分组查询 得到分组页对象
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query , TbItem.class);
		
		// 1. 获取分组结果对象 
		/*
		 * 为什么有了分组页对象还有一个分组结果对象了？是因为在addGroupByField的时候，可以加入多个分组域，所以分组页对象中包含了所有设置的分组域的分组结果对象
		 * 而分组结果对象就是根据上面设置的某一个分组域来获取的分组结果对象。通俗的讲就是一个分组页包含多个分组结果
		 * 
		 * */
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		
		// 2. 获取分组入口页对象 
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		
		// 3. 获取分组入口集合
		List<GroupEntry<TbItem>> entryList = groupEntries.getContent();
		for(GroupEntry<TbItem> entry : entryList){
			String groupValue = entry.getGroupValue();
			list.add(groupValue);
		}
		return list;
	}

	/**
	 * @param searchMap：查询条件map
	 * @param filter：过滤的类型名称 例如：category
	 * @param filed：过滤的域名	例如：item_category
	 * @param query  查询对象
	 */
	private void addFilter(Map searchMap,String filter,String filed,HighlightQuery query){
		if(!"".equals(searchMap.get(filter))){
			FilterQuery filterQuery = new SimpleFilterQuery();
			Criteria filterCriteria = new Criteria(filed).is(searchMap.get(filter));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery );
		}
	}
	
	/**
	 * 查询列表
	 * @param searchMap
	 * @return
	 */
	private Map searchList(Map searchMap) {
		
		Map<String, Object> rtnMap = new HashMap<String, Object>();
		
		// 设置高亮选项
		HighlightQuery query = new SimpleHighlightQuery();
		HighlightOptions options = new HighlightOptions();
		options.addField("item_title");
		options.setSimplePrefix("<em style='color:red'>");
		options.setSimplePostfix("</em>");
		query.setHighlightOptions(options ); // 将高亮设置到查询对象上
		
		// 1.1 关键字查询     查询条件  注意：这里因为搜索的关键字可能是品牌，可能是标题，还可能是分类，所以用复制域
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		
		
		// 1.2 按照商品分类过滤  选择了分类才进行筛选
		addFilter(searchMap,"category","item_category",query);
		/*if(!"".equals(searchMap.get("category"))){
			FilterQuery filterQuery = new SimpleFilterQuery();
			Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery );
		}*/
		// 1.3 按照品牌来过滤
		addFilter(searchMap,"brand","item_brand",query);
		
		// 1.4 按照规格过滤
		Map<String, Object> specMap = (Map<String, Object>) searchMap.get("spec");
		// key 就是机身内存  网络制式这样的key
		for(String key : specMap.keySet()){
			addFilter(specMap,key,"item_spec_"+key,query);
		}
		
		// 1.5 按照价格过滤
		String priceStr = (String) searchMap.get("price");
		if(!StringUtils.isEmpty(priceStr)){
			String[] priceArr = priceStr.split("-");
			Criteria priceCriteria = new Criteria("item_price");
			if("*".equals(priceArr[1])){
				priceCriteria.greaterThanEqual(priceArr[0]);
			} else {
				priceCriteria.between(priceArr[0], priceArr[1], true,true);
			}
			FilterQuery filterQuery = new SimpleFilterQuery(priceCriteria);
			query.addFilterQuery(filterQuery );
		}
		
		// 1.6 分页
		Integer pageNo = (Integer) searchMap.get("pageNo");
		Integer pageSize = (Integer) searchMap.get("pageSize");
		if(null == pageNo){
			pageNo = 1;
		}
		if(null == pageSize){
			pageSize = 30;
		}
		query.setOffset((pageNo-1)*pageSize);   // 设置起始索引
		query.setRows(pageSize);				// 设置每页记录数
		
		// 1.7 排序
		String sort = (String) searchMap.get("sort");
		String sortField = (String) searchMap.get("sortField");
		if(!StringUtils.isEmpty(sort)){
			Sort s = null;
			if("ASC".equals(sort)){
				s = new Sort(Sort.Direction.ASC, "item_"+sortField);  // 升序
			}else {
				s = new Sort(Sort.Direction.DESC, "item_"+sortField); // 降序
			}
			query.addSort(s);
		}
		
		
		
		// *********************   获取高亮结果集
		// 查询获取高亮对象
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query , TbItem.class);
		// 获取高亮入口集合
		List<HighlightEntry<TbItem>> entryList = page.getHighlighted();
		for(HighlightEntry<TbItem> entry : entryList){
			// 获取每个入口的高亮列表
			List<Highlight> highlights = entry.getHighlights();
			
			// 获取实体对象
			TbItem entity = entry.getEntity();
			
			// getEveryEntry(highlights, entity);
			if(!CollectionUtils.isEmpty(highlights) && !CollectionUtils.isEmpty(highlights.get(0).getSnipplets())){
				String title = highlights.get(0).getSnipplets().get(0);
				entity.setTitle(title);
			}
			
		}
		// 查询数据集合
		rtnMap.put("rows", page.getContent());
		rtnMap.put("totalPages", page.getTotalPages());		// 总页数
		rtnMap.put("totales", page.getTotalElements());		// 总记录数
		
		return rtnMap;
	}

	/**设置每一个高亮列表的每一个域的高亮小片
	 * @param highlights
	 * @param entity
	 */
	private void getEveryEntry(List<Highlight> highlights, TbItem entity) {
		// 循环遍历每个入口的高亮列表
		for(Highlight h : highlights){
			// 得到高亮“小片”集合
			List<String> sns = h.getSnipplets();
			for(String str : sns){
				entity.setTitle(str);
				System.out.println(sns);
			}
		}
	}

	@Override
	public void importList(List<TbItem> list) {
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
	}

	/*@Override
	public void deleteByGoodsIds(List goodsIdList) {
		SolrDataQuery query = new SimpleQuery("*:*");
		Criteria criteria = new Criteria("item_goodsid");
		criteria.in(goodsIdList);
		query.addCriteria(criteria );
		solrTemplate.delete(query );
		solrTemplate.commit();
	}*/
	
	@Override
	public void deleteByGoodsIds(List goodsIdList) {
		for(int i = 0 ; i < goodsIdList.size(); i++){
			Long idL = (Long)goodsIdList.get(i);
			solrTemplate.deleteById(idL + "");
		}
		solrTemplate.commit();
	}
	
}
