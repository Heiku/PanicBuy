package com.heiku.panicbuy.redis;

public class SeckillKey extends BasePrefix {

    public SeckillKey(String prefix) {
        super(prefix);
    }


    public static SeckillKey isGoodsEmpty = new SeckillKey("ge");
}
