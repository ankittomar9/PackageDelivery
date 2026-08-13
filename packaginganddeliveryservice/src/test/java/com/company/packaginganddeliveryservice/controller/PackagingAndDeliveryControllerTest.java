package com.company.packaginganddeliveryservice.controller;

import com.company.packaginganddeliveryservice.exception.ComponentTypeNotFoundException;
import com.company.packaginganddeliveryservice.service.PackagingAndDeliveryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PackagingAndDeliveryController.class)
@AutoConfigureMockMvc(addFilters = false)
class PackagingAndDeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PackagingAndDeliveryService packagingAndDeliveryService;

    @Test
    @DisplayName("GET /PackagingAndDeliveryCharge/Integral/1 - Should return 200 OK with cost")
    void getPackagingAndDeliveryCharge_Integral_Returns200() throws Exception {
        when(packagingAndDeliveryService.getPackingAndDeliveryCharge("Integral", 1)).thenReturn(350.0);

        mockMvc.perform(get("/PackagingAndDeliveryCharge/Integral/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("350.0"));
    }

    @Test
    @DisplayName("GET /PackagingAndDeliveryCharge/Invalid/1 - Should return 404 Not Found")
    void getPackagingAndDeliveryCharge_InvalidType_Returns404() throws Exception {
        when(packagingAndDeliveryService.getPackingAndDeliveryCharge("Invalid", 1))
                .thenThrow(new ComponentTypeNotFoundException("Invalid"));

        mockMvc.perform(get("/PackagingAndDeliveryCharge/Invalid/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}