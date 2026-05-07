package com.ngtoan.phone_store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.ngtoan.phone_store")
@EnableScheduling
public class PhoneStoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(PhoneStoreApplication.class, args);
    }
}