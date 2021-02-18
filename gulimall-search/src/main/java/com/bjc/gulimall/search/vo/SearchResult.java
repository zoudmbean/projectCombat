package com.bjc.gulimall.search.vo;

import com.bjc.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;

@Data
public class SearchResult {
    // 查询到的所有商品信息
    private List<SkuEsModel> products;

    /*
    * 分页信息
    * */
    // 当前页码
    private Integer pageNum;
    // 总记录数
    private Long total;
    // 总页码
    private Integer totalPages;

    // 当前查询到的结果，所有涉及到的品牌
    private List<BrandVo> brandVos;

    // 当前查询到的结果，所有涉及到的所有分类
    private List<CatalogVo> catalogs;

    // 当前查询到的结果，所有涉及到的所有属性
    private List<AttrVo> attrs;

    /*
    * 品牌信息封装成内部类
    * */
    @Data
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    /*
     * 属性信息封装成内部类
     * */
    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

    /*
     * 分类信息封装成内部类
     * */
    @Data
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;
    }
}
