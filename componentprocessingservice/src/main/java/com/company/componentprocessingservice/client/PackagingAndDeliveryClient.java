package com.company.componentprocessingservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// BEFORE: @FeignClient(name = "packagingClient", url = "http://localhost:8082")
// AFTER:  Eureka Dynamic Lookup!
@FeignClient(name = "packaginganddeliveryservice")
public interface PackagingAndDeliveryClient {

    @GetMapping("/PackagingAndDeliveryCharge/{componentType}/{count}")
    double getPackagingAndDeliveryCharge(
            @PathVariable("componentType") String componentType,
            @PathVariable("count") int count
    );
}