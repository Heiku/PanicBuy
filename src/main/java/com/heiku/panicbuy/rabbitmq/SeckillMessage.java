package com.heiku.panicbuy.rabbitmq;

import com.heiku.panicbuy.entity.User;

public class SeckillMessage {

    private User user;
    private Long goodsId;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }
}
