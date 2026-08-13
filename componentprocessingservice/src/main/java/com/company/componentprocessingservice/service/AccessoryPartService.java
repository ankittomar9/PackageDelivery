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

@Service("accessoryPartService")
@RequiredArgsConstructor
@Slf4j
public class AccessoryPartService implements ProcessService {

    private final ProcessRequestRepository processRequestRepository;
    private final ProcessResponseRepository processResponseRepository;
    private final PackagingAndDeliveryClient packagingAndDeliveryClient;

    @Override
    public ProcessResponse processDetail(Long requestId) {
        log.info("Processing Accessory Component Return Request for Request ID: {}", requestId);

        ProcessRequest processRequest = processRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Process Request not found with ID: " + requestId));

        int processingDays = 5;
        double processingCharge = 300.0;

        log.info("Calling Packaging & Delivery Microservice for type: {}, count: {}",
                processRequest.getComponentType(), processRequest.getQuantityOfDefective());

//        double packagingAndDeliveryCharge = packagingAndDeliveryClient.getPackagingAndDeliveryCharge(
//                processRequest.getComponentType(),
//                processRequest.getQuantityOfDefective()
//        );

        double packagingAndDeliveryCharge;
        try {
            packagingAndDeliveryCharge = packagingAndDeliveryClient.getPackagingAndDeliveryCharge(
                    processRequest.getComponentType(),
                    processRequest.getQuantityOfDefective()
            );
        } catch (Exception e) {
            log.warn("Packaging & Delivery Microservice (8082) offline. Applying fallback tariff: 100.0");
            packagingAndDeliveryCharge = 100.0;
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