package com.cbh.seckill.serveice.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cbh.seckill.mapper.OrderMapper;
import com.cbh.seckill.pojo.Order;
import com.cbh.seckill.pojo.SeckillGoods;
import com.cbh.seckill.pojo.SeckillOrder;
import com.cbh.seckill.pojo.User;
import com.cbh.seckill.serveice.OrderService;
import com.cbh.seckill.serveice.SeckillGoodsService;
import com.cbh.seckill.serveice.SeckillOrderService;
import com.cbh.seckill.util.Md5Util;
import com.cbh.seckill.util.UUIDUtil;
import com.cbh.seckill.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 */
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper , Order> implements OrderService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private SeckillOrderService seckillOrderService;
    @Resource
    private SeckillGoodsService seckillGoodsService;
    @Resource
    private OrderMapper orderMapper;
    @Override
    @Transactional
    public Order secKill(User user, GoodsVo goodsVo) {
        SeckillGoods goodone = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>().eq("goods_id", goodsVo.getId()));
        if (goodone == null){
            return null;
        }
        SeckillOrder exist = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsVo.getId()));
        if (exist != null){
            return null;
        }
        //判断库存是否为原子性操作
//        goodone.setStockCount(goodone.getStockCount()-1);
//        seckillGoodsService.updateById(goodone);

        //mysql在默认的事务隔离级别下（REPETABLE-READ）
        //当执行update语句时 ， 会在事务中锁定要更新的行
        //可以防止其他会话在同一行执行update
        log.info("userId={}" , user.getId());
        boolean update = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>().setSql("stock_count = stock_count-1").eq("goods_id", goodsVo.getId()).gt("stock_count", 0));
        if (!update){
            // DB更新失败视为售罄：发布售罄标志到Redis，结果查询能快速早退
            redisTemplate.opsForValue().set("seckillSoldOut:" + goodsVo.getId() , 1);
            return null;
        }

        //生成普通订单
        Order order = new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goodsVo.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goodsVo.getGoodsName());
        order.setGoodsCount(1);
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setGoodsPrice(goodone.getSeckillPrice());
        order.setCreateDate(new Date());
        orderMapper.insert(order);

        //生成秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setGoodsId(goodsVo.getId());
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setUserId(user.getId());
        seckillOrderService.save(seckillOrder);

        //将生成的秒杀订单存入到redis ，查询用户是否重购时返回从redis返回
        redisTemplate.opsForValue().set("order:" + user.getId() + ":" + goodsVo.getId() , seckillOrder);

        return order;
    }
    //生成秒杀唯一路径
    @Override
    public String createPath(User user, Long goodsId) {
        //生成秒杀路径
        String path = Md5Util.md5(UUIDUtil.uuid());
        //将随机生成的路径保存Redis ， 设置超时时间
        //key的设计: seckilPayh:userId:goodsId
        redisTemplate.opsForValue().set("seckillPath:" + user.getId() + ":" + goodsId , path , 30 , TimeUnit.SECONDS);
        return path;
    }
    //校验秒杀
    @Override
    public boolean checkPath(User user, Long goodsId, String path) {
        if (user == null || goodsId < 0 || !StringUtils.hasText(path)){
            return false;
        }
        //取出该用户的秒杀路径
        String redisPath = (String) redisTemplate.opsForValue().get("seckillPath:" + user.getId() + ":" + goodsId);
        return path.equals(redisPath);
    }

    @Override
    public boolean checkCaptcha(User user, Long goodsId, String captcha) {
        if (user == null || goodsId < 0 ||  !StringUtils.hasText(captcha)){
            return false;
        }
        //校验用户的验证码是否正确
        //从redis中取出验证码
        String redisCaptcha = (String)redisTemplate.opsForValue().get("captcha:" + user.getId() + ":" + goodsId);
        return captcha.equals(redisCaptcha);
    }
}
