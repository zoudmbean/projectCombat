package com.bjc.gulimall.search.service;

import com.bjc.gulimall.search.vo.SearchParam;
import com.bjc.gulimall.search.vo.SearchResult;

public interface MallSearchService {
    SearchResult search(SearchParam searchParam);

}
