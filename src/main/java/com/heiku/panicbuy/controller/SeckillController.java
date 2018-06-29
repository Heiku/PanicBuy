package com.heiku.panicbuy.controller;


import com.heiku.panicbuy.access.AccessLimit;
import com.heiku.panicbuy.entity.OrderInfo;
import com.heiku.panicbuy.entity.SeckillOrder;
import com.heiku.panicbuy.entity.User;
import com.heiku.panicbuy.rabbitmq.MessageSender;
import com.heiku.panicbuy.rabbitmq.SeckillMessage;
import com.heiku.panicbuy.redis.AccessKey;
import com.heiku.panicbuy.redis.GoodsKey;
import com.heiku.panicbuy.redis.RedisService;
import com.heiku.panicbuy.redis.SeckillKey;
import com.heiku.panicbuy.result.CodeMsg;
import com.heiku.panicbuy.result.Result;
import com.heiku.panicbuy.service.GoodsService;
import com.heiku.panicbuy.service.OrderService;
import com.heiku.panicbuy.service.SeckillService;
import com.heiku.panicbuy.service.UserService;
import com.heiku.panicbuy.util.MD5Util;
import com.heiku.panicbuy.util.UUIDUtil;
import com.heiku.panicbuy.vo.GoodsVo;
import com.sun.org.apache.bcel.internal.classfile.Code;
import com.sun.org.apache.regexp.internal.RE;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/seckill")
public class SeckillController implements InitializingBean {

    private Map<Long, Boolean> localOverMap = new HashMap<>();


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

    @Autowired
    private MessageSender messageSender;

    /**
     * 优化：1.页面静态化，页面缓存
     *      2. (1) 系统初始化，将库存加载到redis
     *         (2) 收到请求，redis自减，当库存不足直接返回
     *         (3) 异步下单，将请求入队，
     *         (4) 请求出队，生成订单，减少库存
     *         (5) 客户端轮询，判断是否秒杀成功
     *
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    // 页面静态化处理
    @RequestMapping(value = "/{path}/doseckill", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> doSeckill(Model model, User user, @RequestParam("goodsId") Long goodsId,
                                     @PathVariable("path") String path){


        // 用户不存在，返回error
        if (user == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        model.addAttribute("user", user);


        // 验证地址
        boolean checkPath = seckillService.checkSeckillPath(user, goodsId, path);
        if (!checkPath){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

        // 内存标记，减少redis访问
        boolean over = localOverMap.get(goodsId);
        if (over){
            return Result.error(CodeMsg.SECKILL_OVER);
        }


        // 库存判断,redis库存递减
        long stock = redisService.decr(GoodsKey.getSeckillGoodsStock, "" + goodsId);
        if (stock < 0){
            // 标记商品售空
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.SECKILL_OVER);
        }

        // 重复秒杀判断
        SeckillOrder seckillOrder = orderService.getSeckillOrder(user.getId(), goodsId);
        if (seckillOrder != null){
            return Result.error(CodeMsg.SECKILL_REPEATE);
        }

        // 请求入队 (封装秒杀请求)
        SeckillMessage message = new SeckillMessage();
        message.setUser(user);
        message.setGoodsId(goodsId);

        messageSender.sendSeckillMessage(message);

        return Result.success(0);
    }


    /**
     * 轮询查询结果，查看是否生成对应的订单
     *      result : orderId：成功
     *                  -1 ： 秒杀失败
     *                  0 :  排队中
     *
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> seckillResult(Model model, User user, @RequestParam("goodsId") long goodsId){

        if (user == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        model.addAttribute("user", user);

        long result = seckillService.getSeckillResult(user.getId(), goodsId);
        return Result.success(result);

    }


    /**
     * 系统初始化, redis设置商品库存
     */
    @Override
    public void afterPropertiesSet() {
        List<GoodsVo> list = goodsService.listGoodVo();
        if (list == null){
            return ;
        }

        // 将每样秒杀商品的库存加载到redis中
        for (GoodsVo goodsVo : list){
            redisService.set(GoodsKey.getSeckillGoodsStock, "" + goodsVo.getId(), goodsVo.getStockCount());
            localOverMap.put(goodsVo.getId(), false);
        }

    }


    /**
     * 获取秒杀地址
     *
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    @AccessLimit(seconds = 5, maxCount = 5, needLogin = true)
    public Result<String> getSeckillPath(HttpServletRequest request, Model model, User user, @RequestParam("goodsId") long goodsId,
                                 @RequestParam("verifyCode") int verifyCode){

        if (user == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        model.addAttribute("user", user);


        // 验证码校验
        boolean checkVerifyCode = seckillService.checkVerifyCode(user, goodsId, verifyCode);
        if (user == null){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

        // 生成秒杀地址
        String path = seckillService.createSeckillPath(user, goodsId);

        return Result.success(path);

    }


    @RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getSeckillVerifyCode(Model model, User user, @RequestParam("goodsId") long goodsId,
                                               HttpServletResponse response){

        if (user == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        BufferedImage image = seckillService.createVerifyCode(user, goodsId);
        try {
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();

            return null;
        }catch (Exception e){
            e.printStackTrace();
            return Result.error(CodeMsg.SECKILL_FAIL);
        }
    }


    /**
     *  redis优化，页面静态化，缓存优化
     *
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    /*@RequestMapping(value = "/doseckill", method = RequestMethod.POST)
    @ResponseBody
    public Result<OrderInfo> doSeckill(Model model, User user, @RequestParam("goodsId") Long goodsId){


        // 用户不存在，返回error
        if (user == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        model.addAttribute("user", user);


        // 库存判断
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);

        int stock = goodsVo.getStockCount();
        if (stock <= 0){        // 库存为0
            return Result.error(CodeMsg.SECKILL_OVER);
        }

        // 秒杀判断
        SeckillOrder seckillOrder = orderService.getSeckillOrder(user.getId(), goodsId);;

        // 是否先前已存在秒杀订单
        if (seckillOrder != null){
            return Result.error(CodeMsg.SECKILL_REPEATE);
        }

        // 秒杀成功：减库存，下订单，记录
        OrderInfo orderInfo = seckillService.executeSeckill(user, goodsVo);

        return Result.success(orderInfo);
    }*/


    /**
     *  未优化秒杀流程
     */
    /*@RequestMapping("/doseckill")
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
    }*/
}
