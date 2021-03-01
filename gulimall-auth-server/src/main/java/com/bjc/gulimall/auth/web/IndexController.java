package com.bjc.gulimall.auth.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    /*
    * 登录页面跳转
    * */
    //@GetMapping("/login.html")
    public String loginPage(Model model){
        return "login";
    }

    /*
    * 注册页面跳转
    * */
    //@GetMapping("/reg.html")
    public String regPage(Model model){
        return "reg";
    }


}
