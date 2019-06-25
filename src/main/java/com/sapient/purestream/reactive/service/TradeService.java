package com.sapient.purestream.reactive.service;

import com.sapient.purestream.reactive.constants.OrderStatus;
import com.sapient.purestream.reactive.entity.Quote;
import com.sapient.purestream.reactive.model.Trade;
import com.sapient.purestream.reactive.repository.TradeRepository;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Observable;
import java.util.stream.Collectors;


@Service
public class TradeService {
    private static final Logger LOG = LoggerFactory.getLogger(TradeService.class);

    @Autowired
    private ConsolidatedTapeService consolidatedTapeService;

    private final TradeRepository tradeRepository;

    private PriorityStrategy priorityStrategy;

    public TradeService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
        priorityStrategy = new PriorityStrategy(this);
    }

    public Mono<Trade> createTrade(Trade trade) {
        trade.setPercentage((1-((double)trade.getRemainingQuantity()/(double)trade.getQuantity()))*100);
//        if(trade.isPriority())
//            priorityStrategy.updatePriorityTrades();
        return this.tradeRepository.save(trade);
    }
    public Flux<Trade> findByTicker(String ticker) {
        return this.tradeRepository.findByTicker(ticker);
    }

    public Flux<Trade> displayTrades() {
        return this.tradeRepository.findAll();
    }

    public Mono<Trade> findById(Long id) {
        return this.tradeRepository.findById(id);
    }

    public Mono<Void> deleteById(Long id) {
        return this.tradeRepository.deleteById(id);
    }

    public Flux<Trade> findByOrderStatus(String type) {
        return this.tradeRepository.findByOrderStatus(type);
    }

    public Flux<Trade> findByQuantity(int quantity) {
        return this.tradeRepository.findByQuantity(quantity);
    }

    public Flux<Trade> findByOrderTypeSideTicker(String orderType, String side, String ticker) {
        return this.tradeRepository.findByOrdertypeSideAndTicker(orderType, side, ticker);
    }

    /*
        Main method - will take trades and try to match
    */
    public Flux<Trade> getStreamingMatch(Trade newTrade, Flux<Trade> newAndRestringTrades) {

        Flux<Quote> quoteStream = consolidatedTapeService.fetchQuoteStream();
        Flux<Trade> matchedTrades = newAndRestringTrades
                .filter(trade -> trade.getTicker().equals(newTrade.getTicker())
                        && trade.getOrderType().equals(newTrade.getOrderType())
                        && !trade.getSide().equals(newTrade.getSide()));
        Flux<Trade> priorityTrades = priorityStrategy.getPriorityMatchedTrades(matchedTrades);
        Flux<Trade> normalTrades = priorityStrategy.getNonPriorityMatchedTrades(matchedTrades);
//                        && (trade.getOrderStatus() != OrderStatus.COMPLETE));

//                .map(t -> {
//                    t.setOrderStatus(OrderStatus.STREAMING);
//                    return t;
//                });
        LOG.info(" Matched Trades for {} are {}", newTrade, matchedTrades);
//        quoteStream.takeUntil(q->newTrade.getRemainingQuantity()==0).doOnNext(qt -> this.executeTrade(qt, newTrade)).subscribe();

//        executeTradeHelper(quoteStream, newTrade, matchedTrades);
        executeTradeHelper(quoteStream, newTrade, priorityTrades.switchIfEmpty(normalTrades));
        if(priorityTrades.subscribe().isDisposed() && newTrade.getRemainingQuantity() > 0)
            executeTradeHelper(quoteStream,newTrade,normalTrades);
         return   matchedTrades;
    }

    private void executeTradeHelper(Flux<Quote> quoteStream, Trade newTrade, Flux<Trade> allMatchedTrades ){
        allMatchedTrades.takeUntil(trade -> {
            if (newTrade.getRemainingQuantity() > 0 && trade.getRemainingQuantity() > newTrade.getQuantity()) {
                quoteStream.takeUntil(q -> newTrade.getRemainingQuantity() == 0)
                        .doOnNext(qt -> this.executeTrade(qt, trade, newTrade)).subscribe();
                return trade.getRemainingQuantity() == (trade.getRemainingQuantity() - newTrade.getQuantity()) && newTrade.getRemainingQuantity() == 0;
            } else if (trade.getRemainingQuantity() > 0 && trade.getRemainingQuantity() < newTrade.getQuantity()) {
                quoteStream.takeUntil(q -> trade.getRemainingQuantity() == 0)
                        .doOnNext(qt -> this.executeTrade(qt, trade, newTrade)).subscribe();
                return newTrade.getRemainingQuantity() == (newTrade.getQuantity() - trade.getRemainingQuantity()) && trade.getRemainingQuantity() == 0;
            } else if (trade.getRemainingQuantity() > 0 && newTrade.getRemainingQuantity() > 0 && trade.getRemainingQuantity() == newTrade.getQuantity()) {
                quoteStream.takeUntil(q -> newTrade.getRemainingQuantity() == 0)
                        .doOnNext(qt -> this.executeTrade(qt, trade, newTrade)).subscribe();
                return trade.getRemainingQuantity() == 0 && newTrade.getRemainingQuantity() == 0;
            }
            return true;
        }).subscribe();
    }

    private void executeTrade(Quote quote, Trade trade, Trade newTrade) {
//        try{
//            Thread.sleep(500);
//        }catch(Exception e){
//
//        }
//        priorityStrategy.switchToPriority(trade, newTrade);
        int subtractedQuantity = getSubtractedQuantity(newTrade);
        if(trade.getRemainingQuantity() == 0){
            trade.setOrderStatus(OrderStatus.COMPLETE);
            if(newTrade.getRemainingQuantity() == 0){
                newTrade.setOrderStatus(OrderStatus.COMPLETE);
                return;
            }else{
                newTrade.setOrderStatus(OrderStatus.RESTING);
                return;
            }
        }else{
            if(newTrade.getRemainingQuantity() == 0) {
                newTrade.setOrderStatus(OrderStatus.COMPLETE);
                trade.setOrderStatus(OrderStatus.RESTING);
                return;
            }
        }

        if (quote.getTicker().equals(trade.getTicker())) {
   //         this.tradeRepository.save(trade).doOnNext(trade1 -> {

                trade.setRemainingQuantity(trade.getRemainingQuantity() - subtractedQuantity);
                trade.setPercentage((1-((double)trade.getRemainingQuantity()/(double)trade.getQuantity()))*100);
                newTrade.setRemainingQuantity(newTrade.getRemainingQuantity() - subtractedQuantity);
                newTrade.setPercentage((1-((double)newTrade.getRemainingQuantity()/(double)newTrade.getQuantity()))*100);
                if (trade.getOrderStatus() != OrderStatus.EXECUTING){
                    trade.setOrderStatus(OrderStatus.EXECUTING);
                }
                if(trade.getRemainingQuantity()==0 ){
                    trade.setOrderStatus(OrderStatus.COMPLETE);
                    tradeRepository.delete(trade);
                }
                if (newTrade.getOrderStatus() != OrderStatus.EXECUTING){
                    newTrade.setOrderStatus(OrderStatus.EXECUTING);
                }
                if(newTrade.getRemainingQuantity()==0){
                    newTrade.setOrderStatus(OrderStatus.COMPLETE);
                    tradeRepository.delete(newTrade);
                }
                if(newTrade.getOrderStatus() != trade.getOrderStatus() ){
                    if(newTrade.getOrderStatus()== OrderStatus.COMPLETE)
                        trade.setOrderStatus(OrderStatus.RESTING);
                    else
                        newTrade.setOrderStatus(OrderStatus.RESTING);
                }
  //          }).subscribe();
            this.tradeRepository.save(trade).subscribe();
            this.tradeRepository.save(newTrade).subscribe();
//            if((remaining.getOrderStatus()==OrderStatus.EXECUTING && trade.getOrderStatus()==OrderStatus.COMPLETE)||(remaining.getOrderStatus()==OrderStatus.COMPLETE && trade.getOrderStatus()==OrderStatus.EXECUTING)){
//                dis.dispose();
//            }
        }


    }
    public int getSubtractedQuantity(Trade trade){
        String strategy = trade.getOrderType();
        int subtractedQuantity = 0;
        switch(strategy){
            case "5-10%":
                subtractedQuantity = (int)(trade.getQuantity()* .10);
                break;
            case "10-20%":
                subtractedQuantity = (int)(trade.getQuantity()*.20);
                break;

        }
        return subtractedQuantity;
    }

}