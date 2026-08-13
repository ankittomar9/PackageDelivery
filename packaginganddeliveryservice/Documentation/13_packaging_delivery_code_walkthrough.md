# Detailed Code Walkthrough — Packaging & Delivery Microservice (`PackagingAndDelivery`)

This document provides a line-by-line breakdown of every core class in **`PackagingAndDelivery`**, highlighting concurrency fixes and Java 21 refactoring.

---

## 1. `PackagingAndDeliveryService.java`

```java
package com.company.packaginganddeliveryservice.service;

import com.company.packaginganddeliveryservice.exception.ComponentTypeNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PackagingAndDeliveryService {

    public static final double PROTECTIVE_SHEATH_CHARGE = 50.0;
    public static final double INTEGRAL_PACKING_CHARGE = 100.0;
    public static final double INTEGRAL_DELIVERY_CHARGE = 200.0;
    public static final double ACCESSORY_PACKING_CHARGE = 50.0;
    public static final double ACCESSORY_DELIVERY_CHARGE = 100.0;

    public double getPackingAndDeliveryCharge(String componentType, int count) {
        log.info("Calculating packaging charges for type: '{}', count: {}", componentType, count);

        if (count <= 0) {
            throw new IllegalArgumentException("Count must be greater than 0");
        }

        if ("integral".equalsIgnoreCase(componentType)) {
            double itemCost = PROTECTIVE_SHEATH_CHARGE + INTEGRAL_PACKING_CHARGE + INTEGRAL_DELIVERY_CHARGE;
            double totalCharge = itemCost * count;
            log.info("Integral charge calculated: {}", totalCharge);
            return totalCharge;

        } else if ("accessory".equalsIgnoreCase(componentType)) {
            double itemCost = PROTECTIVE_SHEATH_CHARGE + ACCESSORY_PACKING_CHARGE + ACCESSORY_DELIVERY_CHARGE;
            double totalCharge = itemCost * count;
            log.info("Accessory charge calculated: {}", totalCharge);
            return totalCharge;

        } else {
            log.warn("Invalid component type: '{}'", componentType);
            throw new ComponentTypeNotFoundException(componentType);
        }
    }
}
```

### 💡 Critical Architectural Fix: Eliminating Shared Concurrency Bugs
- **The Problem in Legacy Code**: Legacy code declared `private int packagingAndDeliveryCost = 0;` as an instance field on the `@Service` singleton bean. Under multi-threaded concurrent web traffic, multiple Tomcat threads mutated this instance field simultaneously, overwriting each other's calculations and returning invalid charges!
- **The Fix**: The service is now **100% stateless**. All calculations take place within stack-allocated local variables (`itemCost`, `totalCharge`), ensuring absolute thread safety under any concurrency load.

---

## 2. `PackagingAndDeliveryController.java`

```java
package com.company.packaginganddeliveryservice.controller;

import com.company.packaginganddeliveryservice.service.PackagingAndDeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Packaging & Delivery Controller")
public class PackagingAndDeliveryController {

    private final PackagingAndDeliveryService packagingAndDeliveryService;

    @GetMapping("/PackagingAndDeliveryCharge/{componentType}/{count}")
    public ResponseEntity<Double> getPackagingAndDeliveryCharge(
            @PathVariable("componentType") String componentType,
            @PathVariable("count") int count
    ) {
        log.info("Received request for type: {}, count: {}", componentType, count);
        double totalCharge = packagingAndDeliveryService.getPackingAndDeliveryCharge(componentType, count);
        return ResponseEntity.ok(totalCharge);
    }
}
```

### 💡 Line-by-Line Explanation
- **Line 12 (`@RequiredArgsConstructor`)**: Generates constructor injection for `private final PackagingAndDeliveryService`, eliminating legacy `@Autowired` field injection.
- **Line 18 (`@GetMapping("/PackagingAndDeliveryCharge/{componentType}/{count}")`)**: Maps GET requests matching the OpenFeign contract expected by `ComponentProcessing`.
