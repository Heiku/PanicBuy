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


    /**
     *  查询对象，采用对象缓存，先从缓存中读取，读取不到再去数据库
     *
     * @param id
     * @return
     */
    public User getById(long id){

        // 取缓存
        User user = redisService.get(UserKey.getById, "" + id, User.class);
        if (user != null){
            return user;
        }

        // 缓存中没有，数据库读取
        user = userDao.getById(id);
        if (user != null){
            redisService.set(UserKey.getById, "" + id, User.class);
        }

        return user;
    }


    /**
     * 用户密码更改
     *
     * @param id
     * @param password
     * @return
     */
    public boolean updatePwd(String token, long id, String password){

        // 判断对象是否存在
        User user = getById(id);
        if (user == null){
            throw  new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }


        // 数据库更新用户信息
        User updateUser = new User();
        user.setId(id);
        user.setPassword(MD5Util.fromPassToDBPass(password, user.getSalt()));

        userDao.updateUser(user);

        // 缓存更新
        redisService.delete(UserKey.getById, "" + id);
        user.setPassword(updateUser.getPassword());
        redisService.set(UserKey.token, token, user);

        return true;
    }


    /**
     * 登录验证
     *
     * @param response
     * @param vo
     * @return
     */
    public String login(HttpServletResponse response, LoginVo vo){
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

        return token;
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
