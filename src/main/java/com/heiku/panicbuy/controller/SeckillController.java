package com.heiku.panicbuy.controller;


import com.heiku.panicbuy.entity.OrderInfo;
import com.heiku.panicbuy.entity.SeckillOrder;
import com.heiku.panicbuy.entity.User;
import com.heiku.panicbuy.redis.RedisService;
import com.heiku.panicbuy.result.CodeMsg;
import com.heiku.panicbuy.service.GoodsService;
import com.heiku.panicbuy.service.OrderService;
import com.heiku.panicbuy.service.SeckillService;
import com.heiku.panicbuy.service.UserService;
import com.heiku.panicbuy.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    private UserService userService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private RedisService redisService;


    @RequestMapping("/doseckill")
    public String doSeckill(Model model, User user, @RequestParam("goodsId") Long goodsId){

        if (user == null){
            return "login";
        }

        model.addAttribute("user", user);

        // 库存判断
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);

        int stock = goodsVo.getStockCount();
        if (stock <= 0){
            model.addAttribute("errMsg", CodeMsg.SECKILL_OVER.getMsg());
            return "seckill_fail";
        }

        // 秒杀判断
        SeckillOrder seckillOrder = orderService.getSeckillOrder(user.getId(), goodsId);;

        // 是否先前已存在秒杀订单
        if (seckillOrder != null){
            model.addAttribute("errMsg", CodeMsg.SECKILL_REPEATE.getMsg());
            return "seckill_fail";
        }

        // 秒杀成功：减库存，下订单，记录
        OrderInfo orderInfo = seckillService.executeSeckill(user, goodsVo);
        model.addAttribute("orderInfo", orderInfo);
        model.addAttribute("goods", goodsVo);

        return "order_detail";
    }
}
