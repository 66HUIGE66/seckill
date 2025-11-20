package com.cbh.seckill.rabbitmq;

import org.springframework.amqp.core.Message;import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;


/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
    接收者
 */
@Service
@Slf4j
public class MQReceiver {
    //可以指定接收的队列
    @RabbitListener(queues = "queue")
    //接收消息
    public void receive(Object msg){
        log.info("接收到消息->{}" , msg);
    }
    //从queue1接收消息
    @RabbitListener(queues = "queue_fanout01")
    public void receive1(Object msg){
        log.info("queue1接收到消息->{}" , msg);
    }
    @RabbitListener(queues = "queue_fanout02")
    public void receive2(Object msg){
        log.info("queue2接收到消息->{}" , msg);
    }
    @RabbitListener(queues = "queue_direct01")
    //从direct交换机接收消息
    public void receive_direct1(Object msg){
        log.info("queue_direct01接收到消息->{}" , msg);
    }
    @RabbitListener(queues = "queue_direct02")
    //从direct交换机接收消息
    public void receive_direct2(Object msg){
        log.info("queue_direct02接收到消息->{}" , msg);
    }
    @RabbitListener(queues = "queue_topic_基尼")
    public void receive_topic01(Object msg){
        log.info("queue_topic_基尼 收到消息->{}" , msg);
    }
    @RabbitListener(queues = "queue_topic_钛镁")
    public void receive_topic02(Object msg){
        log.info("queue_topic_钛镁 收到消息->{}" , msg);
    }
    @RabbitListener(queues = "queue_header01")
    public void receive_header01(Message msg){
        log.info("queue_header01 收到消息->{}" , new String(msg.getBody()));
    }
    @RabbitListener(queues = "queue_header02")
    public void receive_header02(Message msg){

        log.info("queue_header02 收到消息->{}" , new String(msg.getBody()));

    }
}
