package com.sapient.purestream.controller;

import com.sapient.purestream.constants.OrderStatus;
import com.sapient.purestream.constants.Side;
import com.sapient.purestream.exceptions.ResourceNotFoundException;
import com.sapient.purestream.model.Trade;
import com.sapient.purestream.service.SequeneGeneratorService;
import com.sapient.purestream.service.TradeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.*;

@RestController
@RequestMapping("/api")
public class TradeController {

    private final TradeService tradeService;
    private final SequeneGeneratorService sequeneGeneratorService;

    public TradeController(TradeService tradeService, SequeneGeneratorService sequeneGeneratorService) {
        this.tradeService = tradeService;
        this.sequeneGeneratorService = sequeneGeneratorService;
    }

    @PostMapping("/trade")
    public ResponseEntity<Object> createTrade(@RequestBody Trade trade) {
        Long _id = sequeneGeneratorService.generateSequence("Trades");
        trade.setId(_id);
        trade.setOrderCreated(new Date());
        Trade newTrade = this.tradeService.createTrade(trade);
        return new ResponseEntity<>(newTrade, HttpStatus.CREATED);
    }

    @GetMapping("/showAll")
    public ResponseEntity<Object> displayAll() {
        return new ResponseEntity<>(this.tradeService.displayTrades(), HttpStatus.OK);
    }

    @GetMapping("/findById/{id}")
    public ResponseEntity<Object> findById(@PathVariable @NotNull Long id) {
        Optional<Trade> tradeById = this.tradeService.findById(id);
        if (!tradeById.isPresent()) {
            throw new ResourceNotFoundException("Trade with ID " + id + " NOT FOUND");
        }
        return new ResponseEntity<>(tradeById, HttpStatus.OK);
    }

    /*
     * exposing enums will avoid hardcoding in front end
     */
    @GetMapping("/sides")
    public ResponseEntity<Object> getSidesEnum() {
        List<Side> sides = Arrays.asList(Side.values());
        return new ResponseEntity<>(sides, HttpStatus.OK);
    }

    @GetMapping("/orderstatus")
    public ResponseEntity<Object> getOrderStatusEnum() {
        List<OrderStatus> orderStatus = Arrays.asList(OrderStatus.values());
        return new ResponseEntity<>(orderStatus, HttpStatus.OK);
    }
}
