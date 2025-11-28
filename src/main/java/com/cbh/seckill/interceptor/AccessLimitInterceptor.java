package com.cbh.seckill.interceptor;

import com.cbh.seckill.config.AccessLimit;
import com.cbh.seckill.config.UserContext;
import com.cbh.seckill.pojo.User;
import com.cbh.seckill.serveice.Userservice;
import com.cbh.seckill.util.CookieUtil;
import com.cbh.seckill.vo.RespBean;
import com.cbh.seckill.vo.RespBeanEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;
import org.springframework.web.method.HandlerMethod;
/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 */
@Component
public class AccessLimitInterceptor implements HandlerInterceptor {
    @Resource
    private Userservice userservice;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod){
            //这里获取到登录的user对象
            User user = getUser(request, response);
            //存入到Threadlocal
            UserContext.setUser(user);
            //把handler转成HandlerMethod
            HandlerMethod hm = (HandlerMethod) handler;
            //获取到目标方法的注解
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if (accessLimit == null){//如果目标方法没有@AccessLimit注解表示该接口没有处理限流防刷的业务
                return true;
            }
            //获取注解的属性值
            int second = accessLimit.second();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();
            if (needLogin){//该用户必须登录才能访问目标方法/接口
                if (user == null){
                    render(response , RespBeanEnum.SESSION_ERROR);
                    return false; //表示用户没有登录
                }
            }

            String uri = request.getRequestURI();
            String key = uri + ":" + user.getId();
            ValueOperations ops = redisTemplate.opsForValue();
            Integer cnt = (Integer)ops.get(key);
            if (cnt == null){//第一次访问
                ops.set(key , 1 ,second , TimeUnit.SECONDS);
            } else if (cnt < maxCount){
                //            ops.increment(key);
                ops.set(key , cnt + 1 , second , TimeUnit.SECONDS);
            } else { //说明用户在刷接口
                render(response , RespBeanEnum.ACCESS_LIMIT_REACHED);
                return false;
            }
        }
        return true;
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.setUser(null);
    }
    //构建返回对象-以流对象进行返回
    private void render(HttpServletResponse response , RespBeanEnum respBeanEnum) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        PrintWriter writer = response.getWriter();
        //构建RespBean
        RespBean error = RespBean.error(respBeanEnum);
        writer.write(new ObjectMapper().writeValueAsString(error));
        writer.flush();
        writer.close();
    }
    //单独编写方法得到登录的user对象-userTicket
    private User getUser(HttpServletRequest request , HttpServletResponse response){
        String userTicket = CookieUtil.getCookieValue(request, "userTicket");
        if (!StringUtils.hasText(userTicket)){
            return null;//说明用户没有登录 ， 直接返回null
        }
        return userservice.getUserByCookie(userTicket , request , response);
    }
}
