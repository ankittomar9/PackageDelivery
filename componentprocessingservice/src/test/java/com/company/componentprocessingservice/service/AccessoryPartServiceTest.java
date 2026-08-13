package com.company.componentprocessingservice.service;

import com.company.componentprocessingservice.client.PackagingAndDeliveryClient;
import com.company.componentprocessingservice.entity.ProcessRequest;
import com.company.componentprocessingservice.entity.ProcessResponse;
import com.company.componentprocessingservice.repository.ProcessRequestRepository;
import com.company.componentprocessingservice.repository.ProcessResponseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessoryPartServiceTest {

    @Mock
    private ProcessRequestRepository processRequestRepository;

    @Mock
    private ProcessResponseRepository processResponseRepository;

    @Mock
    private PackagingAndDeliveryClient packagingAndDeliveryClient;

    @InjectMocks
    private AccessoryPartService accessoryPartService;

    private ProcessRequest accessoryRequest;

    @BeforeEach
    void setUp() {
        accessoryRequest = ProcessRequest.builder()
                .requestId(10L)
                .userName("sarah_connor")
                .componentType("Accessory")
                .componentName("Sony WH-1000XM5 Charging Cable")
                .quantityOfDefective(2)
                .isPriorityRequest(false)
                .build();
    }

    @Test
    @DisplayName("Should process Accessory in 5 days and charge 300")
    void processDetail_AccessoryRequest_Success() {
        when(processRequestRepository.findById(10L)).thenReturn(Optional.of(accessoryRequest));
        when(packagingAndDeliveryClient.getPackagingAndDeliveryCharge("Accessory", 2)).thenReturn(100.0);
        when(processResponseRepository.save(any(ProcessResponse.class))).thenAnswer(i -> i.getArgument(0));

        ProcessResponse response = accessoryPartService.processDetail(10L);

        assertNotNull(response);
        assertEquals("sarah_connor", response.getUserName());
        assertEquals(300.0, response.getProcessingCharge());
        assertEquals(100.0, response.getPackagingAndDeliveryCharge());
        assertEquals(LocalDate.now().plusDays(5), response.getDateOfDelivery());

        verify(processRequestRepository, times(1)).findById(10L);
    }
}