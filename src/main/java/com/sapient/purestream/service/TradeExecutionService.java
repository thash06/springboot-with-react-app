package com.sapient.purestream.service;

import com.sapient.purestream.constants.OrderStatus;
import com.sapient.purestream.constants.Side;
import com.sapient.purestream.model.ExecutionToken;
import com.sapient.purestream.model.Trade;
import com.sapient.purestream.respository.ConsolidatedTapeRepository;
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
    private final ConsolidatedTapeRepository consolidatedTapeRepository;
    private final ConsolidatedTapeService consolidatedTapeService;
    private final SequeneGeneratorService sequeneGeneratorService;

    public TradeExecutionService(TradeRepository tradeRepository
            , ExecutionRepository executionRepository
            , ConsolidatedTapeRepository consolidatedTapeRepository
            , ConsolidatedTapeService consolidatedTapeService
            , SequeneGeneratorService sequeneGeneratorService) {
        this.tradeRepository = tradeRepository;
        this.executionRepository = executionRepository;
        this.consolidatedTapeRepository = consolidatedTapeRepository;
        this.consolidatedTapeService = consolidatedTapeService;
        this.sequeneGeneratorService = sequeneGeneratorService;

        LOG.info("Trade exec service starting ...");
    }

    public void tradeExecution() {

        // retrieve streaming trades and get execution tokens from trades
        Map<String, Queue<ExecutionToken>> execM2 = retrieveAddToExecutionQ();

        if (execM2 == null || execM2.isEmpty()) {
            LOG.info("Trades exec service: return: no execution");
            return;
        }

        // call exec worker to execute trades
        ExecutionWorker execWorker = new ExecutionWorker(
                execM2, tradeRepository, executionRepository
                , consolidatedTapeRepository, consolidatedTapeService, sequeneGeneratorService);

        Thread ewThread = new Thread(execWorker);
        ewThread.start();
    }

    private synchronized Map<String, Queue<ExecutionToken>> retrieveAddToExecutionQ() {

        Map<String, Queue<ExecutionToken>> execM2 = new HashMap<>();

        //get all streaming trades
        List<Trade> tt = tradeRepository.findByOrderStatus(OrderStatus.STREAMING.name());

        if (tt == null || tt.isEmpty()) {
            return execM2;
        }
        LOG.info(tt.toString());

        Comparator<Trade> tr_comparator = (Trade t1, Trade t2) -> {
            if (t1.getQuantity() == t2.getQuantity())
                return 0;
            else if (t1.getQuantity() < t2.getQuantity())
                return 1;
            else
                return -1;
        };

        // add to execution map
        Stream<Trade> bstream = tt.stream().filter(t -> t.getSide() == Side.BUY).sorted(tr_comparator);
        List<Trade> strades = tt.stream().filter(s -> s.getSide() == Side.SELL).sorted(tr_comparator).collect(Collectors.toList());

        bstream.forEach(t -> {
            Optional<Trade> mt = strades.stream().filter(s -> s.getOrderStatus() == OrderStatus.STREAMING
                    && s.getTicker().equals(t.getTicker()) && s.getOrderType().equals(t.getOrderType()))
                    .findFirst();
            mt.ifPresent(s -> {
                s.setOrderStatus(OrderStatus.EXECUTING);
                t.setOrderStatus(OrderStatus.EXECUTING);
                tradeRepository.save(s);
                tradeRepository.save(t);

                Queue<ExecutionToken> etokens = execM2.get(t.getTicker());
                if (etokens == null) {
                    etokens = new LinkedList<>();       //PriorityQueue<>(et_comparator);
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
