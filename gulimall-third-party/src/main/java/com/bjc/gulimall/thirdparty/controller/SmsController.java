package com.bjc.gulimall.thirdparty.controller;

import com.bjc.common.utils.R;
import com.bjc.gulimall.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sms")
public class SmsController {

    @Autowired
    SmsComponent smsComponent;

    /**
     * @Description: 提供给别的服务进行调用的
     * @Param: [phone, code]
     * @return: com.bjc.common.utils.R
     * @Author: zoudmBean
     * @Date: 2021/2/26
     */
    @GetMapping("/sendcode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code){
        smsComponent.sendSmsCode(phone,code);
        return R.ok();
    }

}
