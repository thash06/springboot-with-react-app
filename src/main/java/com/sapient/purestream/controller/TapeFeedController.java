package com.sapient.purestream.controller;

import com.sapient.purestream.model.ConsolidatedTape;
import com.sapient.purestream.service.TapeFeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tape")
public class TapeFeedController {
    private static final Logger LOG = LoggerFactory
            .getLogger(TapeFeedController.class);

    private final TapeFeedService tapeFeedService;

    public TapeFeedController(TapeFeedService tapeFeedService) {
        this.tapeFeedService = tapeFeedService;
    }

    @GetMapping("/getcurrenttapes")
    public List<ConsolidatedTape> getAllCurrentTapes() {
        return tapeFeedService.getCurrentTapes();
    }

}
