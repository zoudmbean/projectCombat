package com.pinyougou.content.service.impl;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.pojo.TbContentExample;
import com.pinyougou.pojo.TbContentExample.Criteria;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;
	
	@Resource
	private RedisTemplate redisTemplate;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		contentMapper.insert(content);	
		try {
			redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		} catch (Exception e) {
			System.out.println("redis出错了！");
		}
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
		try {
			// 删除之前的分类缓存
			TbContent selectByPrimaryKey = contentMapper.selectByPrimaryKey(content.getId());
			redisTemplate.boundHashOps("content").delete(selectByPrimaryKey.getCategoryId());
		} catch (Exception e) {
			System.out.println("redis出错了！");
		}
		
		contentMapper.updateByPrimaryKey(content);
		
		try {
			redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		} catch (Exception e) {
			System.out.println("redis出错了！");
		}
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			try {
				TbContent content = contentMapper.selectByPrimaryKey(id);
				redisTemplate.boundHashOps("content").delete(content.getCategoryId());
			} catch (Exception e) {
				System.out.println("redis出错了！");
			}
			// 删除广告
			contentMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content!=null){			
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}
			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}
			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}
			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}
	
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/* 根据分类ID查询广告
	 */
	@Override
	public List<TbContent> findByCategoryId(Long categoryId) {
		
		List<TbContent> list = null;
		
		try {
			list = (List<TbContent>) redisTemplate.boundHashOps("content").get(categoryId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(null == list){
			TbContentExample example = new TbContentExample();
			Criteria createCriteria = example.createCriteria();
			createCriteria.andCategoryIdEqualTo(categoryId);
			// 设置排序字段
			example.setOrderByClause("sort_order");
			list = contentMapper.selectByExample(example );
			
			try {
				// 将查询的数据存入缓存中
				redisTemplate.boundHashOps("content").put(categoryId, list);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			System.out.println("数据来自数据库");
		} else {
			System.out.println("数据来自缓存。。。");
		}
		
		return list;
	}
}
