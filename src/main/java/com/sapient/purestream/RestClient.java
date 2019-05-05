package com.sapient.purestream;

import com.sapient.purestream.respository.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestClient {
    private static final Logger LOG = LoggerFactory
            .getLogger(RestClient.class);

    private final String url;
    private final TradeRepository tradeRepository;

    @Autowired
    public RestClient(@Value("${rest.executionservice.url}") String url
            , TradeRepository tradeRepository) {
        this.url = url;
        this.tradeRepository = tradeRepository;
    }

    public ResponseEntity<String> sendExecutionMsg() {    //List<Trade> trades) {

        // send for execution
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);

        ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);

        LOG.info("RestClient: " + response.getStatusCode());

        return response;
    }

}
