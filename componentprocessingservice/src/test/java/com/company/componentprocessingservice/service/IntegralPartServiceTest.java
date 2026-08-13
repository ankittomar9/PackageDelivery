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
class IntegralPartServiceTest {

    @Mock
    private ProcessRequestRepository processRequestRepository;

    @Mock
    private ProcessResponseRepository processResponseRepository;

    @Mock
    private PackagingAndDeliveryClient packagingAndDeliveryClient;

    @InjectMocks
    private IntegralPartService integralPartService;

    private ProcessRequest priorityRequest;
    private ProcessRequest standardRequest;

    @BeforeEach
    void setUp() {
        priorityRequest = ProcessRequest.builder()
                .requestId(1L)
                .userName("john_doe")
                .componentType("Integral")
                .componentName("MacBook Pro M3 Display")
                .quantityOfDefective(1)
                .isPriorityRequest(true)
                .build();

        standardRequest = ProcessRequest.builder()
                .requestId(2L)
                .userName("david_miller")
                .componentType("Integral")
                .componentName("Galaxy S24 Main Logic Board")
                .quantityOfDefective(1)
                .isPriorityRequest(false)
                .build();
    }

    @Test
    @DisplayName("Should expedite processing to 2 days and charge 700 for Priority Integral Request")
    void processDetail_PriorityRequest_Success() {
        when(processRequestRepository.findById(1L)).thenReturn(Optional.of(priorityRequest));
        when(packagingAndDeliveryClient.getPackagingAndDeliveryCharge("Integral", 1)).thenReturn(150.0);
        when(processResponseRepository.save(any(ProcessResponse.class))).thenAnswer(i -> i.getArgument(0));

        ProcessResponse response = integralPartService.processDetail(1L);

        assertNotNull(response);
        assertEquals("john_doe", response.getUserName());
        assertEquals(700.0, response.getProcessingCharge()); // 500 + 200 priority fee
        assertEquals(150.0, response.getPackagingAndDeliveryCharge());
        assertEquals(LocalDate.now().plusDays(2), response.getDateOfDelivery());

        verify(processRequestRepository, times(1)).findById(1L);
        verify(processResponseRepository, times(1)).save(any(ProcessResponse.class));
    }

    @Test
    @DisplayName("Should process in 5 days and charge 500 for Standard Non-Priority Integral Request")
    void processDetail_StandardRequest_Success() {
        when(processRequestRepository.findById(2L)).thenReturn(Optional.of(standardRequest));
        when(packagingAndDeliveryClient.getPackagingAndDeliveryCharge("Integral", 1)).thenReturn(150.0);
        when(processResponseRepository.save(any(ProcessResponse.class))).thenAnswer(i -> i.getArgument(0));

        ProcessResponse response = integralPartService.processDetail(2L);

        assertNotNull(response);
        assertEquals(500.0, response.getProcessingCharge()); // Standard 500 fee
        assertEquals(LocalDate.now().plusDays(5), response.getDateOfDelivery());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when Request ID does not exist")
    void processDetail_NotFound_ThrowsException() {
        when(processRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> integralPartService.processDetail(99L));
        verify(processResponseRepository, never()).save(any());
    }
}