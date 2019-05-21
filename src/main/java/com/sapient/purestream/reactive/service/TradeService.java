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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


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
        return this.tradeRepository.save(trade);
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
        Flux<Trade> matchedTrades = newAndRestringTrades
                .filter(trade -> trade.getTicker().equals(newTrade.getTicker())
                        && trade.getOrderType().equals(newTrade.getOrderType())
                        && !trade.getSide().equals(newTrade.getSide()));
//                .map(t -> {
//                    t.setOrderStatus(OrderStatus.STREAMING);
//                    return t;
//                });
        LOG.info(" Matched Trades for {} are {}", newTrade, matchedTrades);

        Flux<Quote> quoteStream = consolidatedTapeService.fetchQuoteStream();
        quoteStream.takeUntil(q -> newTrade.getRemainingQuantity() == 0)
                .log()
                .subscribe(qt -> this.executeTrade(qt, newTrade));

        return matchedTrades;
    }

    private void executeTrade(Quote quote, Trade trade){
        if(quote.getTicker().equals(trade.getTicker())){
            //take 10% until its less than 5%
            int tradeQuantityRemaining = trade.getRemainingQuantity();
            int removeQuantity = (int) trade.getQuantity()/10;
            if(tradeQuantityRemaining > removeQuantity)
            {
                tradeQuantityRemaining = tradeQuantityRemaining - removeQuantity;
                trade.setRemainingQuantity(tradeQuantityRemaining);
                if(trade.getOrderStatus() != OrderStatus.EXECUTING)
                    trade.setOrderStatus(OrderStatus.EXECUTING);
            } else {
                trade.setRemainingQuantity(0);
                trade.setOrderStatus(OrderStatus.COMPLETE);
            }
            this.tradeRepository.save(trade);
        }
    }

}
