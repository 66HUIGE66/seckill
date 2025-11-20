package com.cbh.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 */
@Getter
@ToString
@AllArgsConstructor
public enum RespBeanEnum {
    //通用
    SUCCESS(200,"SUCCESS"),
    ERROR(500,"服务器异常"),
    //登录
    LOGIN_ERROR(500210 , "用户id火密码错误"),
    MOBILE_ERROR(500211 , "手机号格式不正确"),
    MOBILE_NOT_FIND(500212 , "手机号不存在"),
    BIND_ERROR(500213 , "参数绑定异常"),
    PASSWORD_ERROR(500233 , "密码更新失败"),
    //秒杀
    ENTRY_STOCK(500500 , "库存不足"),
    ENTRY_STOCK_LIMIT(500501 , "该商品限购一件"),
    REQUEST_ILLEGAL(500502, "请求非法"),
    SESSION_ERROR(500503, "用户信息有误"),
    SEK_KILL_WAIT(500504 , "排队中"),
    CAPTCHA_ERROR(500505 , "验证码错误"),
    ACCESS_LIMIT_REACHED(500506 , "访问频繁"),
    SEC_KILL_RETRY(500507 , "本次抢购失败，请重新抢购"),
    SECKILL_ERROR(114514 , "抢购失败");

    private final Integer code;
    private final String message;
}
