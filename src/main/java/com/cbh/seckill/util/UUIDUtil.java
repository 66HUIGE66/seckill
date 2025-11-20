package com.cbh.seckill.util;

import java.util.UUID;

/**
 *
 * 生成uuid的工具类
 */
public class UUIDUtil {
    public static String uuid(){
        //去掉randomUUID生成的“-”
        return UUID.randomUUID().toString().replace("-" , "");
    }
}
