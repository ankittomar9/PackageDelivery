package com.company.componentprocessingservice.client;

import com.company.componentprocessingservice.dto.AuthResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

// BEFORE: @FeignClient(name = "authClient", url = "http://localhost:8084")
// AFTER:  Eureka Dynamic Lookup!
@FeignClient(name = "jwtauthenticationservice")
public interface AuthClient {

    @GetMapping("/validate")
    AuthResponseDTO validateToken(@RequestHeader("Authorization") String token);
}