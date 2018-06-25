package com.heiku.panicbuy.controller;


import com.heiku.panicbuy.entity.User;
import com.heiku.panicbuy.result.Result;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/user")
public class UserController {

    @RequestMapping("/info")
    @ResponseBody
    public Result<User> info(Model model, User user){
        return Result.success(user);
    }
}
