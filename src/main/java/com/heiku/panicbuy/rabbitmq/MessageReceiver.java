package com.heiku.panicbuy.rabbitmq;


import com.heiku.panicbuy.entity.OrderInfo;
import com.heiku.panicbuy.entity.SeckillOrder;
import com.heiku.panicbuy.entity.User;
import com.heiku.panicbuy.redis.RedisService;
import com.heiku.panicbuy.service.GoodsService;
import com.heiku.panicbuy.service.OrderService;
import com.heiku.panicbuy.service.SeckillService;
import com.heiku.panicbuy.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageReceiver {

    private static Logger log = LoggerFactory.getLogger(MessageReceiver.class);


    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private SeckillService seckillService;



    @RabbitListener(queues = MessageQueueConfig.SECKILL_QUEUE)
    public void receive(String message){
        log.info("receive seckill message : " + message);

        SeckillMessage seckillMessage = RedisService.strToBean(message, SeckillMessage.class);

        User user = seckillMessage.getUser();
        long goodsId = seckillMessage.getGoodsId();


        // 库存判断
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);

        int stock = goodsVo.getStockCount();
        if (stock <= 0){        // 库存为0
            return;
        }

        // 秒杀判断
        SeckillOrder seckillOrder = orderService.getSeckillOrder(user.getId(), goodsId);;

        // 是否先前已存在秒杀订单
        if (seckillOrder != null){
            return ;
        }

        // 秒杀成功：减库存，下订单，记录
        OrderInfo orderInfo = seckillService.executeSeckill(user, goodsVo);


    }








    /*@RabbitListener(queues = MessageQueueConfig.QUEUE)
    public void receive(String message){
        log.info("receive message : " + message);
    }


    @RabbitListener(queues = MessageQueueConfig.TOPIC_QUEUE1)
    public void receiveTopic1(String message){
        log.info("topic queue1 message : " + message);
    }

    @RabbitListener(queues = MessageQueueConfig.TOPIC_QUEUE2)
    public void receiveTopic2(String message){
        log.info("topic queue2 message : " + message);
    }

    @RabbitListener(queues = MessageQueueConfig.HEADERS_QUEUE)
    public void receiveHeaders(byte[] message){
        log.info("headers queue : " + new String(message));
    }
    */
}
