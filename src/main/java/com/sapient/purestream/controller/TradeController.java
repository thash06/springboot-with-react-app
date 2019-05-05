package com.sapient.purestream.controller;

import com.sapient.purestream.RestClient;
import com.sapient.purestream.constants.OrderStatus;
import com.sapient.purestream.constants.Side;
import com.sapient.purestream.exceptions.ResourceNotFoundException;
import com.sapient.purestream.model.Trade;
import com.sapient.purestream.service.SequeneGeneratorService;
import com.sapient.purestream.service.TradeExecutionService;
import com.sapient.purestream.service.TradeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Slf4j
public class TradeController {

    private final TradeService tradeService;
    private final SequeneGeneratorService sequeneGeneratorService;
    private final TradeExecutionService tradeExecutionService;
    private final RestClient restrClient;

    public TradeController(TradeService tradeService, SequeneGeneratorService sequeneGeneratorService
            , TradeExecutionService tradeExecutionService, RestClient restrClient) {
        this.tradeService = tradeService;
        this.sequeneGeneratorService = sequeneGeneratorService;
        this.tradeExecutionService = tradeExecutionService;
        this.restrClient = restrClient;
    }

    @PostMapping("/trade")
    public ResponseEntity<Trade> createTrade(@RequestBody Trade trade) {
        Long _id = sequeneGeneratorService.generateSequence("Trades");
        trade.setId(_id);
        trade.setOrderCreated(new Date());
        trade.setOrderStatus(OrderStatus.NEW);
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
        return new ResponseEntity<>(tradeById.get(), HttpStatus.OK);
    }

    @GetMapping("/processOrder")
    public ResponseEntity<Object> getStreamingOrders() {
        List<Trade> trades = this.tradeService.findByOrderStatus(OrderStatus.NEW.toString());
        trades.addAll(this.tradeService.findByOrderStatus(OrderStatus.RESTING.toString()));
        return new ResponseEntity<>(getStreamingMatch(trades), HttpStatus.OK);
    }

    @DeleteMapping("/deleteById/{id}")
    public ResponseEntity<Object> deleteById(@PathVariable @NotNull Long id) throws ResourceNotFoundException {
        Optional<Trade> tradeById = this.tradeService.findById(id);
        if (!tradeById.isPresent()) {
            throw new ResourceNotFoundException("Trade with ID " + id + " NOT FOUND");
        }
        this.tradeService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    /*
     * exposing enums will avoid hardcoding in front end
     */
    @GetMapping("/sides")
    public ResponseEntity<Object> getSidesEnum() {
        Map<String, String> sidesMap = Arrays.stream(Side.values())
                .collect(Collectors.toMap(Enum::toString, Enum::toString, (a, b) -> b));
        return new ResponseEntity<>(sidesMap, HttpStatus.OK);
    }

    private List<Trade> getStreamingMatch(List<Trade> trades) {
        List<Trade> matched = new ArrayList<>();
     /*   trades.stream().map(t -> this.tradeService.findByOrderTypeSideTicker(t.getOrderType(),
                t.getSide().toString(), t.getTicker()))
                .filter(byOrderTypeSideTicker -> byOrderTypeSideTicker.size() > 0)
                .flatMap(Collection::stream).forEach(inner -> {
            inner.setOrderStatus(OrderStatus.STREAMING);
            this.tradeService.createTrade(inner);
            matched.add(inner);
        }); */

        trades.stream().map(t -> {
            trades.stream().filter(t2 -> t2.getSide() != t.getSide()
                    && t2.getTicker().equals(t.getTicker())
                    && t2.getOrderType().equals(t.getOrderType())
            ).findAny().ifPresent(t2 -> {
                t.setOrderStatus(OrderStatus.STREAMING);
                this.tradeService.createTrade(t);
                matched.add(t);
            });
            return t;
        }).count();

        // if (!matched.isEmpty()) {
        //tradeExecutionService.tradeExecution(matched);
        // ResponseEntity<String> response = restrClient.sendExecutionMsg(matched);
        // System.out.println("TradeController: returned from exec service : " + response.getBody());
        // }
        System.out.println("TradeController: complete");

        return matched;
    }
}
