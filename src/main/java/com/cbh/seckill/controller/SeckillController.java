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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
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
    private RedisTemplate redisTemplate;
    @Resource
    private OrderService orderService;
    @Resource
    private SeckillOrderService seckillOrderService;
    @Resource
    private GoodsService goodsService;
    @Resource
    private RabbitMQSenderMessage mqSender;
    //内存标记 ， 记录秒杀商品是否还有库存
    private HashMap<Long , Boolean> entryStockMap = new HashMap<>();
    //处理秒杀请求
//    @RequestMapping("/doSeckill")
//    public String doSeckill(Model model , User user , Long goodsId){
//        //TODO Version1.0
//        if (user == null){ //用户没有登录
//            return "login";
//        }
//        model.addAttribute("user" , user);
//        GoodsVo goodsVo = goodsService.findGoodsVoById(goodsId);
//        if (goodsVo.getStockCount() < 1){
//            model.addAttribute("errmsg" , RespBeanEnum.ENTRY_STOCK.getMessage());
//            return "secKillFail";
//        }
//        //判断是否重购
//        SeckillOrder secOne = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
//        if (secOne != null){
//            model.addAttribute("errmsg" , RespBeanEnum.ENTRY_STOCK_LIMIT.getMessage());
//            return "secKillFail";
//        }
//        //抢购
//        Order order = orderService.secKill(user, goodsVo);
//        if (order == null){
//            model.addAttribute("errmsg" , RespBeanEnum.SECKILL_ERROR.getMessage());
//            return "secKillFail";
//        }
//        //进入订单列表
//        model.addAttribute("order" , order);
//        model.addAttribute("goods" , goodsVo);
//        //TODO Version1.0
//        return "orderDetail";//进入订单详情
//    }

//    @RequestMapping("/doSeckill")
//    public String doSeckill(Model model , User user , Long goodsId){
//        //TODO Version2.0
//        if (user == null){ //用户没有登录
//            return "login";
//        }
//        model.addAttribute("user" , user);
//        GoodsVo goodsVo = goodsService.findGoodsVoById(goodsId);
//        if (goodsVo.getStockCount() < 1){
//            model.addAttribute("errmsg" , RespBeanEnum.ENTRY_STOCK.getMessage());
//            return "secKillFail";
//        }
//        //判断是否重购
////        SeckillOrder secOne = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
////        if (secOne != null){
////            model.addAttribute("errmsg" , RespBeanEnum.ENTRY_STOCK_LIMIT.getMessage());
////            return "secKillFail";
////        }
//        //到redis中查看用户是否已经购买过了
//        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsVo.getId());
//        if (seckillOrder != null){ // 说明已经抢购
//            model.addAttribute("errmsg" , RespBeanEnum.ENTRY_STOCK.getMessage());
//            return "secKillFail";
//        }

