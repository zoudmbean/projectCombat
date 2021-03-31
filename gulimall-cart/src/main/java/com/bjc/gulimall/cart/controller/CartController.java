package com.bjc.gulimall.cart.controller;

import com.bjc.gulimall.cart.service.CartService;
import com.bjc.gulimall.cart.vo.Cart;
import com.bjc.gulimall.cart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * @描述：购物车控制层
 * @创建时间: 2021/3/10
 */
@Controller
public class CartController {

    @Autowired
    CartService cartService;

    // 获取当前用户的所有购物项
    @GetMapping("/currentUserCartItems")
    @ResponseBody
    public List<CartItem> getCurrentCartItems(){
        return cartService.getUserCartItems();
    }

    /* 勾选 */
    @GetMapping("/delItem")
    public String delItem(Long skuId){
        cartService.delItem(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /* 勾选 */
    @GetMapping("/checkItem")
    public String checkItem(Long skuId,Integer check){
        cartService.checkItem(skuId,check);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /* 数量 */
    @GetMapping("/countItem")
    public String countItem(Long skuId,Integer num){
        cartService.countItem(skuId,num);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /*
    * 浏览器有一个cookie用于标识用户身份，一个月过期。
    * 如果第一次访问会分配一个临时身份，而且以后每次访问都会带上
    *
    * 登录：session有
    * 没登录：按照cookie里面带来的user-key来做
    * 第一次：如果没有临时身份，新建一个
    * */
    @GetMapping("/cart.html")
    public String cartListPage(Model model){
        // 快速得到用户的信息  因为拦截器共享了一个ThreadLocal，所以为了快速获取信息，直接从ThreadLocal中获取即可
        // UserInfoTo userInfoTo = CartIntercepter.threadLocal.get();

        Cart cart = cartService.getCart();
        model.addAttribute("cart",cart);
        return "cartList";
    }

    /** 添加商品到购物车
     *  RedirectAttributes:
     *      1）方法addFlashAttribute 将数据放入session中，可以在页面取出，但是只能取一次
     *      2）方法addAttribute 数据自动在URL后面拼接链接
     * */
    @GetMapping("/addToCart")
    public String addToCart(Long skuId, Integer num, RedirectAttributes model){
        CartItem cartItem = cartService.addToCart(skuId,num);

        // 会自动将参数拼接到URL后面
        model.addAttribute("skuId",skuId);
        // model.addFlashAttribute("skuId",skuId);
        return "redirect:http://cart.gulimall.com/addToCartSuccess";
    }

    @GetMapping("/addToCartSuccess")
    public String addToCartSuccess(@RequestParam("skuId") Long skuId,Model model){
        // 重定向到成功页面，再次查询购物车数据
        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("item",cartItem);
        return "success";
    }

}
