package com.bjc.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import com.bjc.common.validation.AddGroup;
import com.bjc.common.validation.UpdateGroup;
import com.bjc.common.validation.UpdateStatusGroup;
import com.bjc.common.validation.selfvalidator.ListValue;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 *
 * @author zoudm
 * @email zoudmbean@163.com
 * @date 2020-12-08 22:27:02
 */
@Data
@Accessors(chain = true)
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@TableId
	@NotNull(message = "更新id不能为空",groups = UpdateGroup.class)
	@Null(message = "新增id必须为空",groups = AddGroup.class)
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "品牌名必须不能为空",groups = {UpdateGroup.class, AddGroup.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	@URL(message = "logo必须是一个合法的URL地址",groups = {UpdateGroup.class, AddGroup.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@NotNull(groups = {AddGroup.class,UpdateGroup.class})
	@ListValue(vals = {0,1},groups = {UpdateStatusGroup.class,AddGroup.class})
	// @ListIntegerValue(vals = {0,1},message = "状态值不是指定的值！")
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@Pattern(regexp = "^[a-zA-Z]$",message = "检索首字母必须是一个字母")		// 默认注解不能满足需要的时候，可以使用Pattern注解，使用正则表达式来校验
	private String firstLetter;
	/**
	 * 排序
	 */
	@Min(value = 0,message = "排序必须大于等于0")
	private Integer sort;

}
