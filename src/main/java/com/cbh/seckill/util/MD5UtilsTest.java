package com.cbh.seckill.util;

import org.junit.Test;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 */
public class MD5UtilsTest {
    @Test
    public void t1(){
        String s = "12345";
        System.out.println(Md5Util.inputPassToMidPass(s));
        System.out.println(Md5Util.midPassToDBPass(Md5Util.inputPassToMidPass(s) , "d7a1c9HH"));
        System.out.println(Md5Util.inputPassToDBPass(s , "hOmo1123"));
    }
}
