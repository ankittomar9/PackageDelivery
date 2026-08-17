package com.company.paymentservice.dto;

import java.time.LocalDateTime;

public record OrderReturnPaidEvent(
        String transactionId,
        String receiptNumber,
        Long cardNumberMasked,
        Double amountPaid,
        String paymentStatus,
        LocalDateTime timestamp
) {}