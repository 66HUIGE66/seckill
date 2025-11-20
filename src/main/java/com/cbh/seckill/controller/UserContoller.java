package com.cbh.seckill.controller;

import com.cbh.seckill.pojo.User;
import com.cbh.seckill.serveice.Userservice;
import com.cbh.seckill.vo.RespBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 */
@RequestMapping("/user")
@Controller
public class UserContoller {
    @Resource
    private Userservice userservice;
    //返回登录用户信息
    //同时携带请求参数addresss
    @RequestMapping("/info")
    @ResponseBody
    public RespBean info(User user /*, String address*/){

        return new RespBean().success(user);
    }
    @RequestMapping("/updatepwd")
    @ResponseBody
    public RespBean updatePassword(String userTicket , String password , HttpServletRequest request , HttpServletResponse response){
        return userservice.updatePassword(userTicket, request, response, password);
    }
}
