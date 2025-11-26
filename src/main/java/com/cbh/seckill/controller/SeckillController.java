package com.cbh.seckill.controller;
import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONUtil;
import com.cbh.seckill.config.AccessLimit;
import com.cbh.seckill.pojo.Order;
import com.cbh.seckill.pojo.SecKillMessage;
import com.cbh.seckill.pojo.SeckillOrder;
import com.cbh.seckill.pojo.User;
import com.cbh.seckill.rabbitmq.MQSender;
import com.cbh.seckill.rabbitmq.RabbitMQSenderMessage;
import com.cbh.seckill.serveice.GoodsService;
import com.cbh.seckill.serveice.OrderService;
import com.cbh.seckill.serveice.SeckillOrderService;
import com.cbh.seckill.vo.GoodsVo;
import com.cbh.seckill.vo.RespBean;
import com.cbh.seckill.vo.RespBeanEnum;
import com.ramostear.captcha.HappyCaptcha;
import com.ramostear.captcha.common.Fonts;
import com.ramostear.captcha.support.CaptchaStyle;
import com.ramostear.captcha.support.CaptchaType;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 */
@Controller
@RequestMapping("/seckill")
public class SeckillController implements InitializingBean {
    //装配redisScript
    @Resource
    private RedisScript<Long> redisScript;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private OrderService orderService;
    @Resource
    private SeckillOrderService seckillOrderService;
    @Resource
    private GoodsService goodsService;
    @Resource
    private RabbitMQSenderMessage mqSender;
    //内存标记 ， 记录秒杀商品是否还有库存
    private ConcurrentHashMap<Long , Boolean> entryStockMap = new ConcurrentHashMap<>();
    //处理秒杀请求
    //加入消息队列
@RequestMapping("/doSeckill")
    public String doSeckill(Model model , User user , Long goodsId){
    //TODO Version5.0
    if (user == null){ //用户没有登录
        return "login";
    }
    model.addAttribute("user" , user);
    

    //判断是否重购
//        SeckillOrder secOne = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
//        if (secOne != null){
//            model.addAttribute("errmsg" , RespBeanEnum.ENTRY_STOCK_LIMIT.getMessage());
//            return "secKillFail";
//        }
    //到redis中查看用户是否已经购买过了
    SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
    if (seckillOrder != null){ // 说明已经抢购
        model.addAttribute("errmsg" , RespBeanEnum.ENTRY_STOCK_LIMIT.getMessage());
        return "secKillFail";
    }
    //对map 进行判断 如果已经么有库存则直接返回
    if (Boolean.TRUE.equals(entryStockMap.get(goodsId))){
        return "secKillFail";
    }
    // 一致性增强：Lua原子“去重哨兵+扣库存”，成功则直接入队并返回
    String lua = "local dup=KEYS[2]; local stock=KEYS[1]; if redis.call('exists',dup)==1 then return -1 end; local v=redis.call('get',stock); if not v then return 0 end; local n=tonumber(v); if n<=0 then return 0 end; redis.call('decr',stock); redis.call('set',dup,'1','EX',ARGV[1]); return 1";
    DefaultRedisScript<Long> stockScript = new DefaultRedisScript<>();
    stockScript.setScriptText(lua);
    stockScript.setResultType(Long.class);
    String stockKey = "seckillGoods:" + goodsId;
    String vs = stringRedisTemplate.opsForValue().get(stockKey);
    if (vs != null && !vs.matches("^-?\\d+$")) {
        stringRedisTemplate.opsForValue().set(stockKey, "0");
    }
    Long r = (Long)stringRedisTemplate.execute(stockScript , Arrays.asList(stockKey , "seckillDup:" + user.getId() + ":" + goodsId) , "300");
    if (r == null || r == 0L){
        entryStockMap.put(goodsId , true);
        redisTemplate.opsForValue().set("seckillSoldOut:" + goodsId , 1 , 1 , TimeUnit.HOURS);
        model.addAttribute("errmsg" , RespBeanEnum.ENTRY_STOCK.getMessage());
        return "secKillFail";
    }
    if (r == -1L){
        model.addAttribute("errmsg" , RespBeanEnum.ENTRY_STOCK_LIMIT.getMessage());
        return "secKillFail";
    }
    SecKillMessage secKillMessage = new SecKillMessage(user, goodsId);
    stringRedisTemplate.opsForValue().set("seckillPending:" + user.getId() + ":" + goodsId , "1" , 600 , TimeUnit.SECONDS);
    stringRedisTemplate.opsForZSet().add("seckillPendingSet" , user.getId() + ":" + goodsId , System.currentTimeMillis());
    mqSender.sendMessage(JSONUtil.toJsonStr(secKillMessage));
    model.addAttribute("errmsg" , "排队中");
    return "secKillFail";

    
    

    


    
        

    
    
}

