package com.company.paymentservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "credit_card")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditCard {

    @Id
    @Column(name = "card_number")
    private Long cardNumber;

    @Column(name = "card_limit")
    private Double cardLimit;
}