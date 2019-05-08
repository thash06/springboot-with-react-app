package com.sapient.purestream.service;

import com.sapient.purestream.model.ConsolidatedTape;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ConsolidatedTapeService {

    private Subject<ConsolidatedTape> subject = null;

    private String[] names = {"IBM", "MSFT", "AAPL"};
    private Map<String, Double> ticks = new HashMap<>();

    private class CTTimerTask extends TimerTask {
        private Random ran = new Random();

        @Override
        public void run() {
            int code = ran.nextInt(3);
            String name = names[code];

            double price = ticks.get(name);

            int guess = ran.nextInt(3);
            double newPrice = price;
            if (guess == 1) {
                newPrice = price * 0.98;
            } else if (guess == 2) {
                newPrice = price * 1.02;
            }

            newPrice = Math.round(newPrice * 100) / 100.0;

            int quantity = (ran.nextInt(1001) / 100) * 100;
            if (quantity < 100)
                quantity = 100;

            ticks.put(name, newPrice);

            ConsolidatedTape consolidTape = new ConsolidatedTape();
            consolidTape.setTicker(name);
            consolidTape.setQuantity(quantity);
            consolidTape.setPrice(newPrice);
            consolidTape.setTimestamp(LocalDateTime.now());

            subject.onNext(consolidTape);
        }

    }

    public ConsolidatedTapeService() {

        ticks.put("IBM", 140.27);
        ticks.put("MSFT", 130.60);
        ticks.put("AAPL", 203.73);

        subject = PublishSubject.<ConsolidatedTape>create().toSerialized();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new CTTimerTask(), 0, 5000);
    }

    public ConnectableObservable<ConsolidatedTape> getConsolidatedTape() {
        return subject.publish();
    }

}
