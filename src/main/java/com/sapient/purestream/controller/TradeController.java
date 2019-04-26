package com.sapient.purestream.controller;

import com.sapient.purestream.constants.OrderStatus;
import com.sapient.purestream.constants.Side;
import com.sapient.purestream.exceptions.ResourceNotFoundException;
import com.sapient.purestream.model.Trade;
import com.sapient.purestream.service.SequeneGeneratorService;
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

    public TradeController(TradeService tradeService, SequeneGeneratorService sequeneGeneratorService) {
        this.tradeService = tradeService;
        this.sequeneGeneratorService = sequeneGeneratorService;

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
        trades.stream().map(t -> this.tradeService.findByOrderTypeSideTicker(t.getOrderType(),
                t.getSide().toString(), t.getTicker()))
                .filter(byOrderTypeSideTicker -> byOrderTypeSideTicker.size() > 0)
                .flatMap(Collection::stream).forEach(inner -> {
            inner.setOrderStatus(OrderStatus.STREAMING);
            this.tradeService.createTrade(inner);
            matched.add(inner);
        });
        return matched;
    }
}
