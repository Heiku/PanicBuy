package com.heiku.panicbuy.controller;

import com.heiku.panicbuy.entity.User;
import com.heiku.panicbuy.redis.RedisService;
import com.heiku.panicbuy.redis.UserKey;
import com.heiku.panicbuy.result.CodeMsg;
import com.heiku.panicbuy.result.Result;
import com.heiku.panicbuy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
public class SimpleController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisService redisService;

    @RequestMapping("/thymeleaf")
    public String thymeleaf(Model model){
        model.addAttribute("name", "heiku");
        return "hello";
    }

    @RequestMapping("/hello")
    @ResponseBody
    public Result<CodeMsg> hello(){
        return Result.success(CodeMsg.SUCCESS);
    }


    @RequestMapping("/error")
    @ResponseBody
    public Result<CodeMsg> error(){
        return Result.error(CodeMsg.SERVER_ERROR);
    }


    @RequestMapping("/db/get")
    @ResponseBody
    public Result getGet(){
        User user = userService.getById(2);

        return Result.success(user);
    }

    /*@RequestMapping("/db/tx")
    @ResponseBody
    public Result<Boolean> getTx(){
        userService.tx();

        return Result.success(true);
    }*/


    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<User> redisGet(){
        User user = redisService.get(UserKey.getById,"" + 1, User.class);
        return Result.success(user);
    }


    /*@RequestMapping("/redis/set")
    @ResponseBody
    public Result<Boolean> redisSet(){
        User user = new User();
        user.setId(1);
        user.setName("my name");

        Boolean v1 = redisService.set(UserKey.getById,"" + 1, user);
        return Result.success(v1);
    }*/
}
