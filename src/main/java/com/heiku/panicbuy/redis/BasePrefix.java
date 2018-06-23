package com.heiku.panicbuy.redis;

public abstract class BasePrefix implements KeyPrefix {

    private int expireSeconds;
    private String prefix;

    // 0 永不过期
    public BasePrefix(String prefix){
        this(0, prefix);
    }


    public BasePrefix(int expireSeconds, String prefix) {
        this.expireSeconds = expireSeconds;
        this.prefix = prefix;
    }


    @Override
    public int expireSeconds() {
        return expireSeconds;
    }


    @Override
    public String getPrefix() {

        String className = getClass().getSimpleName();
        return className + ":" + prefix;
    }
}
