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

import java.time.Duration;
import java.util.stream.Collectors;


@Service
public class TradeService {
    private static final Logger LOG = LoggerFactory.getLogger(TradeService.class);

    @Autowired
    private ConsolidatedTapeService consolidatedTapeService;

    private final TradeRepository tradeRepository;

    public TradeService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    public Mono<Trade> createTrade(Trade trade) {
        trade.setPercentage((1-((double)trade.getRemainingQuantity()/(double)trade.getQuantity()))*100);
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

//                .map(t -> {
//                    t.setOrderStatus(OrderStatus.STREAMING);
//                    return t;
//                });
        LOG.info(" Matched Trades for {} are {}", newTrade, matchedTrades);
//        quoteStream.takeUntil(q->newTrade.getRemainingQuantity()==0).doOnNext(qt -> this.executeTrade(qt, newTrade)).subscribe();
      matchedTrades.takeUntil(trade -> {
            if (newTrade.getRemainingQuantity()>0 && trade.getRemainingQuantity() > newTrade.getQuantity()) {
                quoteStream.takeUntil(q->newTrade.getRemainingQuantity()==0).doOnNext(qt -> this.executeTrade(qt, newTrade)).subscribe();
                quoteStream.takeUntil(q->newTrade.getRemainingQuantity()==0).doOnNext(qt -> this.executeTrade(qt, trade)).subscribe();
                return trade.getRemainingQuantity()==(trade.getRemainingQuantity()-newTrade.getQuantity()) && newTrade.getRemainingQuantity()==0;
            }else if (trade.getRemainingQuantity()>0 && trade.getRemainingQuantity() < newTrade.getQuantity()){
                quoteStream.takeUntil(q->trade.getRemainingQuantity()==0).doOnNext(qt -> this.executeTrade(qt, newTrade)).subscribe();
                quoteStream.takeUntil(q->trade.getRemainingQuantity()==0).doOnNext(qt -> this.executeTrade(qt, trade)).subscribe();
                return newTrade.getRemainingQuantity()==(newTrade.getQuantity()-trade.getRemainingQuantity()) && trade.getRemainingQuantity()==0;
            }else if (trade.getRemainingQuantity()>0 && newTrade.getRemainingQuantity()>0 && trade.getRemainingQuantity() == newTrade.getQuantity()) {
                quoteStream.takeUntil(q-> newTrade.getRemainingQuantity()==0).doOnNext(qt -> this.executeTrade(qt, newTrade)).subscribe();
                quoteStream.takeUntil(q-> trade.getRemainingQuantity()==0 ).doOnNext(qt -> this.executeTrade(qt, trade)).subscribe();
                return trade.getRemainingQuantity()==0 && newTrade.getRemainingQuantity()==0;
            }
return true;
        }).subscribe();
        return   matchedTrades;
    }

    private void executeTrade(Quote quote, Trade trade) {
        if (quote.getTicker().equals(trade.getTicker())) {
            this.tradeRepository.save(trade).doOnNext(trade1 -> {
                    trade1.setRemainingQuantity(trade1.getRemainingQuantity()-1);
                    trade1.setPercentage((1-((double)trade.getRemainingQuantity()/(double)trade.getQuantity()))*100);
                    if (trade1.getOrderStatus() != OrderStatus.EXECUTING){
                        trade1.setOrderStatus(OrderStatus.EXECUTING);
                    }
                    if(trade1.getRemainingQuantity()==0 ){
                        trade1.setOrderStatus(OrderStatus.COMPLETE);
                        tradeRepository.delete(trade1);
                    }
                }).subscribe();
//            if((remaining.getOrderStatus()==OrderStatus.EXECUTING && trade.getOrderStatus()==OrderStatus.COMPLETE)||(remaining.getOrderStatus()==OrderStatus.COMPLETE && trade.getOrderStatus()==OrderStatus.EXECUTING)){
//                dis.dispose();
//            }
        }
    }

}