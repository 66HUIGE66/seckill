package com.cbh.seckill.config;

import com.cbh.seckill.pojo.User;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 */
public class UserContext {
    //每个数据都有自己的ThreadLocal ， 把共享数据放在这里, 保证线程安全
    private static ThreadLocal<User> userThreadLocal = new ThreadLocal<>();
    public static void setUser(User user){
        userThreadLocal.set(user);
    }
    public static User getUser(){
        return userThreadLocal.get();
    }


}
