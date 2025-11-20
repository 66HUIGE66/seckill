package com.cbh.seckill.util;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 * md5工具类
 */
public class Md5Util {
    public static String md5(String s){
        String md5Hex = DigestUtils.md5Hex(s);
        return md5Hex;
    }
    //准备salt
    private static final String SALT = "114515Homo";
    //加密加盐md5(password明文+SALT)
    public static String inputPassToMidPass(String inputPass){
        String str = SALT.substring(0 , 6) + inputPass + SALT.substring(6,SALT.length());
        return md5(str);
    }
    //加密加盐吧MidPass转成DB中的密码
    public static String midPassToDBPass(String midPass , String salt){
        String str = salt.charAt(1) + midPass + salt.charAt(4);
        return md5(str);
    }
    //将明文转为DB的password
    public static String inputPassToDBPass(String s , String salt){
        String midPass = inputPassToMidPass(s);
        String pass = midPassToDBPass(midPass, salt);
        return pass;
    }
}