    //该方法所在类的所有属性出刷后自动执行的
    //可以在改方法中将所有秒杀商品的库存量 加载到Redis
    @Override
    public void afterPropertiesSet() throws Exception {
        //查询所有的秒杀秒杀
        List<GoodsVo> list = goodsService.findGoodsVo();
        //变量list ， 将所有秒杀水平的库存量 存入到Redis中
        if (CollectionUtils.isEmpty(list)){
            return;
        }
        //遍历List , 将秒杀商品的库存量放入到Redis中
        list.forEach(goodsVo -> {
            //初始化map , fales表示有库存 ， true表示没有库存

            entryStockMap.put(goodsVo.getId(),  false);
            // 初始化库存为纯数字字符串，避免DECR因非数字报错
            stringRedisTemplate.opsForValue().set("seckillGoods:" + goodsVo.getId() , String.valueOf(goodsVo.getStockCount()));
        });
    }
    //获取秒杀路径
    @RequestMapping("/path")
    @ResponseBody
    //@AccessLimit 使用该注解的方式实现防止用户刷 ， 通用性和灵活性增强
    //second = 5（5秒过期） , maxCount = 5（最大访问次数）, needLogin = true（是否需要登录）
    @AccessLimit(second = 5 , maxCount = 5 , needLogin = true)
    public RespBean getSeckillPath(User user , Long goodsId , String captcha , HttpServletRequest request , HttpServletResponse response){
        if (user == null || goodsId < 0 || !StringUtils.hasText(captcha)){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        //增加业务逻辑：加入Redis计数器，完成对用户的限流防刷
        //校验用户输入的验证码是否正确
        boolean check = orderService.checkCaptcha(user, goodsId, captcha);
        if (!check){
            return RespBean.error(RespBeanEnum.CAPTCHA_ERROR);
        }
        String path = orderService.createPath(user, goodsId);
        return RespBean.success(path);
    }
    //生成验证码-happycaptcha
    @RequestMapping("/captcha")
    public void happyCaptcha(HttpServletRequest request , HttpServletResponse response , User user , Long goodsId){
        //生成验证码
        //该验证码默认保存到session中 key是captcha
        HappyCaptcha.require(request, response)
                .style(CaptchaStyle.ANIM) //设置展现样式为动画
                .type(CaptchaType.NUMBER) //设置验证码内容为数字
                .length(6) //设置字符长度为6
                .width(220)//设置动画宽度为220
                .height(80)//设置动画高度为80
                .font(Fonts.getInstance().zhFont())         //设置汉字的字体
                .build().finish(); //生成并输出验证码
        // 把验证码的值 ， 保存到Resdis中
        //key：captcha:userId:goodsId
        redisTemplate.opsForValue().set("cahptcha:" + user.getId() + ":" + goodsId , (String)request.getSession().getAttribute("happy-captcha") , 30 , TimeUnit.SECONDS);

    }
    //查询秒杀结果：成功返回订单ID，排队中返回等待提示，失败返回库存不足
    @RequestMapping("/result")
    @ResponseBody
    public RespBean getSeckillResult(User user , Long goodsId){
        if (user == null || goodsId == null || goodsId <= 0){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>()
                .eq("user_id", user.getId())
                .eq("goods_id", goodsId));
        if (seckillOrder != null){
            return RespBean.success(seckillOrder.getOrderId());
        }
        Boolean soldOutMem = entryStockMap.get(goodsId);
        Boolean soldOutRedis = redisTemplate.hasKey("seckillSoldOut:" + goodsId);
        if ((soldOutMem != null && soldOutMem) || Boolean.TRUE.equals(soldOutRedis)){
            return RespBean.error(RespBeanEnum.ENTRY_STOCK);
        }
        return RespBean.error(RespBeanEnum.SEK_KILL_WAIT);
    }

    @RequestMapping("/{path}/doSeckill")
    @ResponseBody
    public RespBean doSeckillPath(@PathVariable String path , User user , Long goodsId){
        if (user == null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        boolean checkPath = orderService.checkPath(user, goodsId, path);
        if (!checkPath){
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null){
            return RespBean.error(RespBeanEnum.ENTRY_STOCK_LIMIT);
        }
        if (Boolean.TRUE.equals(entryStockMap.get(goodsId))){
            return RespBean.error(RespBeanEnum.ENTRY_STOCK);
        }
        String s = "local dup=KEYS[2]; local stock=KEYS[1]; if redis.call('exists',dup)==1 then return -1 end; local v=redis.call('get',stock); if not v then return 0 end; local n=tonumber(v); if n<=0 then return 0 end; redis.call('decr',stock); redis.call('set',dup,'1','EX',ARGV[1]); return 1";
        DefaultRedisScript<Long> stockScript = new DefaultRedisScript<>();
        stockScript.setScriptText(s);
        stockScript.setResultType(Long.class);
        Long r = (Long)redisTemplate.execute(stockScript , Arrays.asList("seckillGoods:" + goodsId , "seckillDup:" + user.getId() + ":" + goodsId) , "300");
        if (r == null || r == 0L){
            entryStockMap.put(goodsId , true);
            redisTemplate.opsForValue().set("seckillSoldOut:" + goodsId , 1 , 1 , TimeUnit.HOURS);
            return RespBean.error(RespBeanEnum.ENTRY_STOCK);
        }
        if (r == -1L){
            return RespBean.error(RespBeanEnum.ENTRY_STOCK_LIMIT);
        }
        SecKillMessage secKillMessage = new SecKillMessage(user, goodsId);
        mqSender.sendMessage(JSONUtil.toJsonStr(secKillMessage));
        return RespBean.error(RespBeanEnum.SEK_KILL_WAIT);
    }

    @RequestMapping("/doSeckillSync")
    @ResponseBody
    public RespBean doSeckillSync(User user , Long goodsId){
        if (user == null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        if (goodsId == null || goodsId <= 0){
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }
        if (Boolean.TRUE.equals(entryStockMap.get(goodsId))){
            return RespBean.error(RespBeanEnum.ENTRY_STOCK);
        }
        String s = "local dup=KEYS[2]; local stock=KEYS[1]; if redis.call('exists',dup)==1 then return -1 end; local v=redis.call('get',stock); if not v then return 0 end; local n=tonumber(v); if n<=0 then return 0 end; redis.call('decr',stock); redis.call('set',dup,'1','EX',ARGV[1]); return 1";
        DefaultRedisScript<Long> stockScript = new DefaultRedisScript<>();
        stockScript.setScriptText(s);
        stockScript.setResultType(Long.class);
        Long r = (Long)redisTemplate.execute(stockScript , Arrays.asList("seckillGoods:" + goodsId , "seckillDup:" + user.getId() + ":" + goodsId) , "300");
        if (r == null || r == 0L){
            entryStockMap.put(goodsId , true);
            redisTemplate.opsForValue().set("seckillSoldOut:" + goodsId , 1 , 1 , TimeUnit.HOURS);
            return RespBean.error(RespBeanEnum.ENTRY_STOCK);
        }
        if (r == -1L){
            return RespBean.error(RespBeanEnum.ENTRY_STOCK_LIMIT);
        }
        GoodsVo goodsVo = goodsService.findGoodsVoById(goodsId);
        Order order = orderService.secKill(user , goodsVo);
        if (order == null){
            return RespBean.error(RespBeanEnum.SECKILL_ERROR);
        }
        return RespBean.success(order.getId());
    }
}
