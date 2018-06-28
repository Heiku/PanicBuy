package com.heiku.panicbuy.service;

import com.heiku.panicbuy.dao.GoodsDao;
import com.heiku.panicbuy.entity.SeckillGoods;
import com.heiku.panicbuy.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {

    @Autowired
    private GoodsDao goodsDao;


    public List<GoodsVo> listGoodVo(){
        return goodsDao.listGoodsVo();
    }

    public GoodsVo getGoodsVoByGoodsId(long goodsId) {
        return goodsDao.getGoodsVoByGoodsId(goodsId);
    }

    public boolean reduceStock(GoodsVo goodsVo) {

        SeckillGoods goods = new SeckillGoods();
        goods.setGoodsId(goodsVo.getId());
        goods.setStockCount(goodsVo.getStockCount() - 1);

        int result = goodsDao.reduceStock(goods);
        return result > 0;
    }
}
