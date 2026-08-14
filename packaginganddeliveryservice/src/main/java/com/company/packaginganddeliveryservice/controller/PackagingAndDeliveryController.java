package com.company.packaginganddeliveryservice.controller;

import com.company.packaginganddeliveryservice.service.PackagingAndDeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Packaging & Delivery Controller", description = "Endpoints for calculating component packaging and delivery charges")
@CrossOrigin(origins = "*")
public class PackagingAndDeliveryController {

    private final PackagingAndDeliveryService packagingAndDeliveryService;

    @Operation(summary = "Calculate Packaging and Delivery Cost", description = "Calculates total packaging, protective packing, and delivery tariff for a component type and defective count")
    @GetMapping("/PackagingAndDeliveryCharge/{componentType}/{count}")
    public ResponseEntity<Double> getPackagingAndDeliveryCharge(
            @PathVariable("componentType") String componentType,
            @PathVariable("count") int count
    ) {
        log.info("Received packaging charge calculation request for type: '{}', count: {}", componentType, count);

        double totalCharge = packagingAndDeliveryService.getPackingAndDeliveryCharge(componentType, count);

        log.info("Calculated total packaging & delivery charge: {}", totalCharge);
        return ResponseEntity.ok(totalCharge);
    }
}