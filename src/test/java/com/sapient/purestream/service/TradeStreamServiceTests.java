package com.sapient.purestream.service;

import com.sapient.purestream.constants.OrderStatus;
import com.sapient.purestream.constants.Side;
import com.sapient.purestream.constants.TradeStrategy;
import com.sapient.purestream.model.ExecutionToken;
import com.sapient.purestream.model.Trade;
import com.sapient.purestream.respository.TradeRepository;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static junit.framework.TestCase.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class TradeStreamServiceTests {
    @Autowired
    private TradeRepository tradeRepository;
    @Autowired
    private TradeExecutionService tradeExecutionService;

    @After
    public void finalize() {
        //cleanup after each test
        tradeRepository.deleteAll();
    }

    @Test
    public void tradeMatchByType() {
        Trade t1 = new Trade();
        t1.setId(15L);
        t1.setTicker("IBM");
        t1.setSide(Side.SELL);
        t1.setQuantity(250);
        t1.setRemainingQuantity(100);
        t1.setOrderType(TradeStrategy.FIVE_FIFTEEN_PCT_POV.getValue());
        t1.setOrderStatus(OrderStatus.STREAMING);
        t1.setOrderCreated(new Date());
        Trade t2 = new Trade();
        t2.setId(16L);
        t2.setTicker("IBM");
        t2.setSide(Side.BUY);
        t2.setQuantity(100);
        t2.setRemainingQuantity(100);
        t2.setOrderType(TradeStrategy.FIVE_FIFTEEN_PCT_POV.getValue());
        t2.setOrderStatus(OrderStatus.STREAMING);
        t2.setOrderCreated(new Date());
        Trade t3 = new Trade();
        t3.setId(17L);
        t3.setTicker("AAPL");
        t3.setSide(Side.SELL);
        t3.setQuantity(100);
        t3.setRemainingQuantity(100);
        t3.setOrderType(TradeStrategy.TEN_THIRTY_PCT_POV.getValue());
        t3.setOrderStatus(OrderStatus.STREAMING);
        t3.setOrderCreated(new Date());
        Trade t4 = new Trade();
        t4.setId(18L);
        t4.setTicker("AAPL");
        t4.setSide(Side.BUY);
        t4.setQuantity(250);
        t4.setRemainingQuantity(250);
        t4.setOrderType(TradeStrategy.TEN_THIRTY_PCT_POV.getValue());
        t4.setOrderStatus(OrderStatus.STREAMING);
        t4.setOrderCreated(new Date());

        tradeRepository.save(t1);
        tradeRepository.save(t2);
        tradeRepository.save(t3);
        tradeRepository.save(t4);

        List<Trade> trades = tradeRepository.findByOrderStatus(OrderStatus.STREAMING.name());
        assertTrue(trades.size() == 4);

        Map<String, Queue<ExecutionToken>> execMap = tradeExecutionService.retrieveAddToExecutionQ();
        assertTrue(execMap.size() == 2);

        ExecutionToken etkn1 = execMap.get("IBM").peek();
        ExecutionToken etkn2 = execMap.get("AAPL").peek();

        assertTrue(etkn1.getTicker().equals("IBM"));
        assertTrue(etkn1.getSellTrade().getId() == 15);
        assertTrue(etkn1.getBuyTrade().getId() == 16);

        assertTrue(OrderStatus.EXECUTING == etkn1.getSellTrade().getOrderStatus());
        assertTrue(OrderStatus.EXECUTING == etkn1.getBuyTrade().getOrderStatus());

        assertTrue(etkn2.getTicker().equals("AAPL"));
        assertTrue(etkn2.getSellTrade().getId() == 17);
        assertTrue(etkn2.getBuyTrade().getId() == 18);

        assertTrue(OrderStatus.EXECUTING == etkn2.getSellTrade().getOrderStatus());
        assertTrue(OrderStatus.EXECUTING == etkn2.getBuyTrade().getOrderStatus());
    }

    @Test
    public void tradeMatchByPriority() {

        Trade t1 = new Trade();
        t1.setId(19L);
        t1.setTicker("MSFT");
        t1.setSide(Side.SELL);
        t1.setQuantity(250);
        t1.setRemainingQuantity(200);
        t1.setOrderType(TradeStrategy.FIVE_FIFTEEN_PCT_POV.getValue());
        t1.setOrderStatus(OrderStatus.STREAMING);
        t1.setOrderCreated(new Date());
        Trade t2 = new Trade();
        t2.setId(20L);
        t2.setTicker("MSFT");
        t2.setSide(Side.BUY);
        t2.setQuantity(200);
        t2.setRemainingQuantity(50);
        t2.setOrderType(TradeStrategy.FIVE_FIFTEEN_PCT_POV.getValue());
        t2.setOrderStatus(OrderStatus.STREAMING);
        t2.setOrderCreated(new Date());
        Trade t3 = new Trade();
        t3.setId(21L);
        t3.setTicker("MSFT");
        t3.setSide(Side.BUY);
        t3.setQuantity(100);
        t3.setRemainingQuantity(100);
        t3.setOrderType(TradeStrategy.FIVE_FIFTEEN_PCT_POV.getValue());
        t3.setOrderStatus(OrderStatus.STREAMING);
        t3.setOrderCreated(new Date());

        tradeRepository.save(t1);
        tradeRepository.save(t2);
        tradeRepository.save(t3);

        Map<String, Queue<ExecutionToken>> execMap = tradeExecutionService.retrieveAddToExecutionQ();
        assertTrue(execMap.size() == 1);

        ExecutionToken etkn1 = execMap.get("MSFT").peek();

        assertTrue(etkn1.getTicker().equals("MSFT"));
        assertTrue(etkn1.getSellTrade().getId() == 19);
        assertTrue(etkn1.getBuyTrade().getId() == 20);

        assertTrue(OrderStatus.EXECUTING == etkn1.getSellTrade().getOrderStatus());
        assertTrue(OrderStatus.EXECUTING == etkn1.getBuyTrade().getOrderStatus());

        Optional<Trade> t3_2 = tradeRepository.findById(21L);
        assertTrue(t3_2.isPresent());
        assertTrue(OrderStatus.RESTING == t3_2.get().getOrderStatus());

    }

}
