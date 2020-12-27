package com.bjc.common.validation.selfvalidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/*
* 自定义校验注解
*
* */
@Documented
@Constraint(validatedBy = {ListValueConstraintValidatorForInteger.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ListValue {
    /*
    * 校验注解中，必须要有message  groups  payload 三个属性 可以直接拷贝JDK提供的校验注解中提供的
    * */
    String message() default "{javax.validation.constraints.ListValue.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /*
    * 需要指定的值
    * */
    int[] vals() default {};
}
