package com.sapient.purestream.service;

import com.sapient.purestream.model.Trade;
import com.sapient.purestream.respository.TradeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class TradeService {

    private final TradeRepository tradeRepository;

    public TradeService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    public Trade createTrade(Trade trade) {
        return this.tradeRepository.save(trade);
    }

    public List<Trade> displayTrades() {
        return this.tradeRepository.findAll();
    }

    public Optional<Trade> findById(Long id) {
        return this.tradeRepository.findById(id);
    }

    public void deleteById(Long id) { this.tradeRepository.deleteById(id); }

    public List<Trade> findByOrderStatus(String type) {
        return this.tradeRepository.findByOrderStatus(type);
    }

    public List<Trade> findByOrderTypeSideTicker(String orderType, String side, String ticker) {
        return this.tradeRepository.findByOrdertypeSideAndTicker(orderType, side, ticker);
    }

}
