package com.bjc.gulimall.auth.feign;

import com.alibaba.fastjson.JSONObject;
import com.bjc.common.utils.R;
import com.bjc.gulimall.auth.vo.UserLoginVo;
import com.bjc.gulimall.auth.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
public interface MemberFeignService {
    @PostMapping("/member/member/regist")
    public R regist(@RequestBody UserRegistVo userRegist);

    @PostMapping("/member/member/login")
    public R login(@RequestBody UserLoginVo userLoginVo);

    /* 社交登录/注册 */
    @PostMapping("/member/member/oauth2/login")
    public R oauthLogin(@RequestBody JSONObject jsonObject);
}
