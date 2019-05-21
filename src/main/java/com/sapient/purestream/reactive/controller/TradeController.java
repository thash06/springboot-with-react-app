package com.sapient.purestream.reactive.controller;

import com.sapient.purestream.reactive.constants.OrderStatus;
import com.sapient.purestream.reactive.exceptions.ResourceNotFoundException;
import com.sapient.purestream.reactive.model.Trade;
import com.sapient.purestream.reactive.service.MongoSequenceGeneratorService;
import com.sapient.purestream.reactive.service.TradeService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
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
        LOG.info(" displayAll orders ...");
        return new ResponseEntity<>(this.tradeService.displayTrades(), HttpStatus.OK);
    }

    @PostMapping("/trade")
    public Mono<ResponseEntity<Trade>> createTrade(@RequestBody Trade trade) {
        LOG.info(" createTrade {} ...", trade);
        return Mono.just(mongoSequenceGeneratorService.generateSequence("Trades"))
                .flatMap(seqNo -> {
                    LOG.info("The seqNo is {}", seqNo);
                            trade.setId(seqNo);
                            trade.setOrderCreated(new Date());
                            trade.setOrderStatus(OrderStatus.NEW);

                    return tradeService.createTrade(trade);
                        }
                )
                .map(newTrade -> {
                    this.getStreamingOrders(newTrade);
                    LOG.info("The new trade values are {}", newTrade);
                    return new ResponseEntity<>(newTrade, HttpStatus.OK);})
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/deleteById/{id}")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable @NotNull Long id) throws ResourceNotFoundException {
        LOG.info(" deleteById {} ...", id);
        return tradeService.findById(id)
                .flatMap(existingOrder ->
                        tradeService.deleteById(id)
                                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)))
                )
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/processOrder")
    public Mono<ResponseEntity> getStreamingOrders(@RequestBody Trade trade) {
        LOG.info(" getStreamingOrders {} ...", trade);
        Flux<Trade> newTrades = this.tradeService.findByOrderStatus(OrderStatus.NEW.toString());
        Flux<Trade> restingTrades = this.tradeService.findByOrderStatus(OrderStatus.RESTING.toString());
        Flux<Trade> newAndRestringTrades = newTrades.concatWith(restingTrades);
        //return new ResponseEntity<>(getStreamingMatch(trade, newAndRestringTrades), HttpStatus.OK);
        return Mono.just(new ResponseEntity(
                tradeService.getStreamingMatch(trade, newAndRestringTrades), HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<Flux>(Flux.empty(), HttpStatus.NOT_FOUND));
    }

//    /*
//    Main method - will take trades and try to match
//     */
//    private Flux<Trade> getStreamingMatch(Trade newTrade, Flux<Trade> newAndRestringTrades) {
//        Flux<Trade> matchedTrades = newAndRestringTrades
//                .filter(trade -> trade.getTicker().equals(newTrade.getTicker())
//                        && trade.getOrderType().equals(newTrade.getOrderType())
//                        && !trade.getSide().equals(newTrade.getSide()));
//        LOG.info(" Matched Trades for {} are {}", newTrade, matchedTrades.blockFirst());
//
//        return matchedTrades;
//    }
}
