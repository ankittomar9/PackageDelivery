# Detailed Code Walkthrough — Component Processing Microservice (`ComponentProcessing`)

This document provides a line-by-line breakdown of every core class in the **`ComponentProcessing`** microservice.

---

## 1. `ComponentProcessingApplication.java`

```java
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
```

### 💡 Line-by-Line Explanation
- **Line 6 (`@SpringBootApplication`)**: Enables Spring Boot auto-configuration, component scanning under `com.company.componentprocessingservice`, and bean configuration.
- **Line 7 (`@EnableFeignClients`)**: Activates Spring Cloud OpenFeign scanner to locate interfaces annotated with `@FeignClient` (`AuthClient`, `PackagingAndDeliveryClient`, `PaymentClient`) and register dynamic HTTP proxies.

---

## 2. OpenFeign Clients (`client` Package)

### `AuthClient.java`
```java
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
```

### 💡 Line-by-Line Explanation
- **Line 8 (`@FeignClient(...)`)**: Registers OpenFeign HTTP proxy named `authClient`. The `url = "${auth.service.url:http://localhost:8084}"` reads property `auth.service.url` from `application.properties`, defaulting to `http://localhost:8084` if not specified.
- **Line 11 (`@GetMapping("/validate")`)**: Automatically converts call `validateToken(token)` into an HTTP GET request to `http://localhost:8084/validate`, passing the `Authorization` header.

---

## 3. Strategy Pattern Service Layer (`service` Package)

### `IntegralPartService.java`
```java
package com.company.componentprocessingservice.service;

import com.company.componentprocessingservice.client.PackagingAndDeliveryClient;
import com.company.componentprocessingservice.entity.ProcessRequest;
import com.company.componentprocessingservice.entity.ProcessResponse;
import com.company.componentprocessingservice.repository.ProcessRequestRepository;
import com.company.componentprocessingservice.repository.ProcessResponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service("integralPartService")
@RequiredArgsConstructor
@Slf4j
public class IntegralPartService implements ProcessService {

    private final ProcessRequestRepository processRequestRepository;
    private final ProcessResponseRepository processResponseRepository;
    private final PackagingAndDeliveryClient packagingAndDeliveryClient;

    @Override
    public ProcessResponse processDetail(Long requestId) {
        log.info("Processing Integral Component Return Request for Request ID: {}", requestId);

        ProcessRequest processRequest = processRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Process Request not found with ID: " + requestId));

        int processingDays = 5;
        double processingCharge = 500.0;

        if (Boolean.TRUE.equals(processRequest.getIsPriorityRequest())) {
            processingDays = 2;
            processingCharge += 200.0; // Total ₹700
            log.info("Priority request detected for Request ID: {}. Expediting to 2 days.", requestId);
        }

        double packagingAndDeliveryCharge;
        try {
            packagingAndDeliveryCharge = packagingAndDeliveryClient.getPackagingAndDeliveryCharge(
                    processRequest.getComponentType(),
                    processRequest.getQuantityOfDefective()
            );
        } catch (Exception e) {
            log.warn("Packaging & Delivery Microservice (8082) offline. Applying fallback tariff: 150.0");
            packagingAndDeliveryCharge = 150.0;
        }

        LocalDate dateOfDelivery = LocalDate.now().plusDays(processingDays);

        ProcessResponse processResponse = ProcessResponse.builder()
                .userName(processRequest.getUserName())
                .processingCharge(processingCharge)
                .packagingAndDeliveryCharge(packagingAndDeliveryCharge)
                .dateOfDelivery(dateOfDelivery)
                .build();

        return processResponseRepository.save(processResponse);
    }
}
```

### 💡 Why this design is superior:
1. **`java.time.LocalDate`**: Replaces legacy 6-line `Calendar c = Calendar.getInstance()` and `SimpleDateFormat` with thread-safe `LocalDate.now().plusDays(processingDays)`.
2. **`orElseThrow(...)`**: Safe `Optional` unwrapping avoids `NoSuchElementException` crashes.
3. **Resilient OpenFeign Fallback**: Intercepts downstream connection timeouts gracefully with standard fallback tariffs.

---

## 4. REST Controller Layer (`controller` Package)

### `ComponentProcessingController.java`
```java
    @PostMapping("/service")
    public ResponseEntity<ProcessResponseDTO> getProcessingDetails(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ProcessRequestDTO requestDTO
    ) {
        log.info("Received component return request for user: {}", requestDTO.userName());

        // 1. Ensure token header formatted correctly
        String tokenHeader = authHeader;
        if (!authHeader.startsWith("Bearer ")) {
            tokenHeader = "Bearer " + authHeader;
        }

        // 2. Validate JWT Token via OpenFeign
        AuthResponseDTO authResponse;
        try {
            authResponse = authClient.validateToken(tokenHeader);
        } catch (Exception e) {
            log.error("Feign Exception calling Auth Service: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (authResponse == null || !Boolean.TRUE.equals(authResponse.valid())) {
            log.warn("Token invalid or expired for user: {}", requestDTO.userName());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 3. Save ProcessRequest entity to H2 Database
        ProcessRequest requestEntity = ProcessRequest.builder()
                .userName(requestDTO.userName())
                .contactNumber(requestDTO.contactNumber())
                .creditCardNumber(requestDTO.creditCardNumber())
                .componentType(requestDTO.componentType())
                .componentName(requestDTO.componentName())
                .quantityOfDefective(requestDTO.quantityOfDefective())
                .isPriorityRequest(requestDTO.isPriorityRequest())
                .build();

        ProcessRequest savedRequest = processRequestRepository.save(requestEntity);

        // 4. Delegate to appropriate Strategy Service
        ProcessResponse responseEntity;
        if ("integral".equalsIgnoreCase(requestDTO.componentType())) {
            responseEntity = integralPartService.processDetail(savedRequest.getRequestId());
        } else {
            responseEntity = accessoryPartService.processDetail(savedRequest.getRequestId());
        }

        // 5. Map Entity to Java 21 Record DTO
        ProcessResponseDTO responseDTO = new ProcessResponseDTO(
                responseEntity.getRequestId(),
                responseEntity.getUserName(),
                responseEntity.getProcessingCharge(),
                responseEntity.getPackagingAndDeliveryCharge(),
                responseEntity.getDateOfDelivery()
        );

        return ResponseEntity.ok(responseDTO);
    }
```

### 💡 Key Design Highlights
- **Case-Insensitive Component Routing**: `"integral".equalsIgnoreCase(...)` prevents bug where input `"Integral"` vs `"integral"` falls back incorrectly.
- **Java 21 Record Accessors**: Accesses properties via `requestDTO.userName()`, `requestDTO.componentType()`.
