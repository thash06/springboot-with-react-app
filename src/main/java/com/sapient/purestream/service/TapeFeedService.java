package com.sapient.purestream.service;

import com.sapient.purestream.model.ConsolidatedTape;
import io.reactivex.observables.ConnectableObservable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CountDownLatch;

@Service
public class TapeFeedService implements Runnable {
    private static final Logger LOG = LoggerFactory
            .getLogger(TapeFeedService.class);

    private final ConsolidatedTapeService consolidatedTapeService;
    private final CountDownLatch clatch = new CountDownLatch(1);

    private Queue<ConsolidatedTape> ctapeList = new LinkedList<>();

    public TapeFeedService(ConsolidatedTapeService consolidatedTapeService) {
        this.consolidatedTapeService = consolidatedTapeService;
    }

    public void run() {
        ConnectableObservable<ConsolidatedTape> ctape = consolidatedTapeService.getConsolidatedTape();
        ctape.subscribe(ct -> {
            if (ctapeList.size() < 10)
                ctapeList.add(ct);
            else {
                ctapeList.poll();
                ctapeList.add(ct);
            }
        });

        ctape.connect();

        try {
            clatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public CountDownLatch getLatch() {
        return clatch;
    }

    public List<ConsolidatedTape> getCurrentTapes() {
        List<ConsolidatedTape> ctList = new ArrayList<>();
        ctList.addAll(ctapeList);
        Collections.reverse(ctList);

        return ctList;
    }

}
