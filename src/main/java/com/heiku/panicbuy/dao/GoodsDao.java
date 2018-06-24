package com.heiku.panicbuy.dao;


import com.heiku.panicbuy.entity.Goods;
import com.heiku.panicbuy.entity.SeckillGoods;
import com.heiku.panicbuy.vo.GoodsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface GoodsDao {

    /**
     * 获取商品列表
     * @return
     */
    @Select("select g.*, sg.seckill_price, sg.stock_count, sg.start_time, sg.end_time " +
            "from seckill_goods sg left join goods g on sg.goods_id = g.id")
    List<GoodsVo> listGoodsVo();


    /**
     * 获取指定 Id 的商品
     *
     * @param goodsId
     * @return
     */
    @Select("select g.*, sg.seckill_price, sg.stock_count, sg.start_time, sg.end_time " +
            "from seckill_goods sg left join goods g on sg.goods_id = g.id where g.id = #{goodsId}")
    GoodsVo getGoodsVoByGoodsId(@Param("goodsId") long goodsId);


    /**
     *
     * 减库存
     *
     * @return
     */
    @Update("update seckill_goods set stock_count = stock_count - 1 where goods_id = #{goodsId}")
    int reduceStock(SeckillGoods goods);
}
