package com.cbh.seckill.serveice;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cbh.seckill.pojo.Order;
import com.cbh.seckill.pojo.User;
import com.cbh.seckill.vo.GoodsVo;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 */

public interface OrderService extends IService<Order> {
    //秒杀
    Order secKill(User user , GoodsVo goodsVo);
    //创建秒杀唯一路径（值）
    String createPath(User user ,Long goodsId);
    //对秒杀路径（值）进行校验
    boolean checkPath(User user ,Long goodsId , String path);
    //验证用户输入的验证码
    boolean checkCaptcha(User user ,  Long goodsId , String captcha);
}
