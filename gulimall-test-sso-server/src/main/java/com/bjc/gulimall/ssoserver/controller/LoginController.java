package com.bjc.gulimall.ssoserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * @描述：登录
 * @创建时间: 2021/3/7
 */
@Controller
public class LoginController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/infoByToken")
    @ResponseBody
    public String getUserinfoByToken(String token){
        String username = redisTemplate.opsForValue().get(token);
        return username;
    }

    @PostMapping("/doLogin")
    public String doLogin(String userName, String pwd, String rUrl, HttpServletResponse response){
        if(!StringUtils.isEmpty(userName) && !StringUtils.isEmpty(pwd) && !StringUtils.isEmpty(rUrl)){
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            // 将令牌保存到redis
            redisTemplate.opsForValue().set(uuid,userName);
            // 将令牌写入到cookie
            Cookie cookie = new Cookie("oss_token", uuid);
            response.addCookie(cookie);
            // 登录成功，跳回到之前的页面
            return "redirect:"+rUrl+"?token="+uuid;
        }
        // 登录失败，跳转到登录页
        return "login";
    }

    @GetMapping("/login.html")
    public String toLoginPage(Model model, @RequestParam(value = "redirect_url",required = false) String url, @CookieValue(value = "oss_token",required = false) String oss_token){
        if(!StringUtils.isEmpty(oss_token)){
            return "redirect:"+url+"?token="+oss_token;
        }
        if(!StringUtils.isEmpty(url)){
            model.addAttribute("redirect_url",url);
        }
        return "login";
    }

}
