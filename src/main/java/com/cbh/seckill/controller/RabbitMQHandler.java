package com.cbh.seckill.controller;

import com.cbh.seckill.rabbitmq.MQSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 */
@Controller
public class RabbitMQHandler {
    //抓配MQSender
    @Resource
    private MQSender mqSender;
    //调用消息生产者
    @RequestMapping("/mq")
    @ResponseBody
    public void mq(){
        mqSender.send("hhhh");
    }
    //发送到交换机
    @RequestMapping("/mq/exchage")
    @ResponseBody
    public void setMqSender(){
        mqSender.sendFanout("okkkkkk");
    }
    //发送到direct交换机
    @RequestMapping("/mq/directExchage01")
    @ResponseBody
    public void sendDirectExchange1(){
        mqSender.sendDiret1("hello , queue1");
    }
    @RequestMapping("/mq/directExchage02")
    @ResponseBody
    public void sendDirectExchange2(){
        mqSender.sendDiret2("hello , queue2");
    }
    @RequestMapping("/mq/topic01")
    @ResponseBody
    public void sendTopicExchange1(){
        mqSender.senTopic1("cxk");
    }
    @RequestMapping("/mq/topic02")
    @ResponseBody
    public void sendTopicExchange2(){
        mqSender.senTopic2("cxk");
    }
    @RequestMapping("/mq/header01")
    @ResponseBody
    public void sendTopicHeader01(){
        mqSender.sendHeaderExchange01("cxk");
    }
    @RequestMapping("/mq/header02")
    @ResponseBody
    public void sendTopicHeader02(){
        mqSender.sendHeaderExchange02("cxk");
    }
}
