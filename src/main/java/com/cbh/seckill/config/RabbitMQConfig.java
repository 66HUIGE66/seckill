package com.cbh.seckill.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 */
@Configuration
public class RabbitMQConfig {
    //定义队列名
    private static final String QUEUE = "queue";
    //fanout (广播)
    private static final String QUEUE1 = "queue_fanout01";
    private static final String QUEUE2 = "queue_fanout02";
    private static final String EXCHANGE = "fanoutExchange";
    //direct (路由)
    private static final String QUEUE_DIRECT1 = "queue_direct01";
    private static final String QUEUE_DIRECT2 = "queue_direct02";
    private static final String EXCHANGE_DIRECT = "queue_directExchage";
    //路由
    private static final String ROUTEING_KEY01 = "queue.red";
    private static final String ROUTEING_KEY02 = "queue.bule";

    /**
     *  1. 配置队列
     * * 2. 队列名为 queue
     *  * 3. true 表示: 持久化(默认为true)
     * * durable： 队列是否持久化。 队列默认是存放到内存中的，rabbitmq重启则丢失，
     * * 若想重启之后还存在则队列要持久化，
     * * 保存到Erlang自带的Mnesia数据库中，当rabbitmq重启之后会读取该数据库
     * * @return
     */
    @Bean
    public Queue queue(){
        return new Queue(QUEUE , true);
    }
    @Bean
    public Queue queue1(){
        return new Queue(QUEUE1);
    }
    @Bean
    public Queue queue2(){
        return new Queue(QUEUE2);
    }
    //创建交换机
    @Bean
    public FanoutExchange exchange(){
        return new FanoutExchange(EXCHANGE);
    }
    @Bean
    //将队列与交换机绑定
    public Binding binding1(){
        return BindingBuilder.bind(queue1()).to(exchange());
    }
    @Bean

    public Binding binding2(){
        return BindingBuilder.bind(queue2()).to(exchange());
    }
    //direct
    @Bean
    public Queue queue_direct1(){
        return new Queue(QUEUE_DIRECT1);
    }
    @Bean
    public Queue queue_direct2(){
        return new Queue(QUEUE_DIRECT2);
    }
    //配置direct 交换机
    @Bean
    public DirectExchange direct_exchange(){
        return new DirectExchange(EXCHANGE_DIRECT);
    }
    //绑定direct交换机
    @Bean
    public Binding binding_direct_queue1(){
        return BindingBuilder.bind(queue_direct1()).to(direct_exchange()).with(ROUTEING_KEY01);
    }
    @Bean
    public Binding binding_direct_quque2(){
        return BindingBuilder.bind(queue_direct2()).to(direct_exchange()).with(ROUTEING_KEY02);
    }


}
