package com.bjc.gulimall.product.exception;

import com.bjc.common.enums.BizCodeEnume;
import com.bjc.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

//@ControllerAdvice
//@ResponseBody
@RestControllerAdvice(basePackages="com.bjc.gulimall.product.controller")
@Slf4j
public class GulimallExceptionControllerAdvice {

    //处理JSR303数据校验抛出的异常
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e){
        // 打印日志
        log.error("提交的数据不合法",e);
        BindingResult bindingResult = e.getBindingResult();
        Map<String,String> errMap = new HashMap<>();
        bindingResult.getFieldErrors().stream().forEach(item -> {
            errMap.put(item.getField(),item.getDefaultMessage());
        });
        return R.error(BizCodeEnume.VAILD_EXCEPTION.getCode(),BizCodeEnume.VAILD_EXCEPTION.getMsg()).put("data",errMap);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable e){
        log.error("出现异常：",e);
        return R.error(BizCodeEnume.UNKNOW_EXCEPTION.getCode(),BizCodeEnume.UNKNOW_EXCEPTION.getMsg());
    }

}
