package com.sapient.purestream.reactive.service;

import com.sapient.purestream.model.Trade;
import com.sapient.purestream.reactive.repository.TradeRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
public class TradeService {

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

}
