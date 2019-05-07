package com.sapient.purestream.reactive.controller;

import com.sapient.purestream.constants.OrderStatus;
import com.sapient.purestream.model.Trade;
import com.sapient.purestream.reactive.service.MongoSequenceGeneratorService;
import com.sapient.purestream.reactive.service.TradeService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Date;

/**
 * @author tarhashm
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class TradeController {
    private static final Logger LOG = LoggerFactory.getLogger(TradeController.class);

    private final TradeService tradeService;
    private final MongoSequenceGeneratorService mongoSequenceGeneratorService;

    public TradeController(TradeService tradeService, MongoSequenceGeneratorService mongoSequenceGeneratorService) {
        this.tradeService = tradeService;
        this.mongoSequenceGeneratorService = mongoSequenceGeneratorService;
    }

    @GetMapping("/showAll")
    public ResponseEntity<Object> displayAll() {
        return new ResponseEntity<>(this.tradeService.displayTrades(), HttpStatus.OK);
    }

    @PostMapping("/trade")
    public ResponseEntity<Mono<Trade>> createTrade(@RequestBody Trade trade) {
        Long id = mongoSequenceGeneratorService.generateSequence("Trades");
        trade.setId(id);
        trade.setOrderCreated(new Date());
        trade.setOrderStatus(OrderStatus.NEW);
        Mono<Trade> newTrade = this.tradeService.createTrade(trade);
        ResponseEntity<Mono<Trade>> monoResponseEntity = new ResponseEntity<>(newTrade, HttpStatus.CREATED);
        return monoResponseEntity;
    }
}
