package com.bjc.gulimall.search.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.bjc.common.to.es.SkuEsModel;
import com.bjc.common.utils.R;
import com.bjc.gulimall.search.config.GulimallElasticSearchConfig;
import com.bjc.gulimall.search.constant.EsConstant;
import com.bjc.gulimall.search.feign.ProductServiceFeign;
import com.bjc.gulimall.search.service.MallSearchService;
import com.bjc.gulimall.search.vo.AttrResponseVo;
import com.bjc.gulimall.search.vo.BrandVo;
import com.bjc.gulimall.search.vo.SearchParam;
import com.bjc.gulimall.search.vo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.terms.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.thymeleaf.util.MapUtils;

import javax.servlet.http.HttpServletMapping;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Slf4j
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private ProductServiceFeign productServiceFeign;

    /*
    * 根据检索参数返回检索结果
    * SearchResult 包含页面需要的所有信息
    * */
    @Override
    public SearchResult search(SearchParam searchParam) {

        SearchResult result = null;

        /*
        *   1. 动态构建出查询需要的DSL语句
        * */
        // 1.1 创建检索请求对象
        // SearchRequest searchRequest = new SearchRequest();
        SearchRequest searchRequest = buildSearchRequest(searchParam);
        try {

            /* 2. 执行检索请求 */
            SearchResponse response = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

            /* 3. 分析响应数据，封装成指定的数据格式 */
            result = buildSearchResult(response,searchParam);
        } catch (Exception e) {
            log.error("动态构建出查询需要的DSL语句出错，原因：",e);
        }
        return result;
    }

    /* 根据响应构建返回结果对象 */
    private SearchResult buildSearchResult(SearchResponse response,SearchParam searchParam) {
        SearchResult result = new SearchResult();

        // 获取命中结果
        SearchHits hits = response.getHits();
        // 从名字结果中获取命中记录
        SearchHit[] resultHits = hits.getHits();


        // 1. 封装所有查询到的商品
        List<SkuEsModel> esModels = new ArrayList<>();
        if(ArrayUtils.isNotEmpty(resultHits)){
            for(SearchHit hit : resultHits){
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel esModel = JSONObject.parseObject(sourceAsString, SkuEsModel.class);
                // 设置高亮
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                if(!CollectionUtils.isEmpty(highlightFields)){
                    HighlightField skuTitle = highlightFields.get("skuTitle");
                    String title = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(title);
                }
                esModels.add(esModel);
            }
        }
        result.setProducts(esModels);

        // 获取所有的聚合信息
        Aggregations aggregations = response.getAggregations();
        // 2. 封装当前所有商品涉及到的所有属性信息
        List<SearchResult.AttrVo> attrs = new ArrayList<>();
        // 获取嵌套聚合
        ParsedNested parsedNested = aggregations.get("attrAgg");
        // 获取属性id聚合
        ParsedLongTerms attrIdAgg =  parsedNested.getAggregations().get("attrIdAgg");
        // 遍历属性id聚合，得到每一个属性下的属性值
        attrIdAgg.getBuckets().forEach(item -> {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            // 获取属性id
            String key = item.getKeyAsString();
            attrVo.setAttrId(Long.parseLong(key));

            // 获取属性名称
            ParsedStringTerms attrNameTerms =  item.getAggregations().get("attrNameIdAgg");
            String name = attrNameTerms.getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(name);

            // 获取属性值
            ParsedStringTerms attrValueTerms =  item.getAggregations().get("attrValueAgg");
            List<String> attrValues = attrValueTerms.getBuckets().stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
            attrVo.setAttrValue(attrValues);

            // 将每一个属性对象添加到属性集合
            attrs.add(attrVo);
        });
        result.setAttrs(attrs);

        // 3. 封装当前商品所涉及到的品牌信息
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brandAgg = aggregations.get("brand_agg");
        brandAgg.getBuckets().forEach(bucket -> {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            // 获取品牌id
            String key = bucket.getKeyAsString();
            brandVo.setBrandId(Long.parseLong(key));

            // 获取品牌名称
            ParsedStringTerms stringTerms =  bucket.getAggregations().get("brand_name_agg");
            String name = stringTerms.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(name);

            // 获取品牌图片
            ParsedStringTerms imgTerms =  bucket.getAggregations().get("brand_img_agg");
            String img = imgTerms.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(img);

            brandVos.add(brandVo);
        });
        result.setBrandVos(brandVos);

        // 4. 封装当前商品所属的分类
        List<SearchResult.CatalogVo> catalogs = new ArrayList<>();
        // 获取分类聚合信息（因为分类id是long类型的，所以用Aggregations接口的实现类ParsedLongTerms）
        ParsedLongTerms catalogAgg = aggregations.get("catalogAgg");
        List<? extends Terms.Bucket> buckets = catalogAgg.getBuckets();
        buckets.forEach(bucket -> {
            SearchResult.CatalogVo cVo = new SearchResult.CatalogVo();
            // bucket的key就是分类的id
            String key = bucket.getKeyAsString();
            cVo.setCatalogId(Long.parseLong(key));

            // 获取分类名称  分类的名称是分类id的子聚合
            ParsedStringTerms nameAgg = bucket.getAggregations().get("catalogNameAgg");
            String name = nameAgg.getBuckets().get(0).getKeyAsString();
            cVo.setCatalogName(name);

            catalogs.add(cVo);
        });
        result.setCatalogs(catalogs);

        // 5. 设置分页信息
        long total = hits.getTotalHits().value;
        //  5.1 页码
        result.setPageNum(searchParam.getPageNum());
        //  5.2 总记录数
        result.setTotal(total);
        //  5.3 总页码
        int pages = (int)(total/EsConstant.PRODUCT_PAGESIZE + (total%EsConstant.PRODUCT_PAGESIZE > 0 ? 1 : 0));
        result.setTotalPages(pages);

        // 6. 封装面包屑导航
        List<String> searchAttrs = searchParam.getAttrs();
        if(!CollectionUtils.isEmpty(searchAttrs)){
            List<SearchResult.NavVo> collect = searchAttrs.stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                // attr 1_白色:黑色:绿色
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R info = productServiceFeign.attrInfo(Long.parseLong(s[0]));
                result.getAttrIds().add(Long.parseLong(s[0]));
                if(info.getCode() == 0){
                    AttrResponseVo rVo = info.getData("attr",new TypeReference<AttrResponseVo>(){});
                    String attrName = rVo.getAttrName();
                    navVo.setNavName(attrName);
                } else {    // 远程调用失败了，给一个默认值
                    navVo.setNavName(s[0]);
                }

                // 取消了面包屑之后，跳转到的link
                ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
                HttpServletRequest request = servletRequestAttributes.getRequest();
                String queryString = request.getQueryString();
                String endQueryString = replaceQueryStr(attr, queryString,"attrs");
                // StringBuffer requestURL = request.getRequestURL();
                navVo.setLink("http://search.gulimall.com/list.html?" + endQueryString);
                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(collect);
        }
        // 品牌 分类添加面包屑导航
        if(!CollectionUtils.isEmpty(searchParam.getBrandId())){
            List<SearchResult.NavVo> navs = result.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("品牌");

            // 取消了面包屑之后，跳转到的link
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = servletRequestAttributes.getRequest();
            String queryString = request.getQueryString();

            // TODO 远程查询
            R r = productServiceFeign.brandsInfos(searchParam.getBrandId());
            if(r.getCode() == 0){
                List<BrandVo> brands = r.getData("brand", new TypeReference<List<BrandVo>>(){});
                StringBuffer buffer = new StringBuffer();
                String endQueryString = "";
                for (BrandVo brand : brands) {
                    buffer.append(brand.getName()+";");
                    endQueryString = replaceQueryStr(brand.getBrandId()+"",queryString,"brandId");
                }
                navVo.setLink("http://search.gulimall.com/list.html?" + endQueryString);
                navVo.setNavValue(buffer.toString());
            }
            navs.add(navVo);
        }

        // TODO 分类 （不需要导航取消）


        return result;
    }

    // 请求参数替换
    // 参数：
    //      1. attr：要编码的字符串
    //      2. queryString：请求字符串
    //      3. key：请求字符串的关键字
    private String replaceQueryStr(String needEncodeStr, String queryString,String key) {
        // 编码
        String encode = "";
        try {
            encode = URLEncoder.encode(needEncodeStr, "UTF-8");
            // 浏览器将空格解析成20%  但是java将空格解析成+  因此这里需要做特殊处理
            encode = encode.replace("+","20%");
        } catch (UnsupportedEncodingException e) {
            log.error("URL编码失败：",e);
        }
        return queryString.replace("&"+key+"=" + encode, "")
                .replace("?"+key+"=" + encode, "")
                .replace(key+"=" + encode, "");
    }

    /* 准备检索请求
    *       关键字模糊匹配、过滤（按照属性、分类、品牌、价格区间），排序，分页，高亮，聚合分析
    * */
    private SearchRequest buildSearchRequest(SearchParam searchParam) {

        // 用于构建DSL语句
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        /*
        * 1. 查询部分DSL构建
        * */
        // 1.1 构建queryBuilder对象。复杂的query是通过bool组合检索的，因此需要构建BoolQueryBuilder对象
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 1.2 构建全文检索条件must，关键字模糊匹配.  如果页面有关键字搜索，才进行全文模糊检索
        if(StringUtils.isNotEmpty(searchParam.getKeyword())){
            // 将按照skuTitle全文检索的条件封装到boolQuery中
            boolQuery.must(QueryBuilders.matchQuery("skuTitle",searchParam.getKeyword()));
        }

        // 1.3 构建过滤filter条件
        //      1.3.1 按照三级分类id过滤
        if(null != searchParam.getCatalog3Id()){
            // 三级分类id是精确匹配，用term
            boolQuery.filter(QueryBuilders.termQuery("catalogId",searchParam.getCatalog3Id()));
        }
        //      1.3.2 按照品牌ID过滤（支持多选）
        if(!CollectionUtils.isEmpty(searchParam.getBrandId())){
            boolQuery.filter(QueryBuilders.termsQuery("brandId",searchParam.getBrandId()));
        }


        //      1.3.3 按照所有指定的属性进行查询
        /*
        * {
          "nested": {
            "path": "attrs",
            "query": {
              "bool": {
                "must": [
                  {
                    "term": {
                      "attrs.attrId": {
                        "value": "15"
                      }
                    }
                  },
                  {
                    "terms": {
                      "attrs.attrValue": [
                        "aaa",
                        "白色",
                        "OCE-AN10"
                      ]
                    }
                  }
                ]
              }
            }
          }
        }
        * */
        if(!CollectionUtils.isEmpty(searchParam.getAttrs())){
            searchParam.getAttrs().forEach(attrStr -> {
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                // attrs=1_5寸:8寸&attrs=2_8G:16G
                String[] s = attrStr.split("_");
                // 检索的属性ID
                String attrId = s[0];
                // 检索的属性ID对应的属性值的数组
                String[] attrValues = s[1].split(":");
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId",attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue",attrValues));

                // 每一个属性都需要生成一个嵌入式查询
                // ScoreMode.None 表示不参与评分
                NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQueryBuilder);
            });
        }
        //      1.3.4 按照库存是否有进行查询
        boolQuery.filter(QueryBuilders.termQuery("hasStock",searchParam.getHasStock() == 1));
        //      1.3.5 按照价格区间进行查询
        if(StringUtils.isNotEmpty(searchParam.getSkuPrice())){
            // 1_500/_500/500_
            /*
            * "range": {
                  "skuPrice": {
                    "gte": 0.0,
                    "lte": 20000.0
                  }
                }
            * */
            int g = 0;
            int l = 0;
            // 1）构建rangeQueryBuilder对象
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("skuPrice");
            // 2）解析查询参数
            String[] s = searchParam.getSkuPrice().split("_");
            if(s.length == 2){
                rangeQueryBuilder.gte(s[0]).lte(s[1]);
            } else if(s.length == 1){ // 否则就是单值
                if(searchParam.getSkuPrice().startsWith("_")){
                    rangeQueryBuilder.lte(s[0]);
                } else {
                    rangeQueryBuilder.gte(s[0]);
                }
            }
            boolQuery.filter(rangeQueryBuilder);
        }

        // 将所有的查询条件封装到sourceBuilder
        sourceBuilder.query(boolQuery);

        /*
        * 2. 排序、分页、高亮
        * */
        // 2.1 排序
        if(StringUtils.isNotEmpty(searchParam.getSort())){
            String sort = searchParam.getSort(); // &sort = hotScore_asc/desc
            String[] s = sort.split("_");
            // SortOrder sortOrder = s[1].equalsIgnoreCase("asc")?SortOrder.ASC:SortOrder.DESC;
            sourceBuilder.sort(s[0], SortOrder.fromString(s[1]));
        }
        // 2.2 分页  from = (当前页-1) * 每页显示条数
        sourceBuilder.from((searchParam.getPageNum()-1) * EsConstant.PRODUCT_PAGESIZE)  // 第几页
                    .size(EsConstant.PRODUCT_PAGESIZE);                                 // 每页显示记录数
        // 2.3 高亮 只有关键字查询才高亮
        if(StringUtils.isNotEmpty(searchParam.getKeyword())){
            HighlightBuilder highlight = new HighlightBuilder();
            // 指定需要高亮的字段是哪个
            highlight.field("skuTitle");
            // 指定高亮前置标签
            highlight.preTags("<b style='color:red'>");
            // 指定后缀标签
            highlight.postTags("</b>");
            sourceBuilder.highlighter(highlight);
        }

        /*
        * 3. 聚合分析
        * */
        // 3.1 品牌聚合
        AggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg")    // 参数为聚合的名称
                                                    .field("brandId")               // 要聚合的字段
                                                    .size(50);                       // 查询并显示多少条记录
        //      3.1.1 子聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg")
                                                .field("brandName").size(1));
        //      3.1.2 子聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg")
                                                .field("brandImg").size(1));
        //      3.1.3 添加聚合条件到sourceBuilder
        sourceBuilder.aggregation(brand_agg);

        // 3.2 分类聚合
        AggregationBuilder catalog_agg = AggregationBuilders.terms("catalogAgg").field("catalogId").size(50);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalogNameAgg").field("catalogName").size(1));
        sourceBuilder.aggregation(catalog_agg);

        // 3.3 属性聚合(嵌入式聚合)
        AggregationBuilder attrAgg = AggregationBuilders.nested("attrAgg", "attrs");

        //      3.3.1 聚合出当前所有的AttrId
        AggregationBuilder attrIdAggregation = AggregationBuilders.terms("attrIdAgg").field("attrs.attrId").size(100);
        //      3.3.2 聚合分析出当前attrId对应的名称
        attrIdAggregation.subAggregation(AggregationBuilders.terms("attrNameIdAgg").field("attrs.attrName").size(1));
        //      3.3.3 聚合分析出当前attrId对应的所有可能的属性值attrValue
        attrIdAggregation.subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue").size(50));
        //      3.3.4 将attrId子聚合添加到嵌入式聚合attrAgg中
        attrAgg.subAggregation(attrIdAggregation);
        //      3.3.4 将嵌入式聚合attrAgg聚合条件添加到sourceBuilder中
        sourceBuilder.aggregation(attrAgg);

        // System.out.println("构建的DSL：" + sourceBuilder.toString());
        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);
        return searchRequest;
    }
}
