package com.heiku.panicbuy.redis;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class RedisService {

    @Autowired
    private JedisPool jedisPool;


    /**
     * 获取key, value
     *
     * @param prefix
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T get(KeyPrefix prefix, String key, Class<T> clazz){
        Jedis jedis = null;

        try {
            jedis = jedisPool.getResource();

            // 生成key
            String realKey = prefix.getPrefix() + key;
            String str = jedis.get(realKey);

            T t = strToBean(str, clazz);
            return t;
        }finally {
            returnToPool(jedis);
        }
    }


    /**
     * 设置 key, value
     *
     * @param prefix
     * @param key
     * @param value
     * @param <T>
     * @return
     */
    public <T> boolean set(KeyPrefix prefix, String key, T value){
        Jedis jedis = null;

        try {
            jedis = jedisPool.getResource();

            String str = beanToStr(value);
            if (str == null || str.length() <= 0){
                return false;
            }

            // 生成key
            String realKey = prefix.getPrefix() + key;
            int seconds = prefix.expireSeconds();
            if (seconds <= 0){
                jedis.set(realKey, str);
            }else {
                jedis.setex(realKey, seconds, str);
            }
            return true;
        }finally {
            returnToPool(jedis);
        }
    }


    /**
     * 指定key, 是否存在value
     *
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> boolean exists(KeyPrefix prefix, String key){
        Jedis jedis = null;

        try {
            jedis = jedisPool.getResource();

            String realKey = prefix.getPrefix() + key;

            return jedis.exists(realKey);
        }finally {
            returnToPool(jedis);
        }
    }


    public <T> boolean delete(KeyPrefix prefix, String key){
        Jedis jedis = null;

        try {
            jedis = jedisPool.getResource();

            String realKey = prefix.getPrefix() + key;

            long result =  jedis.del(realKey);

            return result > 0;
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * 原子递增
     *
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> Long incr(KeyPrefix prefix, String key){
        Jedis jedis = null;

        try {
            jedis = jedisPool.getResource();

            String realKey = prefix.getPrefix() + key;
            return jedis.incr(realKey);
        }finally {
            returnToPool(jedis);
        }
    }


    /**
     * 原子递减
     *
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> Long decr(KeyPrefix prefix, String key){
        Jedis jedis = null;

        try {
            jedis = jedisPool.getResource();

            String realKey = prefix.getPrefix() + key;
            return jedis.decr(realKey);
        }finally {
            returnToPool(jedis);
        }
    }

    private <T> T strToBean(String str, Class<T> clazz){

        if (str == null || str.length() <= 0 ||clazz == null){
            return null;
        }

        if (clazz == int.class || clazz == Integer.class){
            return (T)Integer.valueOf(str);
        }else if (clazz == String.class){
            return (T)str;
        }else if (clazz == long.class || clazz == Long.class){
            return (T)Long.valueOf(str);
        }else {
            return JSON.toJavaObject(JSON.parseObject(str), clazz);
        }
    }


    private <T> String beanToStr(T value){
        if (value == null){
            return null;
        }

        Class<?> clazz = value.getClass();
        if (clazz == int.class || clazz == Integer.class){
            return ""+value;
        }else if (clazz == String.class){
            return (String)value;
        }else if ((clazz == long.class || clazz == Long.class)){
            return ""+value;
        }else {
            return JSON.toJSONString(value);
        }
    }


    // jedis关闭，返回连接池
    private void returnToPool(Jedis jedis){
        if (jedis != null)
            jedis.close();
    }


}
