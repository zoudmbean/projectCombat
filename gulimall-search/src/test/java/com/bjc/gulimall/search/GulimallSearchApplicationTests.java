package com.bjc.gulimall.search;

import com.alibaba.fastjson.JSONObject;
import com.bjc.gulimall.search.config.GulimallElasticSearchConfig;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
class GulimallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

@Test
public void conditionTest() throws IOException {
    // 1. 构建检索请求
    SearchRequest searchRequest = new SearchRequest();
    // 2. 指定检索索引
    searchRequest.indices("bank");

    // 3.1 指定DSL内容
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    // 3.1.1 构建query条件
    searchSourceBuilder.query(QueryBuilders.matchQuery("address","mill"));  // 匹配address包含mill的雇员
    // 3.1.2 构建聚合条件
    // 按照age的值分布聚合，并给该次统计取一个名字aggAge
    TermsAggregationBuilder aggAge = AggregationBuilders.terms("aggAge").field("age").size(10);
    searchSourceBuilder.aggregation(aggAge);
    // 计算平均薪资
    AvgAggregationBuilder banlanceAvg = AggregationBuilders.avg("banlanceAvg").field("balance");
    searchSourceBuilder.aggregation(banlanceAvg);

    // 3. 创建检索条件 用于构建DSL语言
    searchRequest.source(searchSourceBuilder);

    // 4. 执行检索
    SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

    // 5. 分析结果
    // 5.1 获取所有查到的记录
    SearchHits hits = searchResponse.getHits();
    // 5.2 获取所有命中的记录
    SearchHit[] searchHits = hits.getHits();
    for (SearchHit hit : searchHits) {
        System.out.println(hit.getId());
        System.out.println(hit.getSourceAsMap());
    }

    // 5.3 获取分析（聚合）信息
    Aggregations aggregations = searchResponse.getAggregations();
    Terms terms = aggregations.get("aggAge");
    terms.getBuckets().forEach(item -> System.out.println("年龄："+ item.getKeyAsString() + "  total：" + item.getDocCount()));

    Avg blanceAvg = aggregations.get("banlanceAvg");
    System.out.println("平均薪资：" + blanceAvg.getValue());
}

    @Test
    public void getTest() throws IOException {
        GetRequest request = new GetRequest("users","1");
        GetResponse getResponse = client.get(request, GulimallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(getResponse);
    }

    @Test
    public void indexTest() throws IOException {
        // users 索引名称
        IndexRequest request = new IndexRequest("users");
        request.id("2");    // 设置数据id  如果不指定id值，就默认自增
        // 组装要保存的数据
        Map<String,String> map = new HashMap<>();
        map.put("name","李四");
        map.put("age","18");
        map.put("gender","F");
        map.put("addr","中国");

        // 要保存的内容并指定内容类型
        request.source(JSONObject.toJSONString(map), XContentType.JSON);
        // 使用客户端执行索引保存
        IndexResponse indexResponse = client.index(request, GulimallElasticSearchConfig.COMMON_OPTIONS);

        // 提取响应信息
        System.out.println(indexResponse);
    }

}
