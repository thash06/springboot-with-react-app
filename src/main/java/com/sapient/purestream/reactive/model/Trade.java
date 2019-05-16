package com.sapient.purestream.reactive.model;

import com.sapient.purestream.reactive.constants.OrderStatus;
import com.sapient.purestream.reactive.constants.Side;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "Trades")
@Data
public class Trade {
    @Id
    private Long id;
    private Integer quantity;
    private Integer remainingQuantity = -1;
    private Side side;
    private String ticker;
    private String orderType;
    private OrderStatus orderStatus;
    private Date orderCreated;

    public Trade() {}

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Trade))
            return false;
        Trade t = (Trade) o;
        if (this.id == t.id && this.ticker.equals(t.ticker)
                && this.orderType.equals(t.orderType)
                && this.quantity == t.quantity)
            return true;
        else
            return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        long x = id;
        while (x != 0) {
            hash = hash * 31 + (int) (x % 10);
            x = x / 10;
        }

        return hash;
    }
}