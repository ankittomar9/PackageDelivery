package com.company.componentprocessingservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "paymentClient", url = "${payment.service.url:http://localhost:8083}")
public interface PaymentClient {

    @GetMapping("/card/{cardNumber}/{charge}")
    double getCurrentBalance(
            @PathVariable("cardNumber") Long cardNumber,
            @PathVariable("charge") Double charge
    );
}