package com.bjc.gulimall.cart.intercepter;

import com.bjc.common.constant.AuthServerConstant;
import com.bjc.common.constant.CartConstant;
import com.bjc.common.vo.MemberResVo;
import com.bjc.gulimall.cart.to.UserInfoTo;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.UUID;

/**
 * @描述：购物车拦截器
 * @创建时间: 2021/3/10
 *
 * 功能；在执行目标方法之前，判断用户的登录状态，是临时用户还是登录用户
 */
public class CartIntercepter implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    /* 目标方法执行之前拦截 */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        UserInfoTo user = new UserInfoTo();

        MemberResVo loginUser = (MemberResVo)session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(null != loginUser){ // 用户登录
            user.setUserid(loginUser.getId());
        }

        // 获取cookie数据
        Cookie[] cookies = request.getCookies();
        if(null != cookies && cookies.length > 0){
            Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equalsIgnoreCase(CartConstant.TEMP_USER_COOKIE_NAME))
                    .forEach(cookie -> {
                        user.setUserKey(cookie.getValue());
                        user.setTempUser(true);
                    });
        }

        // 如果没有临时用户，就创建一个user-key
        if(StringUtils.isEmpty(user.getUserKey())){
            String uuid = UUID.randomUUID().toString();
            user.setUserKey(uuid);
        }

        // 目标方法执行之前，将用户信息封装到threadLocal
        threadLocal.set(user);
        return true;
    }

    /** 业务执行之后干什么  这里让浏览器保存一个cookie  */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = threadLocal.get();

        // 不是临时用户，才需要添加cookie
        if(!userInfoTo.isTempUser()){
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME,userInfoTo.getUserKey());
            cookie.setDomain("gulimall.com");
            cookie.setMaxAge(CartConstant.EXPRE_TIME_OUT);
            response.addCookie(cookie);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
