package com.pinyougou.mapper;

import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface TbBrandMapper {
    int countByExample(TbBrandExample example);

    int deleteByExample(TbBrandExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TbBrand record);

    int insertSelective(TbBrand record);

    List<TbBrand> selectByExample(TbBrandExample example);

    TbBrand selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TbBrand record, @Param("example") TbBrandExample example);

    int updateByExample(@Param("record") TbBrand record, @Param("example") TbBrandExample example);

    int updateByPrimaryKeySelective(TbBrand record);

    int updateByPrimaryKey(TbBrand record);

	/**根据id批量删除
	 * @param ids
	 */
	void delByIds(@Param("ids") Long[] ids);
	
	/**
	 * 查询品牌数据
	 * @return
	 */
	List<Map<Long, String>> selectOptions();
}