package com.sapient.purestream.reactive.service;

import com.sapient.purestream.reactive.entity.Quote;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author tarhashm
 */

@Component
public class ConsolidatedTapeService {
    private final MathContext mathContext = new MathContext(2);

    private final Random random = new Random();

    private final List<Quote> prices = new ArrayList<>();

    private final Flux<Quote> quoteStream;

    /**
     * Bootstraps the generator with tickers and initial prices
     */
    public ConsolidatedTapeService() {
        initializeQuotes();
        this.quoteStream = getQuoteStream();
    }

    public Flux<Quote> fetchQuoteStream() {
        return quoteStream;
    }

    private void initializeQuotes() {
        this.prices.add(new Quote("GOOG", 847.24));
        this.prices.add(new Quote("MSFT", 65.11));
    }


    private Flux<Quote> getQuoteStream() {
        return Flux.interval(Duration.ofMillis(200))
                .onBackpressureDrop()
                .map(this::generateQuotes)
                .flatMapIterable(quotes -> quotes)
                .share();
    }

    private List<Quote> generateQuotes(long i) {
        Instant instant = Instant.now();
        return prices.stream()
                .map(baseQuote -> {
                    BigDecimal priceChange = baseQuote.getPrice()
                            .multiply(new BigDecimal(0.05 * this.random.nextDouble()), this.mathContext);

                    Quote result = new Quote(baseQuote.getTicker(), baseQuote.getPrice().add(priceChange));
                    result.setInstant(instant);
                    return result;
                })
                .collect(Collectors.toList());
    }
}
