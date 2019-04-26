package com.sapient.purestream.model;

import com.sapient.purestream.constants.OrderStatus;
import com.sapient.purestream.constants.Side;
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
    private Side side;
    private String ticker;
    private String orderType;
    private OrderStatus orderStatus;
    private Date orderCreated;

    public Trade() {}

}
