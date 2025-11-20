package com.cbh.seckill.validator;

import org.springframework.validation.annotation.Validated;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.Valid;
import java.lang.annotation.*;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 * IsMobile:自定义校验注解
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {IsMobileValidtor.class})
public @interface IsMobile {
    String message() default "手机号格式错误";
    boolean required() default true;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default{};
}
