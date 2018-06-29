package com.heiku.panicbuy.service;

import com.heiku.panicbuy.entity.OrderInfo;
import com.heiku.panicbuy.entity.SeckillOrder;
import com.heiku.panicbuy.entity.User;
import com.heiku.panicbuy.redis.RedisService;
import com.heiku.panicbuy.redis.SeckillKey;
import com.heiku.panicbuy.util.MD5Util;
import com.heiku.panicbuy.util.UUIDUtil;
import com.heiku.panicbuy.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

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


    /**
     * 秒杀地址检查
     *
     * @param user
     * @param goodsId
     * @param path
     * @return
     */
    public boolean checkSeckillPath(User user, Long goodsId, String path) {
        if (user == null || path == null){
            return false;
        }

        String checkPath = redisService.get(SeckillKey.seckillPath, "" + user.getId() +  "_" + goodsId, String.class);

        return path.equals(checkPath);
    }


    /**
     * 生成秒杀地址
     *
     * @param user
     * @param goodsId
     * @return
     */
    public String createSeckillPath(User user, long goodsId) {

        if (user == null || goodsId <= 0){
            return null;
        }

        // 生成秒杀地址，存入redis，便于访问
        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        redisService.set(SeckillKey.seckillPath, "" + user.getId() + "_" + goodsId, str);

        return str;
    }

    public BufferedImage createVerifyCode(User user, long goodsId) {
        if (user == null || goodsId <= 0){
            return null;
        }


        int width = 80;
        int height = 32;

        // 生成图片
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();

        // 基本属性设置
        graphics.setColor(new Color(0xDCDCDC));
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.BLACK);
        graphics.drawRect(0, 0, width - 1, height - 1);

        Random random = new Random();
        for (int i = 0; i < 50; i++){
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            graphics.drawOval(x, y, 0, 0);
        }

        // 生成随机验证码，加入图片中
        String verifyCode = generateVerifyCode(random);
        graphics.setColor(new Color(0, 100, 0));
        graphics.setFont(new Font("Candara", Font.BOLD, 24));
        graphics.drawString(verifyCode, 8, 24);
        graphics.dispose();

        int rnd = calc(verifyCode);
        redisService.set(SeckillKey.seckillVerifyCode, "" + user.getId() + "," + goodsId, rnd );

        return image;

    }


    // 运算符存储
    private static char[] ops = new char[] {'+', '-', '*'};


    // 生成验证码内容，等式计算
    private String generateVerifyCode(Random random) {
        int num1 = random.nextInt(10);
        int num2 = random.nextInt(10);
        int num3 = random.nextInt(10);
        char op1 = ops[random.nextInt(3)];
        char op2 = ops[random.nextInt(3)];
        String exp = ""+ num1 + op1 + num2 + op2 + num3;
        return exp;
    }

    // 结果计算
    private static int calc(String exp){
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");

            return (Integer)engine.eval(exp);
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }

    }

    public boolean checkVerifyCode(User user, long goodsId, int verifyCode) {
        if (user == null || goodsId <= 0){
            return false;
        }

        // 获得验证码
        Integer result = redisService.get(SeckillKey.seckillVerifyCode, "" + user.getId() + "," + goodsId, Integer.class);
        if (result == null || result - verifyCode != 0){
            return false;
        }

        // redis删除旧验证码
        redisService.delete(SeckillKey.seckillVerifyCode, "" + user.getId() + "," + goodsId);

        return true;
    }
}
