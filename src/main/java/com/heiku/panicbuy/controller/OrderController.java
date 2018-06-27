package com.heiku.panicbuy.controller;


import com.heiku.panicbuy.entity.OrderInfo;
import com.heiku.panicbuy.entity.User;
import com.heiku.panicbuy.redis.RedisService;
import com.heiku.panicbuy.result.CodeMsg;
import com.heiku.panicbuy.result.Result;
import com.heiku.panicbuy.service.GoodsService;
import com.heiku.panicbuy.service.OrderService;
import com.heiku.panicbuy.vo.GoodsDetailVo;
import com.heiku.panicbuy.vo.GoodsVo;
import com.heiku.panicbuy.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedisService redisService;

    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    @ResponseBody
    public Result<OrderDetailVo> getOrderInfo(Model model, User user, @RequestParam("orderId") String orderId){

        // 用户判断
        if (user == null)
            return Result.error(CodeMsg.SESSION_ERROR);


        // 判断订单是否存在
        OrderInfo orderInfo = orderService.getOrderById(orderId);
        if (orderInfo == null)
            return Result.error(CodeMsg.ORDER_NOT_EXIST);

        long goodsId = orderInfo.getGoodsId();
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);


        // 返回orderDetail
        OrderDetailVo detailVo = new OrderDetailVo();
        detailVo.setGoods(goodsVo);
        detailVo.setOrder(orderInfo);

        return Result.success(detailVo);

    }
}
