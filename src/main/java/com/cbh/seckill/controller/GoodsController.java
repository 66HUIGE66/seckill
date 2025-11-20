package com.cbh.seckill.controller;

import com.cbh.seckill.pojo.User;
import com.cbh.seckill.serveice.GoodsService;
import com.cbh.seckill.serveice.Userservice;
import com.cbh.seckill.vo.GoodsVo;
import com.cbh.seckill.vo.RespBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {
    @Resource
    private RedisTemplate redisTemplate;
    //手动进行渲染需要的模板解析器
    @Resource
    private ThymeleafViewResolver thymeleafViewResolver;
    @Resource
    private Userservice userservice;

    @Resource
    private GoodsService goodsService;

    //商品页面
//    @RequestMapping("/toList")
////    public String toList(HttpSession session , Model model , @CookieValue(value = "userTicket" , required = false) String ticket){
//    public String toList(Model model , @CookieValue(value = "userTicket" , required = false) String ticket , HttpServletRequest request , HttpServletResponse response){
//        //如果cookie没有生成
//        if (!StringUtils.hasText(ticket)){
//            return "login";
//        }
////        //通过ticket获取session中的user
////        User user = (User) session.getAttribute(ticket);
////        if (null == user){//没有登陆记录
////            return "login";
////        }
//        //从redis获取user
//        User user = userservice.getUserByCookie(ticket, request, response);
//        if (null == user){
//            return "login";
//        }
//        //将user放到model
//        model.addAttribute("user" ,user);
//        return "goodsList";
//    }
    //直接到db查询
//    @RequestMapping("/toList")
//    //使用自定义解析器封装
//    public String toList(Model model , User user){
//        if (null == user){
//            return "login";
//        }
//        model.addAttribute("user" ,user);
//        //将商品列表信息放入到model
//        model.addAttribute("goodsList" , goodsService.findGoodsVo());
//        return "goodsList";
//    }
    //使用redis优化
    //produces = "text/html;charset=utf-8" 表示返回的字符串编码格式
    @RequestMapping(value = "/toList" , produces = "text/html;charset=utf-8")
    @ResponseBody
    //使用自定义解析器封装
    public String toList(Model model , User user , HttpServletRequest request , HttpServletResponse response){

        //先到redis获取页面 ， 就返回
        ValueOperations ops = redisTemplate.opsForValue();
        String goodsList = (String)ops.get("goodsList");
        if (StringUtils.hasText(goodsList)){
            return goodsList;
        }
        if (null == user){
            return thymeleafViewResolver.getTemplateEngine().process("login" ,new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap()));
        }
        model.addAttribute("goodsList", goodsService.findGoodsVo());
        model.addAttribute("user" , user);
        //如果redis中没有获取到页面 ， 就手动渲染并存入到redis
        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        goodsList = thymeleafViewResolver.getTemplateEngine().process("goodsList", webContext);
        if (StringUtils.hasText(goodsList)){
            //将页面返回到redis ， 每60秒更新一次(删除后重新从db获取)
            ops.set("goodsList" , goodsList , 60 , TimeUnit.SECONDS);
        }

        return goodsList;
    }
//    @RequestMapping("/toDetail/{id}")
//    //进入到商品详情页 ----- 到db查询
//    // user 是通过自定义参数解析器返回的 ，点击详情时带过来的
//    public String toDetail(Model model , User user , @PathVariable("id") long goodsId){
//        if (null == user) {
//            return "Login";
//        }
//
//        //将user放入model
//        model.addAttribute("user" , user);
//        //获取指定的秒杀商品
//        GoodsVo goodsVo = goodsService.findGoodsVoById(goodsId);
//        model.addAttribute("goods" , goodsVo);
//        //同时返回商品的秒杀状态 和 秒杀倒计时
//        //配合前端展示
//        // 1.secKillStatus 秒杀状态0：未开始 ， 1：进行中  ，2：结束
//        // 2.remainSeconds 剩余描述 ：> 0  表示还有多久开始秒杀
//        //当前时间
//        Date startDate = goodsVo.getStartDate();
//        //秒杀结束时间
//        Date endDate = goodsVo.getEndDate();
//        //秒杀状态
//        int secKillStatus = 0;
//        //秒杀剩余时间
//        int remainSeconds = 0;
//        Date nowDate = new Date();
//        if (nowDate.before(startDate)){
//            //计算还要多久开始秒杀
//            remainSeconds = (int) ((startDate.getTime() - nowDate.getTime()) / 1000);
//        } else if (nowDate.after(endDate)){
//            secKillStatus = 2;
//            remainSeconds = -1;
//        } else {
//            secKillStatus = 1;
//            remainSeconds = 0;
//        }
//        //将seckillStatus和remainSecnds存入model
//        model.addAttribute("secKillStatus" , secKillStatus);
//        model.addAttribute("remainSeconds" , remainSeconds);
//        return "goodsDetail";
//    }
    @RequestMapping(value = "/toDetail/{id}" , produces = "text/html;charset=utf-8")
    @ResponseBody
    //进入到商品详情页 ---- redis优化
    // user 是通过自定义参数解析器返回的 ，点击详情时带过来的
    public String toDetail(Model model , User user , @PathVariable("id") long goodsId , HttpServletRequest request , HttpServletResponse response){
        if (null == user) {
            return thymeleafViewResolver.getTemplateEngine().process("login" ,new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap()));
        }
        ValueOperations ops = redisTemplate.opsForValue();
        String goodsDetail = (String) ops.get("goodsDetail:" + goodsId);
        if (null != goodsDetail){
            return goodsDetail;
        }
        //将user放入model
        model.addAttribute("user" , user);
        //获取指定的秒杀商品
        GoodsVo goodsVo = goodsService.findGoodsVoById(goodsId);
        model.addAttribute("goods" , goodsVo);
        //同时返回商品的秒杀状态 和 秒杀倒计时
        //配合前端展示
        // 1.secKillStatus 秒杀状态0：未开始 ， 1：进行中  ，2：结束
        // 2.remainSeconds 剩余描述 ：> 0  表示还有多久开始秒杀
        //当前时间
        Date startDate = goodsVo.getStartDate();
        //秒杀结束时间
        Date endDate = goodsVo.getEndDate();
        //秒杀状态
        int secKillStatus = 0;
        //秒杀剩余时间
        int remainSeconds = 0;
        Date nowDate = new Date();
        if (nowDate.before(startDate)){
            //计算还要多久开始秒杀
            remainSeconds = (int) ((startDate.getTime() - nowDate.getTime()) / 1000);
        } else if (nowDate.after(endDate)){
            secKillStatus = 2;
            remainSeconds = -1;
        } else {
            secKillStatus = 1;
            remainSeconds = 0;
        }
        //将seckillStatus和remainSecnds存入model
        model.addAttribute("secKillStatus" , secKillStatus);
        model.addAttribute("remainSeconds" , remainSeconds);
        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        goodsDetail = thymeleafViewResolver.getTemplateEngine().process("goodsDetail" , webContext);
        if (StringUtils.hasText(goodsDetail)){
            //将页面返回到redis ， 每60秒更新一次(删除后重新从db获取)
            ops.set("goodsDetail" , goodsDetail , 60 , TimeUnit.SECONDS);
        }
        return goodsDetail;
    }
}
