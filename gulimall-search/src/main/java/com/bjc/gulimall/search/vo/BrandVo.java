package com.bjc.gulimall.search.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.bjc.common.validation.AddGroup;
import com.bjc.common.validation.UpdateGroup;
import com.bjc.common.validation.UpdateStatusGroup;
import com.bjc.common.validation.selfvalidator.ListValue;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

@Data
public class BrandVo {
    private static final long serialVersionUID = 1L;

    /**
     * 品牌id
     */
    private Long brandId;
    /**
     * 品牌名
     */
    private String name;
    /**
     * 品牌logo地址
     */
    private String logo;
    /**
     * 介绍
     */
    private String descript;
    /**
     * 显示状态[0-不显示；1-显示]
     */
    private Integer showStatus;
    /**
     * 检索首字母
     */
    private String firstLetter;
    /**
     * 排序
     */
    private Integer sort;
}
