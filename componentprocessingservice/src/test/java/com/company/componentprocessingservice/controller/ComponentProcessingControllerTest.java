package com.company.componentprocessingservice.controller;

import com.company.componentprocessingservice.client.AuthClient;
import com.company.componentprocessingservice.dto.AuthResponseDTO;
import com.company.componentprocessingservice.dto.ProcessRequestDTO;
import com.company.componentprocessingservice.entity.ProcessRequest;
import com.company.componentprocessingservice.entity.ProcessResponse;
import com.company.componentprocessingservice.repository.ProcessRequestRepository;
import com.company.componentprocessingservice.service.AccessoryPartService;
import com.company.componentprocessingservice.service.IntegralPartService;
import com.company.componentprocessingservice.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ComponentProcessingController.class)
@AutoConfigureMockMvc(addFilters = false)
class ComponentProcessingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IntegralPartService integralPartService;

    @MockitoBean
    private AccessoryPartService accessoryPartService;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private ProcessRequestRepository processRequestRepository;

    @MockitoBean
    private AuthClient authClient;

    private ObjectMapper objectMapper;
    private ProcessRequestDTO validRequestDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        validRequestDTO = new ProcessRequestDTO(
                "john_doe",
                9876543210L,
                4532890123456789L,
                "Integral",
                "MacBook Pro M3 Display Assembly",
                1,
                true
        );
    }

    @Test
    @DisplayName("POST /service - Should return 200 OK when JWT token is valid")
    void getProcessingDetails_ValidToken_Returns200() throws Exception {
        AuthResponseDTO validAuthResponse = new AuthResponseDTO("dummy-jwt-token", true);
        when(authClient.validateToken(anyString())).thenReturn(validAuthResponse);

        ProcessRequest savedRequest = ProcessRequest.builder()
                .requestId(1L)
                .userName("john_doe")
                .componentType("Integral")
                .build();
        when(processRequestRepository.save(any(ProcessRequest.class))).thenReturn(savedRequest);

        ProcessResponse mockResponse = ProcessResponse.builder()
                .requestId(1L)
                .userName("john_doe")
                .processingCharge(700.0)
                .packagingAndDeliveryCharge(150.0)
                .dateOfDelivery(LocalDate.now().plusDays(2))
                .build();
        when(integralPartService.processDetail(1L)).thenReturn(mockResponse);

        mockMvc.perform(post("/service")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(1))
                .andExpect(jsonPath("$.userName").value("john_doe"))
                .andExpect(jsonPath("$.processingCharge").value(700.0));
    }

    @Test
    @DisplayName("POST /service - Should return 401 Unauthorized when JWT token is invalid")
    void getProcessingDetails_InvalidToken_Returns401() throws Exception {
        AuthResponseDTO invalidAuthResponse = new AuthResponseDTO("dummy-jwt-token", false);
        when(authClient.validateToken(anyString())).thenReturn(invalidAuthResponse);

        mockMvc.perform(post("/service")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isUnauthorized());
    }
}