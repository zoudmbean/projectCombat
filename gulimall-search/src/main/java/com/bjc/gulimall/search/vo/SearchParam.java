package com.bjc.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/*
* 封装检索条件
* */
@Data
public class SearchParam {
    private String keyword;     // 页面传递过来的全文匹配关键字
    private Long catalog3Id;    // 三级分类id

    private String sort;        // 排序条件  按照综合、价格、热度等

    /*
    * 过滤条件
    * */
    private Integer hasStock;   // 是否只显示有货
    private String skuPrice;    // 价格区间
    private List<Long> brandId; // 品牌
    private List<String> attrs; // 按照属性筛选  条件格式：属性id_属性值1:属性值2:属性值3

    /*
    * 分页数据
    * */
    private Integer pageNum;    // 页码

}
