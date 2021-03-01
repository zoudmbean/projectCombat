package com.bjc.gulimall.auth.feign;

import com.bjc.common.utils.R;
import com.bjc.gulimall.auth.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
public interface MemberFeignService {
    @PostMapping("/member/member/regist")
    public R regist(@RequestBody UserRegistVo userRegist);
}
