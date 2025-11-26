package com.cbh.seckill.serveice.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cbh.seckill.exception.GlobalException;
import com.cbh.seckill.mapper.UserMapper;
import com.cbh.seckill.pojo.User;
import com.cbh.seckill.serveice.Userservice;
import com.cbh.seckill.util.CookieUtil;
import com.cbh.seckill.util.Md5Util;
import com.cbh.seckill.util.UUIDUtil;

import com.cbh.seckill.vo.LoginVo;
import com.cbh.seckill.vo.RespBean;
import com.cbh.seckill.vo.RespBeanEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper , User> implements Userservice {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UserMapper userMapper;
    @Override
    public RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
        //判断手机号和密码是否为空
//        if (!StringUtils.hasText(mobile) || !StringUtils.hasText(password)){
//            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
//        }
//        //判断手机号是否合法
//        if (!ValidatorUtil.isNumber(mobile)){
//            return RespBean.error(RespBeanEnum.MOBILE_ERROR);
//        }
        //查询db，查看用户是否存在
        User user = userMapper.selectById(mobile);
        if (null == user){
            //return RespBean.error(RespBeanEnum.LOGIN_ERROR);
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        //如果存在则需要进行密码对比
        //从loginVo取出的密码为中间密码
        if (!Md5Util.midPassToDBPass(password , user.getSlat()).equals(user.getPassword())){
            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        }
        //登陆成功, 生成票据ticket(每个用户唯一)
        String ticket = UUIDUtil.uuid();
        //保存到sesion
        //request.getSession().setAttribute(ticket , user);
        //保存到Redis ， 实现分布式Session
        log.info("user->{}" , redisTemplate.opsForValue().get("user:"+ticket));
        redisTemplate.opsForValue().set("user:" + ticket , user);
        log.info("user->{}" , redisTemplate.opsForValue().get("user:"+ticket));
        //将ticket保存到cookie
        CookieUtil.setCookie(request , response , "userTicket" , ticket);
        return RespBean.success(ticket);
    }

    @Override
    public User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response) {
        if (!StringUtils.hasText(userTicket)) {
            return null;
        }
        User user = (User) redisTemplate.opsForValue().get("user:" + userTicket);
        //如果用户不为空就重新设置cookie ,防止过期
        if (user != null){
            CookieUtil.setCookie(request, response ,"userTicket" , userTicket);
        }
        return user;
    }

    @Override
    public RespBean updatePassword(String userTicket , HttpServletRequest request ,HttpServletResponse response , String password) {
        User user = getUserByCookie(userTicket, request, response);
        if (user == null){
            throw new GlobalException(RespBeanEnum.MOBILE_NOT_FIND);
        }
        //更新密码
        user.setPassword(Md5Util.inputPassToDBPass(password , user.getSlat()));
        int i = userMapper.updateById(user);
        if (i == 1){//更新成功
            //删除用户在Redis的数据
            redisTemplate.delete("user:" + userTicket);
            return RespBean.success();
        }
        return RespBean.error(RespBeanEnum.PASSWORD_ERROR);
    }

}
