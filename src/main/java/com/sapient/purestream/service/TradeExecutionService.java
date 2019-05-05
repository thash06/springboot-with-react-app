package com.sapient.purestream.service;

import com.sapient.purestream.constants.OrderStatus;
import com.sapient.purestream.constants.Side;
import com.sapient.purestream.model.ExecutionToken;
import com.sapient.purestream.model.Trade;
import com.sapient.purestream.respository.ExecutionRepository;
import com.sapient.purestream.respository.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TradeExecutionService {
    private static final Logger LOG = LoggerFactory
            .getLogger(TradeExecutionService.class);

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

        LOG.info("Trade exec service starting ...");
    }

    public void tradeExecution() {  //(List<Trade> tt) {

        // retrieve streaming trades and get execution tokens from trades
        Map<String, List<ExecutionToken>> execM2 = retrieveAddToExecutionQ();

        if (execM2 == null || execM2.isEmpty()) {
            LOG.info("Trades exec service: return: no execution");
            return;
        }

        // call exec worker to execute trades
        ExecutionWorker execWorker = new ExecutionWorker(
                execM2, tradeRepository, executionRepository, consolidatedTapeService, sequeneGeneratorService);

        Thread ewThread = new Thread(execWorker);
        ewThread.start();
    }

    private synchronized Map<String, List<ExecutionToken>> retrieveAddToExecutionQ() {  //(List<Trade> tt) {

        Map<String, List<ExecutionToken>> execM2 = new HashMap<>();

        //get all streaming trades
        List<Trade> tt = tradeRepository.findByOrderStatus(OrderStatus.STREAMING.name());

        if (tt == null || tt.isEmpty()) {
            return execM2;
        }
        LOG.info(tt.toString());

        // add to execution map
        Stream<Trade> bstream = tt.stream().filter(t -> t.getSide() == Side.BUY);
        List<Trade> strades = tt.stream().filter(s -> s.getSide() == Side.SELL).collect(Collectors.toList());

        bstream.forEach(t -> {
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

                LOG.info(t.getId() + "," + s.getId() + ":" + etokens.size());
            });
            if (!mt.isPresent()) {
                t.setOrderStatus(OrderStatus.RESTING);
                tradeRepository.save(t);
            }
        });

        strades.stream().filter(s -> s.getOrderStatus() == OrderStatus.STREAMING).forEach(
                s -> {
                    s.setOrderStatus(OrderStatus.RESTING);
                    tradeRepository.save(s);
                });

        return execM2;
    }

}
