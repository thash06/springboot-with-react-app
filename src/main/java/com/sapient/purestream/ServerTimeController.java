package com.sapient.purestream;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class ServerTimeController {
    @GetMapping("/api/server/time")
    public String serverTime() {
        return "Server time: " + new Date() + "\n";
    }


}
