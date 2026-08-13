package com.company.componentprocessingservice.dto;

public record ProcessRequestDTO(
        String userName,
        Long contactNumber,
        Long creditCardNumber,
        String componentType,
        String componentName,
        Integer quantityOfDefective,
        Boolean isPriorityRequest
) {}