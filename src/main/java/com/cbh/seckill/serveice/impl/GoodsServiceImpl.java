package com.cbh.seckill.serveice.impl;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cbh.seckill.mapper.GoodsMapper;
import com.cbh.seckill.mapper.UserMapper;
import com.cbh.seckill.pojo.Goods;
import com.cbh.seckill.pojo.User;
import com.cbh.seckill.serveice.GoodsService;
import com.cbh.seckill.serveice.Userservice;
import com.cbh.seckill.vo.GoodsVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 辉学Java
 * 辉学Java
 * 辉学Java
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService{

    @Resource
    private GoodsMapper goodsMapper;
    @Override
    public List<GoodsVo> findGoodsVo() {
        return goodsMapper.findGoodsVo();
    }

    @Override
    public GoodsVo findGoodsVoById(Long goodsId) {
        return goodsMapper.findGoodsVoById(goodsId);
    }

}
