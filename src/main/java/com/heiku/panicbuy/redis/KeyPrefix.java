package com.heiku.panicbuy.redis;

public interface KeyPrefix {

    int expireSeconds();

    String getPrefix();
}
