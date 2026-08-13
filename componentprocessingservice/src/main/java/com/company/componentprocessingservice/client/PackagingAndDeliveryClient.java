package com.company.componentprocessingservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "packagingClient", url = "${packaging.service.url:http://localhost:8082}")
public interface PackagingAndDeliveryClient {

    @GetMapping("/PackagingAndDeliveryCharge/{componentType}/{count}")
    double getPackagingAndDeliveryCharge(
            @PathVariable("componentType") String componentType,
            @PathVariable("count") int count
    );
}