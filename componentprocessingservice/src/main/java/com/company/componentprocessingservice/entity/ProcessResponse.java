package com.company.componentprocessingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "process_response")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "processing_charge")
    private Double processingCharge;

    @Column(name = "packaging_and_delivery_charge")
    private Double packagingAndDeliveryCharge;

    @Column(name = "date_of_delivery")
    private LocalDate dateOfDelivery;
}