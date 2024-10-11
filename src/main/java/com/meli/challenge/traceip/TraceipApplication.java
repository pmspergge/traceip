package com.meli.challenge.traceip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TraceipApplication {

    public static void main(String[] args) {
        SpringApplication.run(TraceipApplication.class, args);
    }

}
