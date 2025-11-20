package com.cbh.seckill.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 */
@Service
@Slf4j
public class MQSender {
    @Resource
    private RabbitTemplate rabbitTemplate;
    //装配RabbitTemplate 操作rabbitmq
    public void send(Object ob){
      log.info("发送消息->{}" , ob);
      rabbitTemplate.convertAndSend("queue" , ob);
    }
    //发送消息到交换机
    //fanout策略需要忽略routingKey
    public void sendFanout(Object msg){
        log.info("发送消息->{}" , msg);
        rabbitTemplate.convertAndSend("fanoutExchange" , "" ,msg);
    }
    //发送消息到direct交换机
    public void sendDiret1(Object msg){
        log.info("发送消息1->{}" , msg);
        rabbitTemplate.convertAndSend("queue_directExchage" , "queue.red" , msg);
    }
    public void sendDiret2(Object msg){
        log.info("发送消息2->{}" , msg);
        rabbitTemplate.convertAndSend("queue_directExchage" , "queue.bule" , msg);
    }
    //发送消息到 topicExchange
    public void senTopic1(Object msg){
        log.info("发送消息topic1->{}" , msg);
        rabbitTemplate.convertAndSend("topicExchange" , "篮球.rap" , msg);
    }
    public void senTopic2(Object msg){
        log.info("发送消息topic2->{}" , msg);
        rabbitTemplate.convertAndSend("topicExchange" , "唱.篮球.#" , msg);
    }
    //发送消息到header交换机 , 同时携带k-v
    public void sendHeaderExchange01(String msg){
        log.info("发送消息--->{}" , msg);
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("偶像练习生" , "cxk");
        messageProperties.setHeader("技能" , "铁山靠");
        //创建Message对象
        Message message = new Message(msg.getBytes(), messageProperties);
        rabbitTemplate.convertAndSend("headerExchange" , "" , message);
    }
    public void sendHeaderExchange02(String msg){
        log.info("发送消息--->{}" , msg);
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("偶像练习生" , "cxk");
        messageProperties.setHeader("技能" , "铁山靠");
        //创建Message对象
        Message message = new Message(msg.getBytes(), messageProperties);
        rabbitTemplate.convertAndSend("headerExchange" , "" , message);
    }
}
