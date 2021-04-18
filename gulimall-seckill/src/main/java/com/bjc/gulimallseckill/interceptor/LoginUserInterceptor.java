package com.bjc.gulimallseckill.interceptor;

import com.bjc.common.constant.AuthServerConstant;
import com.bjc.common.vo.MemberResVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @描述：订单用户登录拦截器
 * @创建时间: 2021/3/19
 */
@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberResVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();
        AntPathMatcher ant = new AntPathMatcher();
        boolean match = ant.match("/kill",uri);
        if(!match){     // 只拦截秒杀请求，别的请求都放行
            return true;
        }


        HttpSession session = request.getSession();
        MemberResVo user = (MemberResVo)session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(null != user){
            loginUser.set(user);
            return true;
        }
        session.setAttribute("msg","请先进行登录");
        // 没登录就跳转到登录页面
        response.sendRedirect("http://auth.gulimall.com/login.html");
        return false;
    }
}
