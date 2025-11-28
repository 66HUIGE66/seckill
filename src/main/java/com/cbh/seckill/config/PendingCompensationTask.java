package com.cbh.seckill.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.Set;

@Slf4j
@Component
public class PendingCompensationTask {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 每10秒扫描一次，回收超过2分钟仍未完成的挂起请求
    @Scheduled(fixedDelay = 10000)
    public void compensate() {
        long now = System.currentTimeMillis();
        long expireBefore = now - 120_000; // 2分钟
        Set<String> keys = stringRedisTemplate.opsForZSet().rangeByScore("seckillPendingSet", 0, expireBefore);
        if (keys == null || keys.isEmpty()) {
            return;
        }
        for (String key : keys) {
            try {
                String[] parts = key.split(":");
                if (parts.length != 2) {
                    stringRedisTemplate.opsForZSet().remove("seckillPendingSet", key);
                    continue;
                }
                String userId = parts[0];
                String goodsId = parts[1];
                String orderKey = "order:" + userId + ":" + goodsId;
                boolean done = redisTemplate.hasKey(orderKey);
                if (done) {
                    stringRedisTemplate.opsForZSet().remove("seckillPendingSet", key);
                    stringRedisTemplate.delete("seckillPending:" + key);
                    continue;
                }
                stringRedisTemplate.opsForValue().increment("seckillGoods:" + goodsId);
                redisTemplate.delete("seckillDup:" + userId + ":" + goodsId);
                stringRedisTemplate.opsForZSet().remove("seckillPendingSet", key);
                stringRedisTemplate.delete("seckillPending:" + key);
                log.warn("挂起超时回补: user={} goods={}", userId, goodsId);
            } catch (Exception e) {
                log.error("挂起回补异常: key={}", key, e);
            }
        }
    }
}

