package com.heiku.panicbuy.controller;


import com.heiku.panicbuy.entity.User;
import com.heiku.panicbuy.redis.RedisService;
import com.heiku.panicbuy.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    private static Logger log = LoggerFactory.getLogger(GoodsController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private RedisService redisService;


    // 获取cookie 信息（或者手机端的参数）
    @RequestMapping("tolist")
    public String toGoodsList(Model model, User user){
        model.addAttribute("user", user);

        return "good_lists";
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
