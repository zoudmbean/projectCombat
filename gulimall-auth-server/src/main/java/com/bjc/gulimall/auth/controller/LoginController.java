package com.bjc.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.bjc.common.constant.AuthServerConstant;
import com.bjc.common.enums.BizCodeEnume;
import com.bjc.common.utils.R;
import com.bjc.gulimall.auth.feign.MemberFeignService;
import com.bjc.gulimall.auth.feign.ThirdPartFeignService;
import com.bjc.gulimall.auth.vo.UserRegistVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @描述：登录控制器
 * @创建时间: 2021/2/26
 */
@Controller
public class LoginController {

    @Autowired
    private ThirdPartFeignService thirdPartFeignService;

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @ResponseBody
    @GetMapping("/login/sendcode")
    public R sendCode(@RequestParam("phone") String phone){
        // TODO 1 接口防刷
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if(StringUtils.isNotEmpty(redisCode)){
            long time = Long.parseLong(redisCode.split("_")[1]);
            long now = System.currentTimeMillis();
            if(now - time < 60000){  // 如果时间间隔小于60秒  直接返回
                return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(),BizCodeEnume.SMS_CODE_EXCEPTION.getMsg());
            }
        }


        // 生成验证码
        String code = "";
        Random r = new Random();
        for(int i = 0 ; i < 6 ; i++){
            code += r.nextInt(10) + "";
        }
        // 将验证码加上当前时间戳
        code = code + "_" + System.currentTimeMillis();
        // 将验证码存入redis缓存  有效期5分钟
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone,code,5, TimeUnit.MINUTES);
        thirdPartFeignService.sendCode(phone,code.split("_")[0]);
        return R.ok();
    }

    /**
    * @Description:  注册成功页面跳转
    * @Param:
    * @return:
    * @Author: zoudmBean
    * @Date: 2021/2/27
     *
     *
     * @Valid 开启JSR303数据校验
     * BindingResult result  用于接收校验结果信息
     *
     * 注意：Model的数据重定向的时候会丢失，因此在MVC中提供了另一个数据模型RedirectAttributes,当需要重定向的时候，可以使用RedirectAttributes携带数据
     *      是利用session原理，将数据放到session中，重定向到新页面的时候，再将数据取出来
    */
    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo user, BindingResult result, RedirectAttributes model){
        Map<String, String> errMap = new HashMap<>();
        if(result.hasErrors()){
            /*Map<String,String> errMap = new HashMap<>();
            result.getFieldErrors().stream().map(fieldError -> {
                String errorField = fieldError.getField();
                String message = fieldError.getDefaultMessage();
                errMap.put(errorField,message);
            });*/

            // Collectors.toMap 可以直接指定以什么为key，什么为value 第三个参数表示当出现了重复key的数据时，会回调这个方法，可以在这个方法里处理重复Key数据问题，这里直接使用本次的数据
            errMap = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage,(entity1, entity2) -> entity2));
            // model.addAttribute("errors",errMap);
            // 添加一个一闪而过的属性  也就是只使用一次
            model.addFlashAttribute("errors",errMap);
            // 校验出错，转发到注册页
            return "redirect:http://auth.gulimall.com/reg.html";
        }

        // 1. 校验验证码
        String code = user.getCode();
        if(StringUtils.isNotEmpty(code)){
            String redisCode = code.split("_")[0];
            if(!StringUtils.equalsIgnoreCase(code,redisCode)){
                errMap.put("code","验证码已错误！");
                model.addFlashAttribute("errors",errMap);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
            // 2. 校验没问题，实现注册流程
            //  2.1 删除缓存的code
            redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX+user.getPhone());
            //  2.2 调用远程服务，注册
            R regist = memberFeignService.regist(user);
            if(regist.getCode() != 0){
                errMap.put("msg",regist.getData(new TypeReference<String>(){}));
                model.addFlashAttribute("errors",errMap);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        } else {
            errMap.put("code","验证码已过期，请重新获取");
            model.addFlashAttribute("errors",errMap);
            return "redirect:http://auth.gulimall.com/reg.html";
        }

        // 注册成功，返回登录页面
        return "redirect:http://auth.gulimall.com/login.html";
    }
}
