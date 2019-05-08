package com.sapient.purestream.model;

public class ExecutionToken {
    private String ticker;
    private Trade buyTrade;
    private Trade sellTrade;
    private ConsolidatedTape ctape;
    private volatile boolean isInExec;

    public ExecutionToken(String ticker, Trade buyTrade, Trade sellTrade) {
        this.ticker = ticker;
        this.buyTrade = buyTrade;
        this.sellTrade = sellTrade;
        this.ctape = null;
        this.isInExec = false;
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

    public boolean isInExec() {
        return isInExec;
    }

    public void setInExec(boolean inExec) {
        isInExec = inExec;
    }

}
