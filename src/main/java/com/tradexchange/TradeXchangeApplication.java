package com.tradexchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TradeXchangeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradeXchangeApplication.class, args);
    }
}
