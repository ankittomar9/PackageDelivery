package com.company.paymentservice.service;

import com.company.paymentservice.dto.PaymentReceiptDTO;
import com.company.paymentservice.entity.CreditCard;
import com.company.paymentservice.entity.PaymentReceipt;
import com.company.paymentservice.exception.CardNotFoundException;
import com.company.paymentservice.repository.CardRepository;
import com.company.paymentservice.repository.PaymentReceiptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private PaymentReceiptRepository receiptRepository;

    @InjectMocks
    private CardService cardService;

    private CreditCard validCard;

    @BeforeEach
    void setUp() {
        validCard = CreditCard.builder()
                .cardNumber(4532890123456789L)
                .cardLimit(50000.0)
                .build();
    }

    @Test
    @DisplayName("Should deduct charge and return remaining limit when limit is sufficient")
    void processPayment_Success() {
        when(cardRepository.findByCardNumber(4532890123456789L)).thenReturn(Optional.of(validCard));
        when(cardRepository.save(any(CreditCard.class))).thenAnswer(i -> i.getArgument(0));

        double remainingBalance = cardService.processPayment(4532890123456789L, 700.0);

        assertEquals(49300.0, remainingBalance);
        verify(cardRepository, times(1)).save(validCard);
        verify(receiptRepository, times(1)).save(any(PaymentReceipt.class));
    }

    @Test
    @DisplayName("Should return -1.0 when charge exceeds card limit")
    void processPayment_InsufficientLimit_ReturnsMinusOne() {
        when(cardRepository.findByCardNumber(4532890123456789L)).thenReturn(Optional.of(validCard));

        double remainingBalance = cardService.processPayment(4532890123456789L, 60000.0);

        assertEquals(-1.0, remainingBalance);
        verify(cardRepository, never()).save(validCard);
    }

    @Test
    @DisplayName("Should throw CardNotFoundException when card does not exist")
    void processPayment_NotFound_ThrowsException() {
        when(cardRepository.findByCardNumber(9999L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.processPayment(9999L, 100.0));
    }

    @Test
    @DisplayName("Should execute Stripe Gateway charge and generate digital receipt")
    void executeStripeGatewayCharge_Success() {
        when(receiptRepository.save(any(PaymentReceipt.class))).thenAnswer(i -> i.getArgument(0));

        PaymentReceiptDTO receipt = cardService.executeStripeGatewayCharge(4532890123456789L, 700.0, "INR");

        assertNotNull(receipt);
        assertTrue(receipt.transactionId().startsWith("ch_stripe_"));
        assertTrue(receipt.receiptNumber().startsWith("REC-2026-"));
        assertEquals("**** **** **** 6789", receipt.cardNumberMasked());
        assertEquals("SUCCESS", receipt.paymentStatus());
        assertEquals(700.0, receipt.amountPaid());

        verify(receiptRepository, times(1)).save(any(PaymentReceipt.class));
    }
}