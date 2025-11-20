package com.cbh.seckill.util;

import org.junit.Test;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 *
 * 完成一些校验工作 ， 如手机号码是否正确
 */
public class ValidatorUtil {
    //校验手机号^[1][3-9][0-9]{9}$ ， ^0?(11|13|14|15|17|18)[0-9]{9}$
    private static final Pattern mobile_pattern = Pattern.compile("^0?(11|13|14|15|17|18)[0-9]{9}$");
    public static boolean isNumber(String num){
        if (!StringUtils.hasText(num)){
            return false;
        }
        Matcher matcher = mobile_pattern.matcher(num);
        return matcher.matches();
    }
    //测试手机号
    @Test
    public void t1(){
        String num = "114514133";
        System.out.println(isNumber(num));
    }
}
