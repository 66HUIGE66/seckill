package com.cbh.seckill.rabbitmq;

import cn.hutool.json.JSONUtil;
import com.cbh.seckill.pojo.SecKillMessage;
import com.cbh.seckill.pojo.User;
import com.cbh.seckill.pojo.SeckillOrder;
import com.cbh.seckill.serveice.GoodsService;
import com.cbh.seckill.serveice.OrderService;
import com.cbh.seckill.serveice.SeckillOrderService;
import com.cbh.seckill.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 */
@Slf4j
@Service
public class MQReceiverMessage {
    @Resource
    private GoodsService goodsService;
    @Resource
    private OrderService orderService;
    @Resource
    private SeckillOrderService seckillOrderService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @RabbitListener(queues = "seckillQueue")
    public void queue(String message){
        //这里取出的是String，需要的是SecKillMessage,所以需要一个工具类JSONUTIL
        //在hutool依赖里
        SecKillMessage secKillMessage = JSONUtil.toBean(message, SecKillMessage.class);
        //进行秒杀的用户
        User user = secKillMessage.getUser();
        //通过商品id得到对应的GoodsVo
        Long goodsId = secKillMessage.getGoodsId();
        SeckillOrder cache = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (cache != null) {
            log.warn("用户 {} 对商品 {} 重复下单", user.getId(), goodsId);
            return;
        }
        // 优化：去掉数据库查询，依靠数据库唯一索引保证不重复下单
        // SeckillOrder exist = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>()
        //         .eq("user_id", user.getId())
        //         .eq("goods_id", goodsId));
        // if (exist != null) {
        //     return;
        // }
        GoodsVo goodsVo = goodsService.findGoodsVoById(goodsId);
        log.info("<下单信息>:{}",message);
        try {
            com.cbh.seckill.pojo.Order order = orderService.secKill(user, goodsVo);
            if (order == null) {
                stringRedisTemplate.opsForValue().increment("seckillGoods:" + goodsVo.getId());
                redisTemplate.delete("seckillDup:" + user.getId() + ":" + goodsVo.getId());
                stringRedisTemplate.opsForZSet().remove("seckillPendingSet" , user.getId() + ":" + goodsVo.getId());
                stringRedisTemplate.delete("seckillPending:" + user.getId() + ":" + goodsVo.getId());
            } else {
                stringRedisTemplate.opsForZSet().remove("seckillPendingSet" , user.getId() + ":" + goodsVo.getId());
                stringRedisTemplate.delete("seckillPending:" + user.getId() + ":" + goodsVo.getId());
            }
        } catch (DuplicateKeyException e) {
            log.warn("用户 {} 对商品 {} 重复下单", user.getId(), goodsVo.getId());
        } catch (DataIntegrityViolationException e) {
            log.error("下单时发生数据完整性错误: {}", e.getMessage());
            stringRedisTemplate.opsForValue().increment("seckillGoods:" + goodsVo.getId());
            redisTemplate.delete("seckillDup:" + user.getId() + ":" + goodsVo.getId());
            stringRedisTemplate.opsForZSet().remove("seckillPendingSet" , user.getId() + ":" + goodsVo.getId());
            stringRedisTemplate.delete("seckillPending:" + user.getId() + ":" + goodsVo.getId());
        } catch (Exception e) {
            log.error("下单异常", e);
            stringRedisTemplate.opsForValue().increment("seckillGoods:" + goodsVo.getId());
            redisTemplate.delete("seckillDup:" + user.getId() + ":" + goodsVo.getId());
            stringRedisTemplate.opsForZSet().remove("seckillPendingSet" , user.getId() + ":" + goodsVo.getId());
            stringRedisTemplate.delete("seckillPending:" + user.getId() + ":" + goodsVo.getId());
            throw e;
        }
    }
}
