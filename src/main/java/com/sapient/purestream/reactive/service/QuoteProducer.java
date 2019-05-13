package com.sapient.purestream.reactive.service;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_STREAM_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

/**
 * @author tarhashm
 */
@Configuration
@EnableAutoConfiguration
public class QuoteProducer {

    @Bean
    public RouterFunction<ServerResponse> route(ConsolidatedTapeServiceHandler consolidatedTapeServiceHandler) {
        return RouterFunctions
                .route(GET("/pure-stream/quotes").and(accept(APPLICATION_STREAM_JSON)),
                        consolidatedTapeServiceHandler::streamQuotes);
    }
}
