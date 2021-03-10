package com.bjc.gulimall.cart.controller;

import com.bjc.common.constant.AuthServerConstant;
import com.bjc.gulimall.cart.intercepter.CartIntercepter;
import com.bjc.gulimall.cart.to.UserInfoTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

/**
 * @描述：购物车控制层
 * @创建时间: 2021/3/10
 */
@Controller
public class CartController {

    /*
    * 浏览器有一个cookie用于标识用户身份，一个月过期。
    * 如果第一次访问会分配一个临时身份，而且以后每次访问都会带上
    *
    * 登录：session有
    * 没登录：按照cookie里面带来的user-key来做
    * 第一次：如果没有临时身份，新建一个
    * */
    @GetMapping("/cart.html")
    public String cartListPage(HttpSession session){
        // 快速得到用户的信息  因为拦截器共享了一个ThreadLocal，所以为了快速获取信息，直接从ThreadLocal中获取即可
        UserInfoTo userInfoTo = CartIntercepter.threadLocal.get();
        System.out.println(userInfoTo);

        return "cartList";
    }

    /** 添加商品到购物车 */
    @GetMapping("/addToCart")
    public String addToCart(){
        return "success";
    }

}
