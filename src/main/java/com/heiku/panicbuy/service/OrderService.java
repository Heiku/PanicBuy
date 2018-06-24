package com.heiku.panicbuy.service;

import com.heiku.panicbuy.dao.OrderDao;
import com.heiku.panicbuy.entity.OrderInfo;
import com.heiku.panicbuy.entity.SeckillOrder;
import com.heiku.panicbuy.entity.User;
import com.heiku.panicbuy.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class OrderService {
    
    @Autowired
    private OrderDao orderDao;


    public SeckillOrder getSeckillOrder(Long userId, Long goodsId) {
        return orderDao.selectSeckillOrder(userId, goodsId);
    }


    @Transactional
    public OrderInfo createOrder(User user, GoodsVo goodsVo) {

        // 订单设置，写入数据库
        OrderInfo orderInfo = new OrderInfo();

        orderInfo.setCreateTime(new Date());
        orderInfo.setAddressId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsName(goodsVo.getGoodsName());
        orderInfo.setGoodsPrice(goodsVo.getSeckillPrice());
        orderInfo.setOrderChannel(1);
        orderInfo.setStatus(0);
        orderInfo.setUserId(user.getId());
        orderInfo.setGoodsId(goodsVo.getId());

        long orderId = orderDao.insertOrder(orderInfo);

        // 秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setUserId(user.getId());
        seckillOrder.setGoodsId(goodsVo.getId());
        seckillOrder.setOrderId(orderId);

        orderDao.insertSeckillOrder(seckillOrder);

        return orderInfo;
    }
}
