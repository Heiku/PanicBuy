package com.heiku.panicbuy.controller;


import com.heiku.panicbuy.entity.User;
import com.heiku.panicbuy.redis.RedisService;
import com.heiku.panicbuy.service.GoodsService;
import com.heiku.panicbuy.service.UserService;
import com.heiku.panicbuy.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    private static Logger log = LoggerFactory.getLogger(GoodsController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private RedisService redisService;


    /**
     * 商品列表页
     *
     * @param model
     * @param user
     * @return
     */
    // 获取cookie 信息（或者手机端的参数）
    @RequestMapping("/tolist")
    public String toGoodsList(Model model, User user){
        model.addAttribute("user", user);

        // 查询商品列表
        List<GoodsVo> goodsVoList = goodsService.listGoodVo();
        model.addAttribute("goodsList", goodsVoList);

        return "good_lists";
    }



    @RequestMapping("/todetail/{goodsId}")
    public String toDetail(Model model, User user, @PathVariable("goodsId") long goodsId){

        model.addAttribute("user", user);

        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods", goodsVo);

        // 秒杀时间判断
        long startAt = goodsVo.getStartTime().getTime();
        long endAt = goodsVo.getEndTime().getTime();
        long now = System.currentTimeMillis();

        // 秒杀状态
        int seckillStatus = 0;
        // 剩余时间
        int remainSeconds = 0;

        if (now < startAt){     // 秒杀未开始

            seckillStatus = 0;
            remainSeconds = (int) ((startAt - now) / 1000);
        }else if ( now > endAt){    //秒杀结束

            seckillStatus = 2;
            remainSeconds = -1;
        }else {     // 秒杀进行中

            seckillStatus = 1;
            remainSeconds = 0;
        }

        model.addAttribute("seckillStatus", seckillStatus);
        model.addAttribute("remainSeconds", remainSeconds);

        return "good_detail";
    }



    /*@RequestMapping("tolist")
    public String toGoodsList(Model model,
                              @CookieValue(value = UserService.COOKIE_NAME_TOKEN, required = false) String cookieToken,
                              @RequestParam(value = UserService.COOKIE_NAME_TOKEN, required = false) String paramToken){

        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)){
            return "login";
        }

        //获取token
        String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;

        User user = userService.getByToken(token);
        model.addAttribute("user", user);
        return "good_lists";
    }*/
}
