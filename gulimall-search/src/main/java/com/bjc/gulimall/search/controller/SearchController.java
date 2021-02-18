package com.bjc.gulimall.search.controller;

import com.bjc.gulimall.search.service.MallSearchService;
import com.bjc.gulimall.search.vo.SearchParam;
import com.bjc.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SearchController {

    @Autowired
    private MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(Model model, SearchParam searchParam){

        // 根据页面传递来的参数，去es中检索商品信息
        SearchResult searchResult = mallSearchService.search(searchParam);
        model.addAttribute("result",searchResult);
        return "list";
    }

}
