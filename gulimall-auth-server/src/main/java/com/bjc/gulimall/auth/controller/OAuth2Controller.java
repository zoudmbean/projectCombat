package com.bjc.gulimall.auth.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.bjc.common.constant.AuthServerConstant;
import com.bjc.common.utils.R;
import com.bjc.gulimall.auth.feign.MemberFeignService;
import com.bjc.gulimall.auth.utils.HttpUtils;
import com.bjc.common.vo.MemberResVo;
import lombok.extern.java.Log;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @描述：处理社交登录请求的处理器
 * @创建时间: 2021/3/5
 */
@Controller
@Log
public class OAuth2Controller {

    @Autowired
    private MemberFeignService memberFeignService;

    /* 使用code换取access_token */
    @GetMapping("/auth2.0/weibo/success")
    public String weibo(String code, HttpSession session) throws Exception {

        Map<String,String> map = new HashMap<>();
        map.put("client_id","569337071");
        map.put("client_secret","b458d177e886215c4066cbfb14261f0d");
        map.put("grant_type","authorization_code");
        map.put("redirect_uri","http://auth.gulimall.com/auth2.0/weibo/success");
        map.put("code",code);
        // 1. 换取token
        /*
        * public static HttpResponse doPost(String host, String path, String method,
			Map<String, String> headers,
			Map<String, String> querys,
			Map<String, String> bodys)
        *
        * */
        Map<String,String> body = null;
        HttpResponse httpResponse = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", new HashMap<>(), map,body);

        if(httpResponse.getStatusLine().getStatusCode() == 200){
            HttpEntity entity = httpResponse.getEntity();
            String jsonStr = EntityUtils.toString(entity);
            JSONObject jsonObject = JSONObject.parseObject(jsonStr);
            if(jsonObject.isEmpty()){
                return "redirect:http://auth.gulimall.com/login.html";
            }
            // 取出access_token
            String access_token = jsonObject.getString("access_token");
            String uid = jsonObject.getString("uid");
            if(StringUtils.isEmpty(access_token)){
                return "redirect:http://auth.gulimall.com/login.html";
            }

            // 成功，提交到member处理
            R r = memberFeignService.oauthLogin(jsonObject);
            System.out.println(r);
            if(r.getCode() != 0){ // 说明在member服务出问题了，跳转到登录页面
                return "redirect:http://auth.gulimall.com/login.html";
            }
            MemberResVo data = r.getData("data", new TypeReference<MemberResVo>() {});
            log.info("登录成功：用户信息【 " + data + " 】");
            // 保存登录用户到session
            session.setAttribute(AuthServerConstant.LOGIN_USER,data);
            // TODO 默认存的domain是当前系统的域名  需要改成子域名的形式
            // TODO session保存到redis使用JSON序列化
            // 以上两步在sessionConfig配置类中配置即可
        } else {    // 获取失败，重定向到登录页
            return "redirect:http://auth.gulimall.com/login.html";
        }

        // 授权成功，跳转到首页
        return "redirect:http://gulimall.com";
    }

}
