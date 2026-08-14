package com.company.paymentservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_receipt")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;

    @Column(name = "receipt_number", unique = true, nullable = false)
    private String receiptNumber;

    @Column(name = "card_number_masked")
    private String cardNumberMasked;

    @Column(name = "amount_paid")
    private Double amountPaid;

    @Column(name = "currency")
    private String currency;

    @Column(name = "payment_status")
    private String paymentStatus; // "SUCCESS", "FAILED", "DECLINED"

    @Column(name = "payment_provider")
    private String paymentProvider; // "STRIPE_TEST", "INTERNAL_LEDGER"

    @Column(name = "timestamp")
    private LocalDateTime timestamp;
}