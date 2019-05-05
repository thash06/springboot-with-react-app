package com.sapient.purestream.model;

import com.sapient.purestream.constants.ExecutionStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "Executions")
@Data
public class Execution {
    @Id
    private long id;
    private String ticker;
    private int quantity;
    private double price;
    private long buyTradeId;
    private long sellTradeId;
    private ExecutionStatus execStatus;
    private LocalDateTime created;

    public Execution() {
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Execution))
            return false;
        Execution t = (Execution) o;
        if (this.id == t.id && this.ticker.equals(t.ticker)
                && this.quantity == t.quantity && this.buyTradeId == t.buyTradeId
                && this.sellTradeId == t.sellTradeId)
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
