package com.bjc.common.validation.selfvalidator;

import org.apache.commons.lang.ArrayUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 自定义Integer类型指定数值校验注解校验器
 * */
public class ListValueConstraintValidatorForInteger implements ConstraintValidator<ListValue,Integer> {

    Set<Integer> set = new HashSet<>();

    /*
    * 初始化方法，用于获取自定义注解的详细信息
    * */
    @Override
    public void initialize(ListValue constraintAnnotation) {
        int[] vals = constraintAnnotation.vals();
        if(ArrayUtils.isNotEmpty(vals)){
            Arrays.stream(vals).forEach(set::add);
        }
    }

    /*
    * 校验值是否符合要求
    * */
    @Override
    public boolean isValid(Integer val, ConstraintValidatorContext constraintValidatorContext) {
        return set.contains(val);
    }
}
