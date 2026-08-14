package com.company.authorizationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient // <--- Explicit Eureka Discovery Client!
public class JwtAuthenticationApplication {

    public static void main(String[] args) {
        SpringApplication.run(JwtAuthenticationApplication.class, args);
    }

}
