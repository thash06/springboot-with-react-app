package com.sapient.purestream.service;

import com.sapient.purestream.constants.OrderStatus;
import com.sapient.purestream.constants.Side;
import com.sapient.purestream.model.ExecutionToken;
import com.sapient.purestream.model.Trade;
import com.sapient.purestream.respository.ExecutionRepository;
import com.sapient.purestream.respository.TradeRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TradeExecutionService {

    private final TradeRepository tradeRepository;
    private final ExecutionRepository executionRepository;
    private final ConsolidatedTapeService consolidatedTapeService;
    private final SequeneGeneratorService sequeneGeneratorService;

    public TradeExecutionService(TradeRepository tradeRepository
            , ExecutionRepository executionRepository
            , ConsolidatedTapeService consolidatedTapeService
            , SequeneGeneratorService sequeneGeneratorService) {
        this.tradeRepository = tradeRepository;
        this.executionRepository = executionRepository;
        this.consolidatedTapeService = consolidatedTapeService;
        this.sequeneGeneratorService = sequeneGeneratorService;

        System.out.println("Trade exec service starting ...");
    }

    public void tradeExecution() {  //(List<Trade> tt) {

        //System.out.println("Trade exec service runs...");

        // retrieve streaming trades and get execution tokens from trades
        Map<String, List<ExecutionToken>> execM2 = retrieveAddToExecutionQ();

        if (execM2 == null || execM2.isEmpty()) {
            System.out.println("Trades exec service: return: no execution");
            return;
        }

        //System.out.println("Trades added to map");

        // call exec worker to execute trades
        ExecutionWorker execWorker = new ExecutionWorker(
                execM2, tradeRepository, executionRepository, consolidatedTapeService, sequeneGeneratorService);

        Thread ewThread = new Thread(execWorker);
        ewThread.start();

        //System.out.println("Trade exec service returns...");
    }

    private synchronized Map<String, List<ExecutionToken>> retrieveAddToExecutionQ() {  //(List<Trade> tt) {

        Map<String, List<ExecutionToken>> execM2 = new HashMap<>();

        //get all streaming trades
        List<Trade> tt = tradeRepository.findByOrderStatus(OrderStatus.STREAMING.name());

        if (tt == null || tt.isEmpty()) {
            //System.out.println("Trades exec service: null or empty trades");
            return execM2;
        }
        System.out.println(tt.toString());

        // add to execution map

        Stream<Trade> bstream = tt.stream().filter(t -> t.getSide() == Side.BUY);
        List<Trade> strades = tt.stream().filter(s -> s.getSide() == Side.SELL).collect(Collectors.toList());

        bstream.forEach(t -> {
            System.out.println(t.getTicker() + t.getId());
            Optional<Trade> mt = strades.stream().filter(s -> s.getOrderStatus() == OrderStatus.STREAMING
                    && s.getTicker().equals(t.getTicker()) && s.getOrderType().equals(t.getOrderType()))
                    .findFirst();
            mt.ifPresent(s -> {
                s.setOrderStatus(OrderStatus.EXECUTING);
                t.setOrderStatus(OrderStatus.EXECUTING);
                tradeRepository.save(s);
                tradeRepository.save(t);

                List<ExecutionToken> etokens = execM2.get(t.getTicker());
                if (etokens == null) {
                    etokens = new ArrayList<>();
                    execM2.put(t.getTicker(), etokens);
                }
                etokens.add(new ExecutionToken(t.getTicker(), t, s));

                System.out.println(t.getId() + "," + s.getId() + ":" + etokens.size());
            });
            if (!mt.isPresent()) {
                t.setOrderStatus(OrderStatus.RESTING);
                tradeRepository.save(t);
            }
        });

        return execM2;
    }

}
