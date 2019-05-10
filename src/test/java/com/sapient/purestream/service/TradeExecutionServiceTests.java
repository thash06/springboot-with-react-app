package com.sapient.purestream.service;

import com.sapient.purestream.constants.OrderStatus;
import com.sapient.purestream.constants.Side;
import com.sapient.purestream.constants.TradeStrategy;
import com.sapient.purestream.model.ConsolidatedTape;
import com.sapient.purestream.model.Execution;
import com.sapient.purestream.model.ExecutionToken;
import com.sapient.purestream.model.Trade;
import com.sapient.purestream.respository.ConsolidatedTapeRepository;
import com.sapient.purestream.respository.ExecutionRepository;
import com.sapient.purestream.respository.TradeRepository;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.*;

import static junit.framework.TestCase.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TradeExecutionServiceTests {
    @Autowired
    private TradeRepository tradeRepository;
    @Autowired
    private ExecutionRepository executionRepository;
    @Autowired
    private ConsolidatedTapeRepository consolidatedTapeRepository;
    @Autowired
    private SequeneGeneratorService sequeneGeneratorService;
    @Autowired
    private ConsolidatedTapeService consolidatedTapeService;
    @Autowired
    private TradeExecutionService tradeExecutionService;

    @After
    public void finalize() {
        //cleanup after each test
        tradeRepository.deleteAll();
        executionRepository.deleteAll();
        consolidatedTapeRepository.deleteAll();
    }

    @Test
    public void executionProcessing15() {

        Trade t1 = new Trade();
        t1.setId(15L);
        t1.setTicker("IBM");
        t1.setSide(Side.SELL);
        t1.setQuantity(250);
        t1.setRemainingQuantity(100);
        t1.setOrderType(TradeStrategy.FIVE_FIFTEEN_PCT_POV.getValue());
        t1.setOrderStatus(OrderStatus.EXECUTING);
        t1.setOrderCreated(new Date());
        Trade t2 = new Trade();
        t2.setId(16L);
        t2.setTicker("IBM");
        t2.setSide(Side.BUY);
        t2.setQuantity(100);
        t2.setRemainingQuantity(100);
        t2.setOrderType(TradeStrategy.FIVE_FIFTEEN_PCT_POV.getValue());
        t2.setOrderStatus(OrderStatus.EXECUTING);
        t2.setOrderCreated(new Date());

        tradeRepository.save(t1);
        tradeRepository.save(t2);

        ConsolidatedTape ctape = new ConsolidatedTape();
        ctape.setTicker(t1.getTicker());
        ctape.setQuantity(500);
        ctape.setPrice(135.28);
        ctape.setTimestamp(LocalDateTime.now());

        ExecutionToken etkn = new ExecutionToken(t1.getTicker(), t2, t1);  // ticker, buyTrade, sellTrade

        Map<String, Queue<ExecutionToken>> execMap = new HashMap<>();
        Queue<ExecutionToken> q = new LinkedList<>();
        q.add(etkn);
        execMap.put(t1.getTicker(), q);

        ExecutionWorker eworker = new ExecutionWorker(execMap
                , tradeRepository, executionRepository
                , consolidatedTapeRepository, consolidatedTapeService
                , sequeneGeneratorService);

        etkn.setCtape(ctape);
        etkn.setInExec(true);
        eworker.processExecution(etkn);

        List<Trade> tlist = tradeRepository.findAll();
        assertTrue(tlist.size() == 2);

        Trade t1_2 = null;
        Trade t2_2 = null;
        if (tlist.get(0).getSide() == Side.SELL) {
            t1_2 = tlist.get(0);
            t2_2 = tlist.get(1);
        } else {
            t1_2 = tlist.get(1);
            t2_2 = tlist.get(0);
        }

        List<Execution> el = executionRepository.findAll();
        assertTrue(el.size() == 1);

        Execution exec = el.get(0);

        List<ConsolidatedTape> ctl = consolidatedTapeRepository.findAll();
        assertTrue(el.size() == 1);

        ConsolidatedTape ctape_2 = ctl.get(0);
        assertTrue(ctape_2.getId() == ctape.getId());

        assertTrue(exec.getSellTradeId() == t1_2.getId());
        assertTrue(exec.getBuyTradeId() == t2_2.getId());
        assertTrue(exec.getConsolidTapeId() == ctape_2.getId());

        assertTrue(exec.getQuantity() == 75);
        assertTrue(Math.abs(exec.getPrice() - 135.28) < 0.0001);

        assertTrue(t1_2.getRemainingQuantity() == 25);
        assertTrue(t2_2.getRemainingQuantity() == 25);

        assertTrue(t1_2.getOrderStatus() == OrderStatus.EXECUTING);
        assertTrue(t2_2.getOrderStatus() == OrderStatus.EXECUTING);

        assertTrue(execMap.get(t1.getTicker()).size() == 1);
        assertTrue(etkn.isInExec() == false);

    }

    @Test
    public void executionProcessing30() {

        Trade t3 = new Trade();
        t3.setId(17L);
        t3.setTicker("AAPL");
        t3.setSide(Side.SELL);
        t3.setQuantity(100);
        t3.setRemainingQuantity(100);
        t3.setOrderType(TradeStrategy.TEN_THIRTY_PCT_POV.getValue());
        t3.setOrderStatus(OrderStatus.EXECUTING);
        t3.setOrderCreated(new Date());
        Trade t4 = new Trade();
        t4.setId(18L);
        t4.setTicker("AAPL");
        t4.setSide(Side.BUY);
        t4.setQuantity(250);
        t4.setRemainingQuantity(150);
        t4.setOrderType(TradeStrategy.TEN_THIRTY_PCT_POV.getValue());
        t4.setOrderStatus(OrderStatus.EXECUTING);
        t4.setOrderCreated(new Date());

        tradeRepository.save(t3);
        tradeRepository.save(t4);

        ConsolidatedTape ctape = new ConsolidatedTape();
        ctape.setTicker(t3.getTicker());
        ctape.setQuantity(500);
        ctape.setPrice(197.18);
        ctape.setTimestamp(LocalDateTime.now());

        ExecutionToken etkn = new ExecutionToken(t3.getTicker(), t4, t3);  // ticker, buyTrade, sellTrade

        Map<String, Queue<ExecutionToken>> execMap = new HashMap<>();
        Queue<ExecutionToken> q = new LinkedList<>();
        q.add(etkn);
        execMap.put(t3.getTicker(), q);

        ExecutionWorker eworker = new ExecutionWorker(execMap
                , tradeRepository, executionRepository
                , consolidatedTapeRepository, consolidatedTapeService
                , sequeneGeneratorService);

        etkn.setCtape(ctape);
        etkn.setInExec(true);
        eworker.processExecution(etkn);

        List<Trade> tlist = tradeRepository.findAll();
        assertTrue(tlist.size() == 2);

        Trade t3_2 = null;
        Trade t4_2 = null;
        if (tlist.get(0).getSide() == Side.SELL) {
            t3_2 = tlist.get(0);
            t4_2 = tlist.get(1);
        } else {
            t3_2 = tlist.get(1);
            t4_2 = tlist.get(0);
        }

        List<Execution> el = executionRepository.findAll();
        assertTrue(el.size() == 1);

        Execution exec = el.get(0);

        List<ConsolidatedTape> ctl = consolidatedTapeRepository.findAll();
        assertTrue(el.size() == 1);

        ConsolidatedTape ctape_2 = ctl.get(0);
        assertTrue(ctape_2.getId() == ctape.getId());

        assertTrue(exec.getSellTradeId() == t3_2.getId());
        assertTrue(exec.getBuyTradeId() == t4_2.getId());
        assertTrue(exec.getConsolidTapeId() == ctape_2.getId());

        assertTrue(exec.getQuantity() == 100);
        assertTrue(Math.abs(exec.getPrice() - 197.18) < 0.0001);

        assertTrue(t3_2.getRemainingQuantity() == 0);
        assertTrue(t4_2.getRemainingQuantity() == 50);

        assertTrue(t3_2.getOrderStatus() == OrderStatus.COMPLETE);
        assertTrue(t4_2.getOrderStatus() == OrderStatus.RESTING);

        assertTrue(execMap.get(t3.getTicker()).size() == 0);
    }

    @Test
    public void runTradeExecution() {
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

        tradeExecutionService.tradeExecution();  // whole trade execution process

        Optional<Trade> ot2 = tradeRepository.findById(20L);
        assertTrue(ot2.isPresent());
        while (ot2.get().getRemainingQuantity() != 0) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ot2 = tradeRepository.findById(20L);
        }

        List<Trade> tlist = tradeRepository.findAll();
        assertTrue(tlist.size() == 3);

        Trade t1_2 = tradeRepository.findById(19L).get();
        Trade t2_2 = tradeRepository.findById(20L).get();
        Trade t3_2 = tradeRepository.findById(21L).get();

        List<Execution> elist = executionRepository.findAll();
        assertTrue(!elist.isEmpty());

        List<ConsolidatedTape> ctlist = consolidatedTapeRepository.findAll();
        assertTrue(!ctlist.isEmpty());

        Optional<Integer> execQuantity = elist.stream().map(exec -> {
            exec.getTicker().equals("MSFT");
            assertTrue(exec.getSellTradeId() == 19L);
            assertTrue(exec.getBuyTradeId() == 20L);

            Optional<ConsolidatedTape> ctape = consolidatedTapeRepository.findById(exec.getConsolidTapeId());
            assertTrue(ctape.isPresent());
            assertTrue(Math.abs(exec.getPrice() - ctape.get().getPrice()) < 0.0001);

            return exec.getQuantity();
        }).reduce((x, y) -> (Integer) x + (Integer) y);

        assertTrue(execQuantity.isPresent());
        assertTrue(execQuantity.get() == 50);

        assertTrue(t1_2.getRemainingQuantity() == 150);
        assertTrue(t2_2.getRemainingQuantity() == 0);
        assertTrue(t3_2.getRemainingQuantity() == 100);

        assertTrue(t1_2.getOrderStatus() == OrderStatus.RESTING);
        assertTrue(t2_2.getOrderStatus() == OrderStatus.COMPLETE);
        assertTrue(t3_2.getOrderStatus() == OrderStatus.RESTING);
    }
}
