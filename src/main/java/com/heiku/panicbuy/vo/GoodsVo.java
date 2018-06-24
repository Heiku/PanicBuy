package com.heiku.panicbuy.vo;

import com.heiku.panicbuy.entity.Goods;

import java.util.Date;

/**
 * extends Goods，保存Goods信息
 */
public class GoodsVo extends Goods {

    private Double seckillPrice;
    private Integer stockCount;
    private Date startTime;
    private Date endTime;

    public Integer getStockCount() {
        return stockCount;
    }

    public void setStockCount(Integer stockCount) {
        this.stockCount = stockCount;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Double getSeckillPrice() {
        return seckillPrice;
    }

    public void setSeckillPrice(Double seckillPrice) {
        this.seckillPrice = seckillPrice;
    }
}
