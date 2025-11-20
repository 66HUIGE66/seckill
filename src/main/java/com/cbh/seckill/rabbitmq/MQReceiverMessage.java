package com.cbh.seckill.rabbitmq;

import cn.hutool.json.JSONUtil;
import com.cbh.seckill.pojo.SecKillMessage;
import com.cbh.seckill.pojo.User;
import com.cbh.seckill.serveice.GoodsService;
import com.cbh.seckill.serveice.OrderService;
import com.cbh.seckill.vo.GoodsVo;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 */
@Service
public class MQReceiverMessage {
    @Resource
    private GoodsService goodsService;
    @Resource
    private OrderService orderService;

    @RabbitListener(queues = "seckillQueue")
    public void queue(String message){
        //这里取出的是String，需要的是SecKillMessage,所以需要一个工具类JSONUTIL
        //在hutool依赖里
        SecKillMessage secKillMessage = JSONUtil.toBean(message, SecKillMessage.class);
        //进行秒杀的用户
        User user = secKillMessage.getUser();
        //通过商品id得到对应的GoodsVo
        Long goodsId = secKillMessage.getGoodsId();
        GoodsVo goodsVo = goodsService.findGoodsVoById(goodsId);
        //下单
        orderService.secKill(user , goodsVo);
    }
}
