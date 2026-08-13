package com.company.componentprocessingservice.dto;

import java.time.LocalDate;

public record ProcessResponseDTO(
        Long requestId,
        String userName,
        Double processingCharge,
        Double packagingAndDeliveryCharge,
        LocalDate dateOfDelivery
) {}