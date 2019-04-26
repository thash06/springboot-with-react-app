package com.sapient.purestream.controller;

import com.sapient.purestream.constants.OrderStatus;
import com.sapient.purestream.constants.Side;
import com.sapient.purestream.exceptions.ResourceNotFoundException;
import com.sapient.purestream.model.Trade;
import com.sapient.purestream.service.SequeneGeneratorService;
import com.sapient.purestream.service.TradeProcessor;
import com.sapient.purestream.service.TradeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class TradeController {

    private final TradeService tradeService;
    private final SequeneGeneratorService sequeneGeneratorService;
    // private Queue<Trade> queue;

    public TradeController(TradeService tradeService, SequeneGeneratorService sequeneGeneratorService) {
        this.tradeService = tradeService;
        this.sequeneGeneratorService = sequeneGeneratorService;
        //  this.queue = new LinkedList<>();
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
    public ResponseEntity<Object> getCompletedOrders() {
        return new ResponseEntity<>(processOrder(), HttpStatus.OK);
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

    public List<Trade> processOrder() {
        List<Trade> trades = this.tradeService.findByOrderTypes(OrderStatus.NEW.toString());
        ExecutorService service = Executors.newCachedThreadPool();

        for (Trade t : trades) {
            service.execute(new TradeProcessor(t, this.tradeService));
        }
        service.shutdown();
        try {
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return trades;
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
}
