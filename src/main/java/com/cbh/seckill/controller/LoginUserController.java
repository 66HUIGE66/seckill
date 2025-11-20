package com.cbh.seckill.controller;

import com.cbh.seckill.serveice.Userservice;
import com.cbh.seckill.vo.LoginVo;
import com.cbh.seckill.vo.RespBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 */
@Controller
@RequestMapping("/login")
@Slf4j
public class LoginUserController {
    @Resource
    private Userservice userservice;
    //编写方法，可以进入登录界面
    @RequestMapping("/toLogin")
    public String toLogin(){
        return "login";
    }
    //处理用户登录请求
    @RequestMapping("/doLogin")
    @ResponseBody
    public RespBean doLogin(@Valid LoginVo loginVo , HttpServletRequest request , HttpServletResponse response){
        log.info("loginVo={}" , loginVo);
        return userservice.doLogin(loginVo , request , response);
    }

}
