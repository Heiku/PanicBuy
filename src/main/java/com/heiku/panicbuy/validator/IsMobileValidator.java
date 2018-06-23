package com.heiku.panicbuy.validator;

import com.heiku.panicbuy.util.ValidatorUtil;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IsMobileValidator implements ConstraintValidator<IsMobile, String> {

    // 注解是否必须
    private boolean required = false;

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {

        if (required){
            return ValidatorUtil.isMobile(s);
        }else {
            if (StringUtils.isEmpty(s)){
                return true;
            }else {
                return ValidatorUtil.isMobile(s);
            }
        }
    }

    @Override
    public void initialize(IsMobile constraintAnnotation) {
        required = constraintAnnotation.required();
    }
}
