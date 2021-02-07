package com.bjc.gulimall.product.web;

import com.bjc.gulimall.product.entity.CategoryEntity;
import com.bjc.gulimall.product.service.CategoryService;
import com.bjc.gulimall.product.vo.Category2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){

        // 1. 查询所有的一级分类
        List<CategoryEntity> list =  categoryService.getLevel1Categorys();
        model.addAttribute("categorys",list);
        // 默认前缀后缀都要，所以只需要写文件名，视图解析器就会自动进行拼串
        return "index";
    }

    @GetMapping("/index/catalog.json")
    @ResponseBody
    public Map<String, List<Category2Vo>> getCatagoryJson(){
        Map<String, List<Category2Vo>> map = categoryService.getCatagoryJson();
        return map;
    }

}
