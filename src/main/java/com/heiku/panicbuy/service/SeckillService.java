package com.heiku.panicbuy.service;

import com.heiku.panicbuy.entity.OrderInfo;
import com.heiku.panicbuy.entity.User;
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


    /**
     * 秒杀执行操作：减库存 + 记录购买行为
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

        // 记录订单
        OrderInfo orderInfo = orderService.createOrder(user, goodsVo);

        // 减库存
        goodsService.reduceStock(goodsVo);


        return orderInfo;
    }
}
