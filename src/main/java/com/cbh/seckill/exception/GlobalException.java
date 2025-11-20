package com.cbh.seckill.exception;

import com.cbh.seckill.vo.RespBeanEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ControllerAdvice
public class GlobalException extends RuntimeException{
    private RespBeanEnum respBeanEnum;

}
