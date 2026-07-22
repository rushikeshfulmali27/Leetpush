package com.leethubai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LeetHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeetHubApplication.class, args);
    }
}
