package com.sapient.purestream.reactive.entity;


import com.sapient.purestream.reactive.constants.TradeStrategy;
import com.sapient.purestream.reactive.model.Trade;

public class TickerStrategy {
    private String ticker;
    private String strategy;

    public TickerStrategy(String ticker, String strategy){
        this.ticker = ticker;
        this.strategy = strategy;
    }

}

