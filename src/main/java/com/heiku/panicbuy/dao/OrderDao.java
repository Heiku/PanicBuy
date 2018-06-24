package com.heiku.panicbuy.dao;

import com.heiku.panicbuy.entity.OrderInfo;
import com.heiku.panicbuy.entity.SeckillOrder;
import org.apache.ibatis.annotations.*;

@Mapper
public interface OrderDao {


    @Select("select * from seckill_order where user_id = #{userId} and goods_id = #{goodsId}")
    SeckillOrder selectSeckillOrder(@Param("userId") long userId, @Param("goodsId") long goodsId);


    /**
     * 插入订单
     *
     * @return
     */
    @Insert("insert into order_info(user_id, goods_id, goods_name, goods_count, goods_price, order_channel," +
            "status, create_time)" +
            "values(#{userId}, #{goodsId}, #{goodsName}, #{goodsCount}, #{goodsPrice}, #{orderChannel}, " +
            "#{status}, #{createTime})")
    @SelectKey(keyColumn = "id", keyProperty = "id", resultType = Long.class, before = false, statement = "select last_insert_id()")
    long insertOrder(OrderInfo orderInfo);


    /**
     * 插入秒杀订单
     *
     * @param seckillOrder
     */
    @Insert("insert into seckill_order (user_id, goods_id, order_id) values(#{userId}, #{goodsId}, #{orderId})")
    int insertSeckillOrder(SeckillOrder seckillOrder);
}
