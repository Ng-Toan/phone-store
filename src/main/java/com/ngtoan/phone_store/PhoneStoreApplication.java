package com.ngtoan.phone_store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.ngtoan.phone_store")
public class PhoneStoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(PhoneStoreApplication.class, args);
    }
}