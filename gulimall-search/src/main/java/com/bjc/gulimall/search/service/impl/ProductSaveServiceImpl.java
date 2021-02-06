package com.bjc.gulimall.search.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.bjc.common.to.es.SkuEsModel;
import com.bjc.gulimall.search.config.GulimallElasticSearchConfig;
import com.bjc.gulimall.search.constant.EsConstant;
import com.bjc.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
        // 保存到es
        // 1. 给es建立索引，product，建立好映射关系
        // 2. 批量给es保存数据 BulkRequest bulkRequest, RequestOptions options
        BulkRequest bulkRequest = new BulkRequest();
        skuEsModels.forEach(item -> {
            // 新建Index请求并指定索引
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            // 指定每条数据的唯一id
            indexRequest.id(item.getSkuId().toString());
            // 存储数据，并指定数据类型为json
            indexRequest.source(JSONObject.toJSONString(item), XContentType.JSON);

            bulkRequest.add(indexRequest);
        });

        // 批量操作
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        // TODO 如果有错误记录，做相应的处理
        boolean b = bulk.hasFailures();
        if(b){
            List<String> collect = Arrays.stream(bulk.getItems()).map(item -> item.getId()).collect(Collectors.toList());
            log.error("批量保存数据到es异常！",collect);
        } else {
            log.error("上架完成！");
        }
        return b;
    }
}
