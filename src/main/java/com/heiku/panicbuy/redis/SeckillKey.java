package com.heiku.panicbuy.redis;

public class SeckillKey extends BasePrefix {

    public SeckillKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }


    public static SeckillKey isGoodsEmpty = new SeckillKey(0,"ge");
    public static SeckillKey seckillPath = new SeckillKey(60,"sp");
    public static SeckillKey seckillVerifyCode = new SeckillKey(300, "vc");
}
