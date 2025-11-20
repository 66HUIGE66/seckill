package com.cbh.seckill.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 */
@Configuration
public class RabbitMQTopicConfig {
    //定义队列名 ， 交换机 ，路由
    private static final String QUEUE1 = "queue_topic_基尼";
    private static final String QUEUE2 = "queue_topic_钛镁";
    private static final String EXCHANGE = "topicExchange";
    private static final String ROUTING_KEY01 = "#.篮球.#";
    private static final String ROUTING_KEY02 = "*.篮球.#";

    //创建/配置队列
    @Bean
    public Queue queue_topic1(){
        return new Queue(QUEUE1);
    }
    @Bean
    public Queue queue_topic2(){
        return new Queue(QUEUE2);
    }
    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(EXCHANGE);
    }
    //绑定同时指定路由
    @Bean
    public Binding binding_topic_j(){
        return BindingBuilder.bind(queue_topic1()).to(topicExchange()).with(ROUTING_KEY01);
    }
    @Bean
    public Binding binding_topic_n(){
        return BindingBuilder.bind(queue_topic2()).to(topicExchange()).with(ROUTING_KEY02);
    }
}
