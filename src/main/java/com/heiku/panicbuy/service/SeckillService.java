package com.heiku.panicbuy.service;

import com.heiku.panicbuy.entity.OrderInfo;
import com.heiku.panicbuy.entity.SeckillOrder;
import com.heiku.panicbuy.entity.User;
import com.heiku.panicbuy.redis.RedisService;
import com.heiku.panicbuy.redis.SeckillKey;
import com.heiku.panicbuy.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeckillService {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;


    @Autowired
    private RedisService redisService;


    /**
     * 秒杀执行操作：减库存 + 记录购买行为
     *
     *              1.如果先update, update在前面会加锁
     *          锁 + update(发送在mysql网络时间+gc时间） + insert(发送在mysql网络时间+gc时间) + 提交锁
     *          其实的线程就要等，这个锁提交才能执行。
     *             2.如果先insert,
     *          insert(发送在mysql网络时间+gc时间） +  锁+ update(发送在mysql网络时间+gc时间) + 提交锁
     *
     *
     * @param user
     * @param goodsVo
     * @return
     */
    @Transactional
    public OrderInfo executeSeckill(User user, GoodsVo goodsVo){

        OrderInfo orderInfo = null;
        // 减库存
        boolean success = goodsService.reduceStock(goodsVo);
        if (!success){

            // 库存为空，在redis中记录
            setGoodsEmpty(goodsVo.getId());
            return null;
        }else {
            // 记录订单
            orderInfo = orderService.createOrder(user, goodsVo);
        }

        return orderInfo;
    }


    /**
     * 判断是否生成订单
     *
     * @param userId
     * @param goodsId
     * @return
     */
    public long getSeckillResult(Long userId, long goodsId) {
        SeckillOrder seckillOrder = orderService.getSeckillOrder(userId, goodsId);

        if (seckillOrder != null){
            return seckillOrder.getOrderId();
        }else {
            // 判断是卖完还是在排队中
            boolean isEmpty = getGoodsEmpty(goodsId);
            if (isEmpty){
                return -1;
            }else {
                return 0;
            }
        }
    }



    private void setGoodsEmpty(Long goodsId) {
        redisService.set(SeckillKey.isGoodsEmpty, "" + goodsId, true);
    }

    private boolean getGoodsEmpty(long goodsId){
        return redisService.exists(SeckillKey.isGoodsEmpty, "" + goodsId);
    }

}
