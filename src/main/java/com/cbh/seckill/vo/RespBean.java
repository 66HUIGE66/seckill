package com.cbh.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RespBean {
    private long code;
    private String message;
    private Object object;
    //成功后返回带数据
    public static RespBean success(Object object){
        return new RespBean(RespBeanEnum.SUCCESS.getCode() , RespBeanEnum.SUCCESS.getMessage() , object);
    }
    //不带数据
    public static RespBean success(){
        return new RespBean(RespBeanEnum.SUCCESS.getCode() , RespBeanEnum.SUCCESS.getMessage() , null);
    }
    //失败不带数据
    public static RespBean error(RespBeanEnum respBeanEnum){
        return new RespBean(respBeanEnum.getCode(), respBeanEnum.getMessage(),  null);
    }
    //失败带数据
    public static RespBean error(RespBeanEnum respBeanEnum , Object object){
        return new RespBean(respBeanEnum.getCode(), respBeanEnum.getMessage(),  object);
    }
}
