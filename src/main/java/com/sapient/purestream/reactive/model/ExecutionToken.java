package com.sapient.purestream.reactive.model;

public class ExecutionToken {
    private String ticker;
    private Trade buyTrade;
    private Trade sellTrade;
    private ConsolidatedTape ctape;

    public ExecutionToken(String ticker, Trade buyTrade, Trade sellTrade) {
        this.ticker = ticker;
        this.buyTrade = buyTrade;
        this.sellTrade = sellTrade;
        this.ctape = null;
    }

    public String getTicker() {
        return ticker;
    }

    public Trade getBuyTrade() {
        return buyTrade;
    }

    public Trade getSellTrade() {
        return sellTrade;
    }

    public ConsolidatedTape getCtape() {
        return ctape;
    }

    public void setCtape(ConsolidatedTape ctape) {
        this.ctape = ctape;
    }
}
