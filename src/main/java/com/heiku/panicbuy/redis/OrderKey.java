package com.heiku.panicbuy.redis;

public class OrderKey extends BasePrefix {

    private OrderKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }
}
