package com.heiku.panicbuy.controller;


import com.heiku.panicbuy.entity.User;
import com.heiku.panicbuy.redis.GoodsKey;
import com.heiku.panicbuy.redis.RedisService;
import com.heiku.panicbuy.result.Result;
import com.heiku.panicbuy.service.GoodsService;
import com.heiku.panicbuy.service.UserService;
import com.heiku.panicbuy.vo.GoodsDetailVo;
import com.heiku.panicbuy.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
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


    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;



    /**
     * 商品列表页
     *
     *
     * QPS:2000, load 5
     *
     * @param model
     * @param user
     * @return
     */
    // 获取cookie 信息（或者手机端的参数）
    @RequestMapping(value = "/tolist", produces = "text/html")
    @ResponseBody
    public String toGoodsList(HttpServletRequest request, HttpServletResponse response, Model model, User user){
        model.addAttribute("user", user);


        // 取出缓存的页面
        String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
        if (!StringUtils.isEmpty(html)){
            return html;
        }

        // 查询商品列表
        List<GoodsVo> goodsVoList = goodsService.listGoodVo();
        model.addAttribute("goodsList", goodsVoList);

        //return "good_lists";


        // 手动渲染，存入缓存
        WebContext context = new WebContext(request, response, request.getServletContext(),
                request.getLocale(), model.asMap());

        html = thymeleafViewResolver.getTemplateEngine().process("good_lists", context);

        if (!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getGoodsList, "", html);
        }

        return html;
    }



    /*@RequestMapping(value = "/todetail/{goodsId}", produces = "text/html")
    @ResponseBody
    public String toDetail(HttpServletRequest request, HttpServletResponse response,
            Model model, User user, @PathVariable("goodsId") long goodsId){

        model.addAttribute("user", user);

        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods", goodsVo);


        // 取出缓存的页面(URL 缓存)
        String html = redisService.get(GoodsKey.getGoodsDetail, ""+goodsId, String.class);
        if (!StringUtils.isEmpty(html)){
            return html;
        }

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

        // return "good_detail";

        // 手动渲染，存入缓存(URL 缓存)
        WebContext context = new WebContext(request, response, request.getServletContext(),
                request.getLocale(), model.asMap());

        html = thymeleafViewResolver.getTemplateEngine().process("good_detail", context);

        if (!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getGoodsDetail, "" + goodsId, html);
        }


        return html;
    }*/


    @RequestMapping(value = "/detail/{goodsId}", method = RequestMethod.GET)
    @ResponseBody
    public Result<GoodsDetailVo> toDetail(HttpServletRequest request, HttpServletResponse response,
                                          Model model, User user, @PathVariable("goodsId") long goodsId){


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


        GoodsDetailVo vo = new GoodsDetailVo();
        vo.setUser(user);
        vo.setGoods(goodsVo);
        vo.setSeckillStatus(seckillStatus);
        vo.setRemainSeconds(remainSeconds);

        return Result.success(vo);
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
