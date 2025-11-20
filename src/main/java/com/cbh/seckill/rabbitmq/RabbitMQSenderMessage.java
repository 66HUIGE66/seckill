package com.cbh.seckill.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 */
@Slf4j
@Service
public class RabbitMQSenderMessage {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String message){
        log.info("发送消息-->{}" , message );
        rabbitTemplate.convertAndSend("seckillExchange" , "seckill.e" ,message);
    }

}
