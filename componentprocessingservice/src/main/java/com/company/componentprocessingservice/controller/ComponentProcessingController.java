package com.company.componentprocessingservice.controller;

import com.company.componentprocessingservice.client.AuthClient;
import com.company.componentprocessingservice.dto.AuthResponseDTO;
import com.company.componentprocessingservice.dto.ProcessRequestDTO;
import com.company.componentprocessingservice.dto.ProcessResponseDTO;
import com.company.componentprocessingservice.entity.ProcessRequest;
import com.company.componentprocessingservice.entity.ProcessResponse;
import com.company.componentprocessingservice.repository.ProcessRequestRepository;
import com.company.componentprocessingservice.service.AccessoryPartService;
import com.company.componentprocessingservice.service.IntegralPartService;
import com.company.componentprocessingservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Component Processing Controller", description = "Endpoints for initiating component return processing and completing payments")
@CrossOrigin(origins = "*")
public class ComponentProcessingController {

    private final IntegralPartService integralPartService;
    private final AccessoryPartService accessoryPartService;
    private final PaymentService paymentService;
    private final ProcessRequestRepository processRequestRepository;
    private final AuthClient authClient;

    @Operation(summary = "Process Component Return Request", description = "Validates JWT, stores request, calculates charges & turnaround duration")
    @ApiResponse(responseCode = "200", description = "Return processing calculation completed")
    @ApiResponse(responseCode = "401", description = "Unauthorized access / Invalid token")
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

        // 2. Validate JWT Token with jwtAuthentication Microservice via OpenFeign
        AuthResponseDTO authResponse;
        try {
            authResponse = authClient.validateToken(tokenHeader);
            log.info("Token validation result from Auth Service: {}", authResponse);
        } catch (Exception e) {
            log.error("Feign Client Exception when calling Auth Service: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (authResponse == null || !Boolean.TRUE.equals(authResponse.valid())) {
            log.warn("Token is invalid or expired for user: {}", requestDTO.userName());
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

        // 4. Delegate to appropriate Strategy Service based on component type
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

        log.info("Successfully calculated return processing for Request ID: {}", responseEntity.getRequestId());
        return ResponseEntity.ok(responseDTO);
    }
    @Operation(summary = "Process Order Payment", description = "Completes payment processing for return order charges")
    @PostMapping("/payment/{requestID}/{creditCardNumber}/{creditLimit}/{processingCharge}")
    public ResponseEntity<String> paymentProcessing(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("requestID") Long requestID,
            @PathVariable("creditCardNumber") Long creditCardNumber,
            @PathVariable("creditLimit") Double creditLimit,
            @PathVariable("processingCharge") Double processingCharge
    ) {
        log.info("Received payment execution request for Request ID: {}", requestID);

        String tokenHeader = authHeader;
        if (!authHeader.startsWith("Bearer ")) {
            tokenHeader = "Bearer " + authHeader;
        }

        AuthResponseDTO authResponse;
        try {
            authResponse = authClient.validateToken(tokenHeader);
        } catch (Exception e) {
            log.error("Feign Client Exception when calling Auth Service: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization Failed. Please try again.");
        }

        if (authResponse == null || !Boolean.TRUE.equals(authResponse.valid())) {
            log.warn("Authorization failed during payment processing for Request ID: {}", requestID);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization Failed. Please try again.");
        }

        // Complete processing with safe local fallback
        String result = paymentService.completeProcessing(requestID, creditCardNumber, creditLimit, processingCharge);
        return ResponseEntity.ok(result);
    }

}