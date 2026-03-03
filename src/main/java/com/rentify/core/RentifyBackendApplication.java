package com.rentify.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RentifyBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(RentifyBackendApplication.class, args);
    }

}
