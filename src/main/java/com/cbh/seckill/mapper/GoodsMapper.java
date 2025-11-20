package com.cbh.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cbh.seckill.pojo.Goods;

import com.cbh.seckill.vo.GoodsVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 */
@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {
    //获取商品列表-秒杀
    List<GoodsVo> findGoodsVo();
    //根据id获取指定的商品
    GoodsVo findGoodsVoById(Long goodsId);
}
