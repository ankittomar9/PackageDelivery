package com.company.paymentservice.repository;

import com.company.paymentservice.entity.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<CreditCard, Long> {
    Optional<CreditCard> findByCardNumber(Long cardNumber);
}