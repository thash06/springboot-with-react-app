package com.sapient.purestream.reactive;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

//import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author tarhashm
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConsolidatedTapeServiceAppTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void fetchQuotes() {
//        List<Quote> result =
//                webTestClient
//                        // We then create a GET request to test an endpoint
//                        .get().uri("/pure-stream/quotes")
//                        .accept(MediaType.APPLICATION_STREAM_JSON)
//                        .exchange()
//                        // and use the dedicated DSL to test assertions against the response
//                        .expectStatus().isOk()
//                        .expectHeader().contentType(MediaType.APPLICATION_STREAM_JSON)
//                        .returnResult(Quote.class)
//                        .getResponseBody()
//                        .take(20)
//                        .collectList()
//                        .block();
//
//        assertThat(result).allSatisfy(quote -> assertThat(quote.getPrice()).isPositive());
    }

}