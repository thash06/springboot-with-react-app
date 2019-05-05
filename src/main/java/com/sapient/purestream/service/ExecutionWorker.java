package com.sapient.purestream.service;

import com.sapient.purestream.constants.ExecutionStatus;
import com.sapient.purestream.constants.OrderStatus;
import com.sapient.purestream.constants.Side;
import com.sapient.purestream.model.ConsolidatedTape;
import com.sapient.purestream.model.Execution;
import com.sapient.purestream.model.ExecutionToken;
import com.sapient.purestream.model.Trade;
import com.sapient.purestream.respository.ExecutionRepository;
import com.sapient.purestream.respository.TradeRepository;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

public class ExecutionWorker implements Runnable {

    private final TradeRepository tradeRepository;
    private final ExecutionRepository executionRepository;
    private final ConsolidatedTapeService consolidatedTapeService;
    private final SequeneGeneratorService sequeneGeneratorService;
    private final Map<String, List<ExecutionToken>> execMap;

    private Disposable disposable = null;
    private int period = 0;
    private int counter = 0;
    CountDownLatch clatch = new CountDownLatch(1);

    public ExecutionWorker(Map<String, List<ExecutionToken>> execMap
            , TradeRepository tradeRepository
            , ExecutionRepository executionRepository
            , ConsolidatedTapeService consolidatedTapeService
            , SequeneGeneratorService sequeneGeneratorService) {
        this.execMap = execMap;
        this.tradeRepository = tradeRepository;
        this.executionRepository = executionRepository;
        this.consolidatedTapeService = consolidatedTapeService;
        this.sequeneGeneratorService = sequeneGeneratorService;

        System.out.println("Trade exec worker starting ...");
    }

    class EWTimerTask extends TimerTask {
        public void run() {

            //  System.out.println("TE timer task runs ...");

            //  System.out.println("execMap size: " + execMap.size());

            if (execMap.isEmpty()) {
                disposable.dispose();
                cleanMap();
                clatch.countDown();
            } else {
                counter++;
                if (counter == 10) {
                    disposable.dispose();
                    cleanMap();
                    clatch.countDown();
                }
            }

        }
    }

    private void cleanMap() {
        execMap.keySet().stream().forEach(k -> execMap.get(k).stream()
                .forEach(e -> {
                    Trade bt = e.getBuyTrade();
                    Trade st = e.getSellTrade();
                    bt.setOrderStatus(OrderStatus.RESTING);
                    st.setOrderStatus(OrderStatus.RESTING);
                    tradeRepository.save(bt);
                    tradeRepository.save(st);
                }));
    }

    public void run() {

        ConnectableObservable<ConsolidatedTape> ctapestream =
                consolidatedTapeService.getConsolidatedTape();
        disposable = ctapestream.connect();

        execMap.keySet().stream().map(k -> execMap.get(k).size()).forEach(n -> period += n);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new EWTimerTask(), 0, period * 5000);

        // System.out.println("TExec worker runs ...");

        ctapestream.map(ct -> {
            List<ExecutionToken> tokens = execMap.get(ct.getTicker());
            if (tokens != null) {
                ExecutionToken et = tokens.remove(0);
                if (tokens.isEmpty())
                    execMap.remove(ct.getTicker());

                et.setCtape(ct);
                Observable.just(et)
                        //.observeOn(Schedulers.io())
                        .map(t -> {
                            processExecution(et);
                            return t;
                        })
                        .subscribe();
            }

            return ct;
        }).subscribe();

        //System.out.println("TExec worker waits ...");
        try {
            clatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        timer.cancel();
        // System.out.println("TExec worker done ...");
    }

    private boolean processExecution(ExecutionToken execToken) {

        boolean result = true;
        int estQuantity = (int) Math.round(execToken.getCtape().getQuantity() * 0.15);

        int execQuantity = estQuantity;
        double price = execToken.getCtape().getPrice();

        Trade buyTrade = execToken.getBuyTrade();
        Trade sellTrade = execToken.getSellTrade();
        int buyTradeQuantity = buyTrade.getRemainingQuantity();
        if (buyTradeQuantity == -1)
            buyTradeQuantity = buyTrade.getQuantity();
        int sellTradeQuantity = sellTrade.getRemainingQuantity();
        if (sellTradeQuantity == -1)
            sellTradeQuantity = sellTrade.getQuantity();

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

        if (buyTrade.getSide() != Side.BUY || sellTrade.getSide() != Side.SELL
                || !buyTrade.getTicker().equals(sellTrade.getTicker())
                || !buyTrade.getOrderType().equals(sellTrade.getOrderType())) {

            exec.setExecStatus(ExecutionStatus.ERROR);
            buyTrade.setOrderStatus(OrderStatus.RESTING);
            sellTrade.setOrderStatus(OrderStatus.RESTING);

            result = false;
        }

        if (buyTradeQuantity == execQuantity) {
            buyTrade.setRemainingQuantity(0);
            buyTrade.setOrderStatus(OrderStatus.COMPLETE);
        } else {
            buyTrade.setRemainingQuantity(buyTradeQuantity - execQuantity);
            buyTrade.setOrderStatus(OrderStatus.RESTING);
        }

        if (sellTradeQuantity == execQuantity) {
            sellTrade.setRemainingQuantity(0);
            sellTrade.setOrderStatus(OrderStatus.COMPLETE);
        } else {
            sellTrade.setRemainingQuantity(sellTradeQuantity - execQuantity);
            sellTrade.setOrderStatus(OrderStatus.RESTING);
        }

        executionRepository.save(exec);
        tradeRepository.save(buyTrade);
        tradeRepository.save(sellTrade);

        return result;
    }
}
