package com.sapient.purestream.reactive.service;

import com.sapient.purestream.reactive.model.Quote;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_STREAM_JSON;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

/**
 * @author tarhashm
 */
@Component
public class ConsolidatedTapeServiceHandler {

    private final ConsolidatedTapeService consolidatedTapeService;

    public ConsolidatedTapeServiceHandler(ConsolidatedTapeService consolidatedTapeService) {
        this.consolidatedTapeService = consolidatedTapeService;
    }

    public Mono<ServerResponse> streamQuotes(ServerRequest request) {
        return ok()
                .contentType(APPLICATION_STREAM_JSON)
                .body(this.consolidatedTapeService.fetchQuoteStream(), Quote.class);
    }
}
