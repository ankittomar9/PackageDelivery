package com.company.componentprocessingservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @Column(name = "request_id")
    private Integer requestId;

    @Column(name = "credit_card_number")
    private Long creditCardNumber;

    @Column(name = "credit_limit")
    private Double creditLimit;

    @Column(name = "processing_charge")
    private Double processingCharge;

    // Explicit 4-arg constructor matching PaymentService parameters
    public Payment(int requestId, Long creditCardNumber, Double creditLimit, Double processingCharge) {
        this.requestId = requestId;
        this.creditCardNumber = creditCardNumber;
        this.creditLimit = creditLimit;
        this.processingCharge = processingCharge;
    }
}