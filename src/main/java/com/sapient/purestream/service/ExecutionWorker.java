package com.sapient.purestream.service;

import com.sapient.purestream.constants.ExecutionStatus;
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
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class ExecutionWorker implements Runnable {
    private static final Logger LOG = LoggerFactory
            .getLogger(ExecutionWorker.class);

    private final TradeRepository tradeRepository;
    private final ExecutionRepository executionRepository;
    private final ConsolidatedTapeRepository consolidatedTapeRepository;
    private final ConsolidatedTapeService consolidatedTapeService;
    private final SequeneGeneratorService sequeneGeneratorService;
    private final Map<String, Queue<ExecutionToken>> execMap;

    private Disposable disposable = null;
    private int period = 0;
    CountDownLatch clatch = new CountDownLatch(1);

    public ExecutionWorker(Map<String, Queue<ExecutionToken>> execMap
            , TradeRepository tradeRepository
            , ExecutionRepository executionRepository
            , ConsolidatedTapeRepository consolidatedTapeRepository
            , ConsolidatedTapeService consolidatedTapeService
            , SequeneGeneratorService sequeneGeneratorService) {
        this.execMap = execMap;
        this.tradeRepository = tradeRepository;
        this.executionRepository = executionRepository;
        this.consolidatedTapeRepository = consolidatedTapeRepository;
        this.consolidatedTapeService = consolidatedTapeService;
        this.sequeneGeneratorService = sequeneGeneratorService;
    }

    class EWTimerTask extends TimerTask {
        public void run() {

            if (execMap.isEmpty()) {
                disposable.dispose();
                clatch.countDown();
            }

        }
    }

    public void run() {
        LOG.info("Trade exec worker starting ...");

        ConnectableObservable<ConsolidatedTape> ctapestream =
                consolidatedTapeService.getConsolidatedTape();
        disposable = ctapestream.connect();

        execMap.keySet().stream().map(k -> execMap.get(k).size()).forEach(n -> period += n);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new EWTimerTask(), 0, period * 5000);

        ctapestream.map(ct -> {
            Queue<ExecutionToken> tokens = execMap.get(ct.getTicker());
            if (tokens != null && !tokens.isEmpty()) {
                List<ExecutionToken> elist = tokens.stream().filter(tk -> tk.isInExec() == false).collect(Collectors.toList());
                elist.stream().forEach(tk -> {
                    tk.setInExec(true);
                    tk.setCtape(ct);

                    Observable.just(tk)
                            .map(tkn -> {
                                processExecution(tkn);
                                return tkn;
                            }).subscribe();
                });
            } else {
                execMap.remove(ct.getTicker());
            }

            return ct;
        }).subscribe();

        try {
            clatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        timer.cancel();

        LOG.info("Trade exec worker exiting ...");
    }

    private boolean processExecution(ExecutionToken execToken) {

        boolean result = true;
        double percent = execToken.getBuyTrade().getOrderType()
                .equals(TradeStrategy.FIVE_FIFTEEN_PCT_POV.getValue())
                ? 0.15 : 0.3;    // for strategy "5-15%" or "10-30%" POV strategy
        int estQuantity = (int) Math.round(execToken.getCtape().getQuantity() * percent);

        int execQuantity = estQuantity;
        double price = execToken.getCtape().getPrice();

        Trade buyTrade = execToken.getBuyTrade();
        Trade sellTrade = execToken.getSellTrade();
        int buyTradeQuantity = buyTrade.getRemainingQuantity();
        int sellTradeQuantity = sellTrade.getRemainingQuantity();

        if (buyTradeQuantity < estQuantity || sellTradeQuantity < estQuantity) {
            execQuantity = buyTradeQuantity < sellTradeQuantity ? buyTradeQuantity : sellTradeQuantity;
        }

        Execution exec = new Execution();
        exec.setId(sequeneGeneratorService.generateSequence("Executions"));
        exec.setTicker(buyTrade.getTicker());
        exec.setQuantity(execQuantity);
        exec.setPrice(price);
        exec.setBuyTradeId(buyTrade.getId());
        exec.setSellTradeId(sellTrade.getId());
        exec.setExecStatus(ExecutionStatus.SUCCESS);
        exec.setCreated(LocalDateTime.now());

        ConsolidatedTape ctape = execToken.getCtape();
        long ctapeId = sequeneGeneratorService.generateSequence("ConsolidatedTapes");
        ctape.setId(ctapeId);

        exec.setConsolidTapeId(ctapeId);

        if (buyTrade.getSide() != Side.BUY || sellTrade.getSide() != Side.SELL
                || !buyTrade.getTicker().equals(sellTrade.getTicker())
                || !buyTrade.getOrderType().equals(sellTrade.getOrderType())) {

            exec.setExecStatus(ExecutionStatus.CANCEL);
            exec.setCancelReason("buy and sell trades do not match (" + buyTrade.getId() + "," + sellTrade.getId() + ")");
            buyTrade.setOrderStatus(OrderStatus.RESTING);
            sellTrade.setOrderStatus(OrderStatus.RESTING);

            result = false;
        }

        boolean complete = false;
        if (result) {
            if (buyTradeQuantity == execQuantity) {
                buyTrade.setRemainingQuantity(0);
                buyTrade.setOrderStatus(OrderStatus.COMPLETE);
                complete = true;
            }

            if (sellTradeQuantity == execQuantity) {
                sellTrade.setRemainingQuantity(0);
                sellTrade.setOrderStatus(OrderStatus.COMPLETE);
                complete = true;
            }

            if (complete && buyTradeQuantity == execQuantity && sellTradeQuantity != execQuantity) {
                sellTrade.setRemainingQuantity(sellTradeQuantity - execQuantity);
                sellTrade.setOrderStatus(OrderStatus.RESTING);
            }

            if (complete && buyTradeQuantity != execQuantity && sellTradeQuantity == execQuantity) {
                buyTrade.setRemainingQuantity(buyTradeQuantity - execQuantity);
                buyTrade.setOrderStatus(OrderStatus.RESTING);
            }

            if (!complete) {
                buyTrade.setRemainingQuantity(buyTradeQuantity - execQuantity);
                sellTrade.setRemainingQuantity(sellTradeQuantity - execQuantity);
            }
        }
        executionRepository.save(exec);
        tradeRepository.save(buyTrade);
        tradeRepository.save(sellTrade);
        consolidatedTapeRepository.save(ctape);

        if (!complete && result) {
            execToken.setInExec(false);
        } else {
            Queue<ExecutionToken> q = execMap.get(execToken.getTicker());
            q.remove(execToken);
        }

        return result;
    }
}
