package com.cbh.seckill.validator;

import org.springframework.util.StringUtils;
import com.cbh.seckill.util.ValidatorUtil;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 */
public class IsMobileValidtor implements ConstraintValidator<IsMobile , String> {
    private boolean required = false;
    @Override
    public void initialize(IsMobile constraintAnnotation) {
        //初始化
        required = constraintAnnotation.required();
    }
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        //必填
        if (required) {
            return ValidatorUtil.isNumber(value);
        } else {//非必填
            if (!StringUtils.hasText(value)) {
                return true;
            } else {
                return ValidatorUtil.isNumber(value);
            }
        }
    }

}