//        //抢购
//        Order order = orderService.secKill(user, goodsVo);
//        if (order == null){
//            model.addAttribute("errmsg" , RespBeanEnum.SECKILL_ERROR.getMessage());
//            return "secKillFail";
//        }
//        //进入订单列表
//        model.addAttribute("order" , order);
//        model.addAttribute("goods" , goodsVo);
//        //TODO Version2.0
//        return "orderDetail";//进入订单详情
//    }
//@RequestMapping("/doSeckill")
//public String doSeckill(Model model , User user , Long goodsId){
//    //TODO Version3.0
//    if (user == null){ //用户没有登录
//        return "login";
//    }
//    model.addAttribute("user" , user);
//    GoodsVo goodsVo = goodsService.findGoodsVoById(goodsId);
//    if (goodsVo.getStockCount() < 1){
//        model.addAttribute("errmsg" , RespBeanEnum.ENTRY_STOCK.getMessage());
//        return "secKillFail";
//    }
//    //判断是否重购
////        SeckillOrder secOne = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
////        if (secOne != null){
////            model.addAttribute("errmsg" , RespBeanEnum.ENTRY_STOCK_LIMIT.getMessage());
////            return "secKillFail";
////        }
//    //到redis中查看用户是否已经购买过了
//    SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsVo.getId());
//    if (seckillOrder != null){ // 说明已经抢购
//        model.addAttribute("errmsg" , RespBeanEnum.ENTRY_STOCK.getMessage());
//        return "secKillFail";
//    }
//    //库存预减 ， 如果在减库存中发现秒杀商品已经没有了 ， 就直接返回
//    //有效减少线程堆积 ，
//    // decrement 是具有原子性的
//    Long decrement = redisTemplate.opsForValue().decrement("seckillGoods:" + goodsId);
//    if(decrement < 0){//说明此时没有库存了
//        redisTemplate.opsForValue().increment("seckillGoods:" + goodsId);
//        model.addAttribute("errmsg" , RespBeanEnum.ENTRY_STOCK.getMessage());
//        return "secKillFail";
//    }
//    //抢购
//    Order order = orderService.secKill(user, goodsVo);
//    if (order == null){
//        model.addAttribute("errmsg" , RespBeanEnum.SECKILL_ERROR.getMessage());
//        return "secKillFail";
//    }
//    //进入订单列表
//    model.addAttribute("order" , order);
//    model.addAttribute("goods" , goodsVo);
//    //TODO Version3.0
//    return "orderDetail";//进入订单详情
//}
//@RequestMapping("/doSeckill")
//public String doSeckill(Model model , User user , Long goodsId){
//    //TODO Version4.0
//    if (user == null){ //用户没有登录
//        return "login";
//    }
//    model.addAttribute("user" , user);
//    GoodsVo goodsVo = goodsService.findGoodsVoById(goodsId);
//    if (goodsVo.getStockCount() < 1){
//        model.addAttribute("errmsg" , RespBeanEnum.ENTRY_STOCK.getMessage());
//        return "secKillFail";
//    }
//    //判断是否重购
////        SeckillOrder secOne = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
////        if (secOne != null){
////            model.addAttribute("errmsg" , RespBeanEnum.ENTRY_STOCK_LIMIT.getMessage());
////            return "secKillFail";
////        }
//    //到redis中查看用户是否已经购买过了
//    SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsVo.getId());
//    if (seckillOrder != null){ // 说明已经抢购
//        model.addAttribute("errmsg" , RespBeanEnum.ENTRY_STOCK.getMessage());
//        return "secKillFail";
//    }
//    //对map 进行判断 如果已经么有库存则直接返回
//    if (entryStockMap.get(goodsId)){
//          return "secKillFail";
//    }
//    //库存预减 ， 如果在减库存中发现秒杀商品已经没有了 ， 就直接返回
//    //有效减少线程堆积 ，
//    // decrement 是具有原子性的
//    Long decrement = redisTemplate.opsForValue().decrement("seckillGoods:" + goodsId);
//    if(decrement < 0){//说明此时没有库存了
//        entryStockMap.put(goodsId , true);
//        redisTemplate.opsForValue().increment("seckillGoods:" + goodsId);
//        model.addAttribute("errmsg" , RespBeanEnum.ENTRY_STOCK.getMessage());
//        return "secKillFail";
//    }
//    //抢购
//    Order order = orderService.secKill(user, goodsVo);
//    if (order == null){
//        model.addAttribute("errmsg" , RespBeanEnum.SECKILL_ERROR.getMessage());
//        return "secKillFail";
//    }
//    //进入订单列表
//    model.addAttribute("order" , order);
//    model.addAttribute("goods" , goodsVo);
//    //TODO Version4.0
//    return "orderDetail";//进入订单详情
//}
    //加入消息队列
@RequestMapping("/doSeckill")
public String doSeckill(Model model , User user , Long goodsId){
    //TODO Version5.0
    if (user == null){ //用户没有登录
        return "login";
    }
    model.addAttribute("user" , user);
    GoodsVo goodsVo = goodsService.findGoodsVoById(goodsId);
    if (goodsVo.getStockCount() < 1){
        model.addAttribute("errmsg" , RespBeanEnum.ENTRY_STOCK.getMessage());
        return "secKillFail";
    }
    //判断是否重购
//        SeckillOrder secOne = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
//        if (secOne != null){
//            model.addAttribute("errmsg" , RespBeanEnum.ENTRY_STOCK_LIMIT.getMessage());
//            return "secKillFail";
//        }
    //到redis中查看用户是否已经购买过了
    SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsVo.getId());
    if (seckillOrder != null){ // 说明已经抢购
        model.addAttribute("errmsg" , RespBeanEnum.ENTRY_STOCK_LIMIT.getMessage());
        return "secKillFail";
    }
    //对map 进行判断 如果已经么有库存则直接返回
    if (entryStockMap.get(goodsId)){
        return "secKillFail";
    }
    //库存预减 ， 如果在减库存中发现秒杀商品已经没有了 ， 就直接返回
    //有效减少线程堆积 ，
    // decrement 是具有原子性，
