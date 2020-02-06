package com.pinyougou.sellergoods.service.impl;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.mapper.TbSellerMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbGoodsExample;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.povo.Goods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service(timeout=100000)
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	
	@Resource
	private TbGoodsDescMapper goodsDescMapper; 
	
	@Resource
	private TbItemMapper itemMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page= (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}
	
	@Resource
	private TbItemCatMapper itemCateMapper;
	
	@Resource
	private TbBrandMapper brandMapper;
	
	@Resource
	private TbSellerMapper sellerMapper;
	
	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		// 保存商品
		TbGoods tGoods = goods.getGoods();
		tGoods.setAuditStatus("0");//设置未申请状态
		goodsMapper.insert(tGoods);	
		
		// 保存商品详情
		TbGoodsDesc goodsDesc = goods.getGoodsDesc();
		goodsDesc.setGoodsId(tGoods.getId());
		goodsDescMapper.insert(goodsDesc);
		
		if("1".equals(tGoods.getIsEnableSpec())){ // 如果启用规则
			// 保存sku信息
			List<TbItem> itemList = goods.getItemList();
			for(TbItem item : itemList){
				// 1. 构建标题：SPU名称 + 规格选项值
				String title = tGoods.getGoodsName();
				Map<String,Object> map = (Map<String, Object>) JSONObject.parse(item.getSpec()) ;
				for(String key : map.keySet()){
					title += " " + map.get(key);
				}
				item.setTitle(title);
				
				setItemInfo(goods, item);
				
				// 保存到数据库
				itemMapper.insert(item);
			}
		} else {  // 如果不启用
			
			TbItem item = new TbItem();
			// 构建标题
			item.setTitle(tGoods.getGoodsName());
			item.setPrice( goods.getGoods().getPrice() );//价格			
			item.setStatus("1");//状态
			item.setIsDefault("1");//是否默认			
			item.setNum(99999);//库存数量
			item.setSpec("{}");		

			setItemInfo(goods, item);
			
			// 保存到数据库
			itemMapper.insert(item);
		}
	}

	private void setItemInfo(Goods goods, TbItem item) {
		TbGoodsDesc goodsDesc = goods.getGoodsDesc();
		TbGoods tGoods = goods.getGoods();
		// 存图片  第一张
		List<Map> imgs = JSONArray.parseArray(goodsDesc.getItemImages(), Map.class);
		if(!CollectionUtils.isEmpty(imgs)){
			String url = (String) imgs.get(0).get("url");
			item.setImage(url);
		}
		
		// 商品三级分类
		item.setCategoryid(tGoods.getCategory3Id());
		
		// 创建日期和更新日期
		item.setCreateTime(new Date());
		item.setUpdateTime(new Date());
		
		// goodID 与 sellID(商家id)
		item.setGoodsId(tGoods.getId());
		item.setSellerId(tGoods.getSellerId());
		
		// 分类名称（为了solor搜索方便）
		TbItemCat itemCate = itemCateMapper.selectByPrimaryKey(tGoods.getCategory3Id());
		item.setCategory(itemCate.getName());
		
		// 品牌
		TbBrand brand = brandMapper.selectByPrimaryKey(tGoods.getBrandId());
		item.setBrand(brand.getName());
		
		// 商家店铺名称
		TbSeller seller = sellerMapper.selectByPrimaryKey(tGoods.getSellerId());
		item.setSeller(seller.getNickName());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbGoods goods){
		goodsMapper.updateByPrimaryKey(goods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbGoods findOne(Long id){
		return goodsMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			goodsMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		
		if(goods!=null){			
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}
	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
}
