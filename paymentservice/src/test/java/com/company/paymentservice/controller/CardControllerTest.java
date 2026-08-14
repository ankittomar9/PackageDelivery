package com.company.paymentservice.controller;

import com.company.paymentservice.dto.PaymentReceiptDTO;
import com.company.paymentservice.service.CardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
@AutoConfigureMockMvc(addFilters = false)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @Test
    @DisplayName("GET /card/{cardNumber}/{charge} - Should return remaining balance (Feign Endpoint)")
    void getBalance_Success() throws Exception {
        when(cardService.processPayment(4532890123456789L, 700.0)).thenReturn(49300.0);

        mockMvc.perform(get("/card/4532890123456789/700.0"))
                .andExpect(status().isOk())
                .andExpect(content().string("49300.0"));
    }

    @Test
    @DisplayName("POST /api/v1/payments/stripe-charge - Should return Digital Payment Receipt")
    void executeStripeCharge_Success() throws Exception {
        PaymentReceiptDTO receipt = new PaymentReceiptDTO(
                "ch_stripe_abc123",
                "REC-2026-98123",
                "**** **** **** 6789",
                700.0,
                "INR",
                "SUCCESS",
                "STRIPE_GATEWAY",
                LocalDateTime.now(),
                "Payment Charge Authorized Successfully"
        );

        when(cardService.executeStripeGatewayCharge(4532890123456789L, 700.0, "INR")).thenReturn(receipt);

        mockMvc.perform(post("/api/v1/payments/stripe-charge")
                        .param("cardNumber", "4532890123456789")
                        .param("charge", "700.0")
                        .param("currency", "INR")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("ch_stripe_abc123"))
                .andExpect(jsonPath("$.receiptNumber").value("REC-2026-98123"))
                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"));
    }
}