package com.cbh.seckill.vo;

import com.cbh.seckill.validator.IsMobile;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 */
@Data
public class LoginVo {
    @NotNull
    @IsMobile
    private String mobile;
    @NotNull
    @Length(min = 32)
    private String password;
}
