package com.cbh.seckill.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 */
@Configuration
public class RabbitQMHeaderConfig {
    private static final String QUEUE01 = "queue_header01";
    private static final String QUEUE02 = "queue_header02";
    private static final String EXCHANEG = "headerExchange";

    @Bean
    public Queue queue_header01() {
        return new Queue(QUEUE01);
    }

    @Bean
    public Queue queue_header02() {
        return new Queue(QUEUE02);
    }

    @Bean
    public HeadersExchange headersExchange() {
        return new HeadersExchange(EXCHANEG);
    }

    //完成绑定 ， 同时声明要匹配的k-v
    @Bean
    public Binding binding_header01_any() {
        //先声明k-v ， 因为有多个使用放入map中
        HashMap<String, Object> map = new HashMap<>();
        map.put("偶像练习生", "cxk");
        map.put("技能", "ctrl");
        return BindingBuilder.bind(queue_header01()).to(headersExchange()).whereAny(map).match();
    }

    @Bean
    public Binding binding_header02_all() {
        //先声明k-v ， 因为有多个使用放入map中
        HashMap<String, Object> map = new HashMap<>();
        map.put("偶像练习生", "cxk");
        map.put("技能", "ctrl");
        return BindingBuilder.bind(queue_header02()).to(headersExchange()).whereAll(map).match();
    }


}
