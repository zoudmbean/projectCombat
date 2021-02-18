package com.bjc.gulimall.search.service.impl;

import com.bjc.gulimall.search.service.MallSearchService;
import com.bjc.gulimall.search.vo.SearchParam;
import com.bjc.gulimall.search.vo.SearchResult;
import org.springframework.stereotype.Service;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    /*
    * 根据检索参数返回检索结果
    * SearchResult 包含页面需要的所有信息
    * */
    @Override
    public SearchResult search(SearchParam searchParam) {
        return null;
    }
}
