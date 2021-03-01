package com.bjc.gulimall.thirdparty.component;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class SmsComponent {

    public void sendSmsCode(String phone,String code){
        System.out.println("***************************************");
        System.out.println("***************************************");
        System.out.println("***************************************");
        System.out.println("********* 验证码是【" + code + "】   *********");
        System.out.println("***************************************");
        System.out.println("***************************************");
        System.out.println("***************************************");
    }

    public void sendSmsCode1(String phone,String code){
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", "<accessKeyId>", "<accessSecret>");
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        // 手机号
        request.putQueryParameter("PhoneNumbers", phone);
        // 签名
        request.putQueryParameter("SignName", code);
        // 模板名称
        request.putQueryParameter("TemplateCode", "1");
        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

}
