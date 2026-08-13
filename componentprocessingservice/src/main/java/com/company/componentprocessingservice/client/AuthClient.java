package com.company.componentprocessingservice.client;

import com.company.componentprocessingservice.dto.AuthResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "authClient", url = "${auth.service.url:http://localhost:8084}")
public interface AuthClient {

    @GetMapping("/validate")
    AuthResponseDTO validateToken(@RequestHeader("Authorization") String token);
}