package com.bjc.gulimall.ssoclient.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @描述：测试
 * @创建时间: 2021/3/7
 */
@Controller
public class HelloController {

    @Value("${sso.server.url}")
    private String ssoServerUrl;

    /*
    * 无需登录就可以访问
    * */
    @GetMapping("/hello")
    @ResponseBody
    public String hello(){
        return "hello";
    }

    @GetMapping("/boss")
    public String emp(Model model, HttpSession session,@RequestParam(value = "token",required = false) String token){
        if(null != token && !"".equalsIgnoreCase(token)){
            RestTemplate template = new RestTemplate();
            ResponseEntity<String> forEntity = template.getForEntity("http://sso.com:8600/infoByToken?token="+token, String.class);
            String body = forEntity.getBody();
            session.setAttribute("loginUser",body);
        }
        Object loginUser = session.getAttribute("loginUser");
        if(null == loginUser){ // 没有登录
            // 跳转到登录服务器
            return "redirect:"+ssoServerUrl+"?redirect_url=http://client2.com:8701/boss";
        }
        List<String> list = Stream.of("张三", "李四", "王五", "赵六", "钱七", "王八").collect(Collectors.toList());
        model.addAttribute("emps",list);
        return "list";
    }
}
