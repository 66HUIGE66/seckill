package com.cbh.seckill.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 *
 *
 * RabbitMQSecKillConfig创建消息队列和交换机
 */
@Configuration
public class RabbitMQSecKillConfig {
    private static final String QUEUE = "seckillQueue";
    private static final String EXCHANGE = "seckillExchange";

    //创建队列
    @Bean
    public Queue queue_seckill(){
        return new Queue(QUEUE);
    }
    @Bean
    public TopicExchange topicExchange_seckill(){
        return new TopicExchange(EXCHANGE);
    }
    //绑定并指定路由
    @Bean
    public Binding binding_seckill(){
        return BindingBuilder.bind(queue_seckill()).to(topicExchange_seckill()).with("seckill.#");
    }
}
