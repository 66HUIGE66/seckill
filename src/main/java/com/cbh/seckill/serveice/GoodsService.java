package com.cbh.seckill.serveice;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cbh.seckill.pojo.Goods;
import com.cbh.seckill.vo.GoodsVo;

import java.util.List;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 */
public interface GoodsService extends IService<Goods> {
    //商品列表
    List<GoodsVo> findGoodsVo();
    //根据id获取指定的商品
    GoodsVo findGoodsVoById(Long goodsId);
}
