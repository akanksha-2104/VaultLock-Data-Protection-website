package com.dataprotection.dataprotection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.dataprotection.dataprotection")
public class DataprotectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataprotectionApplication.class, args);
    }
}