package com.company.paymentservice.dto;

import java.time.LocalDateTime;

public record PaymentReceiptDTO(
        String transactionId,
        String receiptNumber,
        String cardNumberMasked,
        Double amountPaid,
        String currency,
        String paymentStatus,
        String paymentProvider,
        LocalDateTime timestamp,
        String message
) {}