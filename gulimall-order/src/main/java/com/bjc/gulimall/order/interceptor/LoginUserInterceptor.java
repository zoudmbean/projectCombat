package com.bjc.gulimall.order.interceptor;

import com.bjc.common.constant.AuthServerConstant;
import com.bjc.common.vo.MemberResVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @描述：订单用户登录拦截器
 * @创建时间: 2021/3/19
 */
@Component
public class LoginUserInterceptor  implements HandlerInterceptor {

    public static ThreadLocal<MemberResVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        AntPathMatcher ant = new AntPathMatcher();
        String uri = request.getRequestURI();
        boolean match = ant.match("/order/order/status/**", uri);
        boolean match1 = ant.match("/payed/notify", uri);

        if(match || match1){ // 如果匹配，直接放行，无需拦截
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
