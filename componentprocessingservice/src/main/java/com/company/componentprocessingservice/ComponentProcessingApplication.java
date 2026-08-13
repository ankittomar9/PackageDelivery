package com.company.componentprocessingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ComponentProcessingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComponentProcessingApplication.class, args);
    }
}