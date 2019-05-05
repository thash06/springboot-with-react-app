package com.sapient.purestream.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ConsolidatedTape implements Serializable {

    private String ticker;
    private int quantity;
    private double price;
    private LocalDateTime timestamp;

    public ConsolidatedTape(String ticker, int quantity, double price, LocalDateTime timestamp) {
        this.ticker = ticker;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = timestamp;
    }

    public String toString() {
        return "[ticker=" + ticker + ",quantity=" + quantity + ",price=" + price + ",timestamp=" + timestamp + "]";
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

}
