package com.heiku.panicbuy.util;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidatorUtil {

    // 正则校验手机号
    private static final Pattern mobile_pattern = Pattern.compile("1\\d{10}");

    public static boolean isMobile(String src){
        if (StringUtils.isEmpty(src)){
            return false;
        }

        Matcher m = mobile_pattern.matcher(src);
        return m.matches();
    }


    public static void main(String[] args) {

        System.out.println(ValidatorUtil.isMobile("13022851873"));
        System.out.println(ValidatorUtil.isMobile("23026866478"));
    }
}