//    Long decrement = redisTemplate.opsForValue().decrement("seckillGoods:" + goodsId);
//    if(decrement < 0){//说明此时没有库存了
//        entryStockMap.put(goodsId , true);
//        redisTemplate.opsForValue().increment("seckillGoods:" + goodsId);
//        model.addAttribute("errmsg" , RespBeanEnum.ENTRY_STOCK.getMessage());
//        return "secKillFail";
//    }
    //使用Redis分布式锁
    //对应当前项目使用redisTemplate.opsForValue().increment就可以控制抢购，
    // 因为该方法具有原子性
    //但如果需要需要进行较多的操作则需要保证隔离性，需要扩大隔离范围，部分操作还需要原子性

    //获取锁，得到应该uuid值转为锁的值
    String uuid = UUID.randomUUID().toString();
    Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 3, TimeUnit.SECONDS);

    if (lock){
//        //释放锁
//        //定义lua脚本
//        String script = "if redis.call('get',KEYS[1])==ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
//        //使用redis执行lua
//        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
//        redisScript.setScriptText(script);
//        redisScript.setResultType(Long.class);
        //执行自己的业务
        Long decrement = redisTemplate.opsForValue().decrement("seckillGoods:" + goodsId);
        if(decrement < 0){//说明此时没有库存了
            entryStockMap.put(goodsId , true);
            redisTemplate.opsForValue().increment("seckillGoods:" + goodsId);
            //释放锁
            redisTemplate.execute(redisScript , Arrays.asList("lock"), uuid);
            model.addAttribute("errmsg" , RespBeanEnum.ENTRY_STOCK.getMessage());
            return "secKillFail";
        }
        redisTemplate.execute(redisScript , Arrays.asList("lock"), uuid);
    } else {
        //获取锁失败、返回信息, 这次抢购失败,请继续抢购
        model.addAttribute("errmsg", RespBeanEnum.SEC_KILL_RETRY.getMessage());
        return "secKillFail";//错误页面
    }

    //抢购 , 向消息队列发送秒杀消息 ， 实现了秒杀的异步请求
    //发送消息后可以快速返回临时结果
    SecKillMessage secKillMessage = new SecKillMessage(user, goodsId);
    mqSender.sendMessage(JSONUtil.toJsonStr(secKillMessage));
    model.addAttribute("errmsg" , "排队中");
    //TODO Version5.0
    return "secKillFail";//进入订单详情
}
    //隐藏路径，增加秒杀安全
    //直接返回RespBean
//@RequestMapping("/{path}/doSeckill")
//@ResponseBody
//public RespBean doSeckill(@PathVariable String path , User user , Long goodsId){
//    //TODO Version6.0
//    if (user == null){ //用户没有登录
//        return RespBean.error(RespBeanEnum.SESSION_ERROR);
//    }
//    //增加判断路径
//    boolean checkPath = orderService.checkPath(user, goodsId, path);
//    if (!checkPath){
//        //校验失败
//        return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
//    }
//    GoodsVo goodsVo = goodsService.findGoodsVoById(goodsId);
//    if (goodsVo.getStockCount() < 1){
//        return RespBean.error(RespBeanEnum.ENTRY_STOCK);
//    }
//    //判断是否重购
////        SeckillOrder secOne = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
////        if (secOne != null){
////            model.addAttribute("errmsg" , RespBeanEnum.ENTRY_STOCK_LIMIT.getMessage());
////            return "secKillFail";
////        }
//    //到redis中查看用户是否已经购买过了
//    SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsVo.getId());
//    if (seckillOrder != null){ // 说明已经抢购
//        return RespBean.error(RespBeanEnum.ENTRY_STOCK_LIMIT);
//    }
//    //对map 进行判断 如果已经么有库存则直接返回
//    if (entryStockMap.get(goodsId)){
//        return RespBean.error(RespBeanEnum.ENTRY_STOCK);
//    }
//    //库存预减 ， 如果在减库存中发现秒杀商品已经没有了 ， 就直接返回
//    //有效减少线程堆积 ，
//    // decrement 是具有原子性的
//    Long decrement = redisTemplate.opsForValue().decrement("seckillGoods:" + goodsId);
//    if(decrement < 0){//说明此时没有库存了
//        entryStockMap.put(goodsId , true);
//        redisTemplate.opsForValue().increment("seckillGoods:" + goodsId);
//        return RespBean.error(RespBeanEnum.ENTRY_STOCK);
//    }
//    //抢购 , 向消息队列发送秒杀消息 ， 实现了秒杀的异步请求
//    //发送消息后可以快速返回临时结果
//    SecKillMessage secKillMessage = new SecKillMessage(user, goodsId);
//    mqSender.sendMessage(JSONUtil.toJsonStr(secKillMessage));
//    //TODO Version6.0
//    return RespBean.error(RespBeanEnum.SEK_KILL_WAIT);
//}
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
            redisTemplate.opsForValue().set("seckillGoods:" + goodsVo.getId() , goodsVo.getStockCount());
        });
    }
    //获取秒杀路径
    @RequestMapping("/path")
    @ResponseBody
    //@AccessLimit 使用该注解的方式实现防止用户刷 ， 通用性和灵活性增强
    //second = 5（5秒过期） , maxCount = 5（最大访问次数）, needLogin = true（是否需要登录）
    @AccessLimit(second = 5 , maxCount = 10 , needLogin = true)
    public RespBean getSeckillPath(User user , Long goodsId , String captcha , HttpServletRequest request , HttpServletResponse response){
        if (user == null || goodsId < 0 || !StringUtils.hasText(captcha)){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        //增加业务逻辑：加入Redis计数器，完成对用户的限流防刷
//        String uri = request.getRequestURI();
//        ValueOperations ops = redisTemplate.opsForValue();
//        String key = uri + ":" + user.getId();
//        Integer cnt = (Integer) ops.get(key);
//        if (cnt == null){//第一次访问
//            ops.set(key , 1 ,5 ,TimeUnit.SECONDS);
//        } else if (cnt < 5){
////            ops.increment(key);
//        } else { //说明用户在刷接口
//            return RespBean.error(RespBeanEnum.ACCESS_LIMIT_REACHED);
//        }
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
}
