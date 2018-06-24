package com.heiku.panicbuy.service;


import com.heiku.panicbuy.dao.UserDao;
import com.heiku.panicbuy.entity.User;
import com.heiku.panicbuy.exception.GlobalException;
import com.heiku.panicbuy.redis.RedisService;
import com.heiku.panicbuy.redis.UserKey;
import com.heiku.panicbuy.result.CodeMsg;
import com.heiku.panicbuy.util.MD5Util;
import com.heiku.panicbuy.util.UUIDUtil;
import com.heiku.panicbuy.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class UserService {

    public static final String COOKIE_NAME_TOKEN = "token";

    @Autowired
    private UserDao userDao;

    @Autowired
    private RedisService redisService;

    public User getById(long id){
        return userDao.getById(id);
    }


    public boolean login(HttpServletResponse response, LoginVo vo){
        if (vo == null){
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }

        String mobile = vo.getMobile();
        String password = vo.getPassword();

        // 验证手机号即用户是否存在
        User user = getById(Long.parseLong(mobile));
        if (user == null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }

        // 验证密码
        String dbPwd = user.getPassword();
        String dbSalt = user.getSalt();
        String calPwd = MD5Util.fromPassToDBPass(password, dbSalt);
        if (!calPwd.equals(dbPwd)){
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }

        // 添加 cookie
        String token = UUIDUtil.uuid();
        addCookie(response, token, user);

        return true;
    }


    // 通过token获取用户信息
    public User getByToken(HttpServletResponse response, String token) {
        if (StringUtils.isEmpty(token)){
            return null;
        }

        User user =  redisService.get(UserKey.token, token, User.class);

        //延长cookie
        if (user != null) {
            addCookie(response, token, user);
        }
        return user;
    }


    // Cookie更新，添加
    private void addCookie(HttpServletResponse response, String token, User user){
        redisService.set(UserKey.token, token, user);

        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
        cookie.setMaxAge(UserKey.token.expireSeconds());
        cookie.setPath("/");

        response.addCookie(cookie);
    }
}
