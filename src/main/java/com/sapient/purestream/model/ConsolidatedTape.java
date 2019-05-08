package com.sapient.purestream.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "ConsolidatedTapes")
@Data
public class ConsolidatedTape {

    @Id
    private long id;
    private String ticker;
    private int quantity;
    private double price;
    private LocalDateTime timestamp;

    public ConsolidatedTape() {
    }

    public String toString() {
        return "[ticker=" + ticker + ",quantity=" + quantity + ",price=" + price + ",timestamp=" + timestamp + "]";
    }

}
