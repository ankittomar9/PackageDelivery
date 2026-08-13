package com.company.componentprocessingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "process_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "contact_number")
    private Long contactNumber;

    @Column(name = "credit_card_number")
    private Long creditCardNumber;

    @Column(name = "component_type")
    private String componentType;

    @Column(name = "component_name")
    private String componentName;

    @Column(name = "quantity_of_defective")
    private Integer quantityOfDefective;

    @Column(name = "is_priority_request")
    private Boolean isPriorityRequest;

    // Explicit getter so processRequest.getIsPriorityRequest() resolves cleanly everywhere
    public Boolean getIsPriorityRequest() {
        return isPriorityRequest;
    }
}