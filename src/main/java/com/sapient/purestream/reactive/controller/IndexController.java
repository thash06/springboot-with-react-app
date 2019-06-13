package com.sapient.purestream.reactive.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author tarhashm
 */
@RequestMapping("/error")
@RestController
public class IndexController implements ErrorController {

    private static final String PATH = "/error";

    @GetMapping(value = PATH)
    public String error() {
        return "Error handling";
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }
}
