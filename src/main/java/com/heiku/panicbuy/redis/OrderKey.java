package com.heiku.panicbuy.redis;

public class OrderKey extends BasePrefix {

    private OrderKey(String prefix) {
        super(prefix);
    }


    public static OrderKey getSeckillOrderByUidGid = new OrderKey("skug");
}
