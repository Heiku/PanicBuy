package com.heiku.panicbuy.access;

import com.alibaba.fastjson.JSON;
import com.heiku.panicbuy.entity.User;
import com.heiku.panicbuy.redis.AccessKey;
import com.heiku.panicbuy.redis.RedisService;
import com.heiku.panicbuy.result.CodeMsg;
import com.heiku.panicbuy.result.Result;
import com.heiku.panicbuy.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;


@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisService redisService;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod){

            // 获取用户信息
            User user = getSeckillUser(request, response);
            UserContext.setUser(user);


            // handkerMethod 获取 AccessLimit 注解
            HandlerMethod handlerMethod = (HandlerMethod)handler;
            AccessLimit accessLimit = handlerMethod.getMethodAnnotation(AccessLimit.class);
            if (accessLimit == null){
                return true;
            }

            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();

            String key = request.getRequestURI();
            if (needLogin){
                if (user == null){

                    // 直接返回 登录失败
                    render(response, CodeMsg.SESSION_ERROR);
                    return false;
                }

                key += "_" + user.getId();

            }else {

            }

            // 动态修改redis保留时间 (seconds)
            AccessKey ak = AccessKey.withExpire(seconds);

            Integer count = redisService.get(ak, key, Integer.class);
            if (count == null){
                redisService.set(ak, key, 1);
            }else if (count < maxCount){
                redisService.incr(ak, key);
            }else {
                render(response, CodeMsg.ACCESS_FREQUENT);
                return false;
            }
        }

        return true;
    }


    // 返回登录结果
    private void render(HttpServletResponse response, CodeMsg codeMsg) throws Exception{
        response.setContentType("application/json;charset=UTF-8");

        OutputStream out = response.getOutputStream();

        // 直接返回json格式
        String str = JSON.toJSONString(Result.error(codeMsg));

        out.write(str.getBytes("UTF-8"));
        out.flush();
        out.close();
    }



    // 获取用户信息
    private User getSeckillUser(HttpServletRequest request, HttpServletResponse response){
        String paramToken = request.getParameter(UserService.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieValue(request, UserService.COOKIE_NAME_TOKEN);

        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)){
            return null;
        }

        String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
        return userService.getByToken(response, token);
    }

    // 遍历获取指定的cookie
    private String getCookieValue(HttpServletRequest request, String cookieName){
        Cookie[] cookies = request.getCookies();

        if(cookies == null || cookies.length <= 0){
            return null;
        }

        for (Cookie cookie : cookies){
            if (cookie.getName().equals(cookieName)){
                return cookie.getValue();
            }
        }
        return null;
    }
}
