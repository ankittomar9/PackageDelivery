package com.company.paymentservice.repository;

import com.company.paymentservice.entity.PaymentReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentReceiptRepository extends JpaRepository<PaymentReceipt, Long> {
    Optional<PaymentReceipt> findByTransactionId(String transactionId);
}