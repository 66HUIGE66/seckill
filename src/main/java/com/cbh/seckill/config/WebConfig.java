package com.cbh.seckill.config;

import com.cbh.seckill.interceptor.AccessLimitInterceptor;
import org.springframework.stereotype.Controller;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.util.List;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 */
@Controller
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private AccessLimitInterceptor accessLimitInterceptor;
    @Resource
    private UserArgumentResolver userArgumentResolver;

    //装配自定义拦截器,这样才会生效
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accessLimitInterceptor);
    }

    //静态资源加载
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }
    //将自定义解析器加入到HandelMethodArgumentResolver才能生效
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userArgumentResolver);
    }
}
