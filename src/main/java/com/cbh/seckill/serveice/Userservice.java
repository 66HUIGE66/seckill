package com.cbh.seckill.serveice;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cbh.seckill.pojo.User;
import com.cbh.seckill.vo.LoginVo;
import com.cbh.seckill.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 */
public interface Userservice extends IService<User> {
    //完成登录校验
    RespBean doLogin(LoginVo loginVo , HttpServletRequest request , HttpServletResponse response);
    //工具cookie(票据)到redis获取user
    User getUserByCookie(String userTicket , HttpServletRequest request , HttpServletResponse response);
    //更新密码
    RespBean updatePassword(String userTicket , HttpServletRequest request ,HttpServletResponse response,String password);
}
