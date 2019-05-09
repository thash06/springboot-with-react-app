package com.sapient.purestream;

import com.sapient.purestream.model.ConsolidatedTape;
import com.sapient.purestream.service.ConsolidatedTapeService;
import com.sapient.purestream.service.TapeFeedService;
import io.reactivex.observables.ConnectableObservable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

@Component
@Profile("!test")
public class StartTradeStreamServices implements CommandLineRunner {
    private static final Logger LOG = LoggerFactory
            .getLogger(StartTradeStreamServices.class);

    private final ConsolidatedTapeService consolidatedTapeService;
    private final TapeFeedService tapeFeedService;
    private final RestClient restClient;

    public StartTradeStreamServices(ConsolidatedTapeService consolidatedTapeService
            , TapeFeedService tapeFeedService
            , RestClient restClient) {
        this.consolidatedTapeService = consolidatedTapeService;
        this.tapeFeedService = tapeFeedService;
        this.restClient = restClient;
    }

    @Override
    public void run(String... args) {

        ConnectableObservable<ConsolidatedTape> ctape = consolidatedTapeService.getConsolidatedTape();
        ctape.take(5).subscribe(t -> LOG.info(t.toString()));
        ctape.connect();

        Thread tapeFeedThread = new Thread(tapeFeedService);
        tapeFeedThread.start();

        class ESTimerTask extends TimerTask {
            public void run() {
                // run execution service
                restClient.sendExecutionMsg();
            }
        }

        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new ESTimerTask(), 0, 6000);

        final CountDownLatch clatch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            clatch.countDown();
            tapeFeedService.getLatch().countDown();
            timer.cancel();
        }));

        try {
            clatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
