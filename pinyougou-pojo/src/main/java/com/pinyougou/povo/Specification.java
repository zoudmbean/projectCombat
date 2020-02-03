package com.pinyougou.povo;

import java.io.Serializable;
import java.util.List;

import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationOption;

/**
 * 规格组合实体bean
 * @author Administrator
 *
 */
public class Specification implements Serializable{
	private TbSpecification specification;
	private List<TbSpecificationOption> specificationOptionalList;
	
	public TbSpecification getSpecification() {
		return specification;
	}
	public void setSpecification(TbSpecification specification) {
		this.specification = specification;
	}
	public List<TbSpecificationOption> getSpecificationOptionalList() {
		return specificationOptionalList;
	}
	public void setSpecificationOptionalList(List<TbSpecificationOption> specificationOptionalList) {
		this.specificationOptionalList = specificationOptionalList;
	}
	
}
