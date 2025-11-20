package com.cbh.seckill.config;

import com.cbh.seckill.pojo.User;
import com.cbh.seckill.serveice.Userservice;
import com.cbh.seckill.util.CookieUtil;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 * UserArgumentResolver自定义解析器
 */

@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {
    @Resource
    private Userservice userservice;
    //判断当前要解析的参数类型是否是需要的
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> aClass = parameter.getParameterType();
        return aClass == User.class;
    }
    //如果上面返回为true ， 则执行下面的方法
    //怎么处理可以自己写
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        // 优先从ThreadLocal获取(性能优化)
        User user = UserContext.getUser();
        if (user != null) {
            return user;
        }

        // 从Cookie+Redis获取(兜底逻辑)
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        String ticket = CookieUtil.getCookieValue(request, "userTicket");
        if (StringUtils.hasText(ticket)) {
            user = userservice.getUserByCookie(ticket, request, response);
            if (user != null) {
                UserContext.setUser(user); // 缓存到ThreadLocal
                return user;
            }
        }
        return null;
//        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
//        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
//        //从cookie获取
//        String ticket = CookieUtil.getCookieValue(request, "userTicket");
//        if (!StringUtils.hasText(ticket)){
//            return null;
//        }
//        //从Redis获取user
//        User user = userservice.getUserByCookie(ticket, request, response);
//        return user;
        //从Threadlocal获取user
//        return UserContext.getUser();
    }
}
