package com.mercury.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/healthCheck")
public class HealthController {
    @GetMapping
    public String healthCheck(){
        return "UP";
    }

    @GetMapping("/timeout")
    public String getHealthWithTimeout() throws InterruptedException {
        Thread.sleep(8000);
        return "Failed";
    }
}
