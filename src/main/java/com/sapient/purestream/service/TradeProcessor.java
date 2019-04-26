package com.sapient.purestream.service;

import com.sapient.purestream.constants.OrderStatus;
import com.sapient.purestream.model.Trade;

public class TradeProcessor implements Runnable {

    Trade tradeObj;
    TradeService tradeService;

    public TradeProcessor(Trade tradeObj,TradeService tradeService) {
        this.tradeObj = tradeObj;
        this.tradeService = tradeService;
    }

    @Override
    public void run() {
        try {
            this.tradeObj.setOrderStatus(OrderStatus.COMPLETE);
            this.tradeService.createTrade(this.tradeObj);
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}