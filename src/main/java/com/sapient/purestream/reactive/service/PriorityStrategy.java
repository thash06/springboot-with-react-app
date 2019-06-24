package com.sapient.purestream.reactive.service;

import com.sapient.purestream.reactive.constants.NumConstants;
import com.sapient.purestream.reactive.constants.OrderStatus;
import com.sapient.purestream.reactive.controller.TradeController;
import com.sapient.purestream.reactive.model.Trade;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedList;
import java.util.List;

public class PriorityStrategy {
    private int priorityQuantity;

    private TradeService tradeService;

    private boolean currTradeIsPriority;

    private Trade haltedTrade;

    private Flux<Trade> priorityTrades;

    public PriorityStrategy(TradeService tradeService){
        this.tradeService = tradeService;
        priorityQuantity = NumConstants.PRIORITY_QUANTITY;
        priorityTrades = null;
    }

    public void updatePriorityTrades(){

        priorityTrades = getAllAvailableTrades().filter( t -> t.getQuantity() >= priorityQuantity);
    }

    public Flux<Trade> getPriorityTrades(){
        return priorityTrades;
    }

    public Flux<Trade> updateMatchedTrades(Trade newTrade) {
        updatePriorityTrades();
        Flux<Trade> updatedTrades = priorityTrades
                .filter(trade1 -> trade1.getOrderType().equals(newTrade.getOrderType())
                        && trade1.getTicker().equals(newTrade.getTicker())
                        && !trade1.getSide().equals(newTrade.getSide())
                        &&  trade1.getRemainingQuantity() >= tradeService.getSubtractedQuantity(newTrade)
                        && trade1.getOrderStatus() != OrderStatus.COMPLETE);
        return updatedTrades;
    }

    public Flux<Trade> getPriorityMatchedTrades(Flux<Trade> matchedTrades){
        Flux<Trade> priorityTrades = matchedTrades
                .filter(trade -> trade.isPriority());
        return priorityTrades;

    }

    public Flux<Trade> getNonPriorityMatchedTrades(Flux<Trade> trades){
        Flux<Trade> regTrades = trades
                .filter(trade -> !trade.isPriority());
        return regTrades;
    }

    public Flux<Trade> getAllAvailableTrades(){
        Flux<Trade> newTrades = this.tradeService.findByOrderStatus(OrderStatus.NEW.toString());
        Flux<Trade> restingTrades = this.tradeService.findByOrderStatus(OrderStatus.RESTING.toString());
        Flux<Trade> newAndRestringTrades = newTrades.concatWith(restingTrades);
        return newAndRestringTrades;
    }
    public void executePriorityTrade(){
        if(priorityTrades == null)
            return;
        Flux<Trade> newAndRestringTrades = getAllAvailableTrades();
        priorityTrades.subscribe(t -> tradeService.getStreamingMatch(t, newAndRestringTrades));
    }

    public void matchedTradesInOrder(Flux<Trade> matchedTrades){
        List<Mono<Trade>> tradeList = new LinkedList<>();
        Flux<Trade> priority = getPriorityMatchedTrades(matchedTrades);
        Flux<Trade> normal = getNonPriorityMatchedTrades(matchedTrades);


    }

    public void switchToPriority(Trade trade, Trade newTrade){
        updatePriorityTrades();
        if(!trade.isPriority() && updateMatchedTrades(newTrade) != null) {
            trade.setOrderStatus(OrderStatus.RESTING);
            if(!newTrade.isPriority() && getPriorityTrades()
                    .filter(t -> t.getTicker().equals(newTrade.getTicker())
                            && t.getSide().equals(newTrade.getSide())
                            && t.getOrderType().equals(newTrade.getOrderType()))!= null){
                newTrade.setOrderStatus(OrderStatus.RESTING);
                executePriorityTrade();
            }
            else{
                tradeService.getStreamingMatch(newTrade, getAllAvailableTrades());
            }
        }
    }



}
