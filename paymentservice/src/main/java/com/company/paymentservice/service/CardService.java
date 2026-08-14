package com.company.paymentservice.service;

import com.company.paymentservice.dto.PaymentReceiptDTO;
import com.company.paymentservice.entity.CreditCard;
import com.company.paymentservice.entity.PaymentReceipt;
import com.company.paymentservice.exception.CardNotFoundException;
import com.company.paymentservice.repository.CardRepository;
import com.company.paymentservice.repository.PaymentReceiptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    private final CardRepository cardRepository;
    private final PaymentReceiptRepository receiptRepository;

    /**
     * Legacy & Feign Compatible Balance Deduction
     */
    public double processPayment(Long cardNumber, Double charge) {
        log.info("Processing balance deduction for card: {}, charge: {}", cardNumber, charge);

        CreditCard card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new CardNotFoundException("Credit card not found: " + cardNumber));

        double remainingLimit = card.getCardLimit() - charge;

        if (remainingLimit >= 0) {
            card.setCardLimit(remainingLimit);
            cardRepository.save(card);

            // Generate Digital Receipt Audit Record
            generateReceipt(cardNumber, charge, "SUCCESS", "INTERNAL_LEDGER", "Payment Successful");
            log.info("Payment successful. Remaining card limit: {}", remainingLimit);
            return remainingLimit;
        } else {
            generateReceipt(cardNumber, charge, "FAILED", "INTERNAL_LEDGER", "Insufficient Card Limit");
            log.warn("Payment failed due to insufficient limit for card: {}", cardNumber);
            return -1.0;
        }
    }

    /**
     * Modern Stripe / Gateway Charge Execution with Receipt Generation
     */
    public PaymentReceiptDTO executeStripeGatewayCharge(Long cardNumber, Double charge, String currency) {
        log.info("Executing Payment Gateway charge for card: {}, amount: {} {}", cardNumber, charge, currency);

        String cardStr = String.valueOf(cardNumber);
        String maskedCard = "**** **** **** " + (cardStr.length() >= 4 ? cardStr.substring(cardStr.length() - 4) : "1234");

        String txId = "ch_stripe_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String receiptNo = "REC-2026-" + ((int) (Math.random() * 90000) + 10000);

        // Stripe Test Card Rule Simulation: Card ending in 0002 fails, all others succeed!
        String status = cardStr.endsWith("0002") ? "DECLINED" : "SUCCESS";
        String message = status.equals("SUCCESS") ? "Stripe Payment Charge Authorized Successfully" : "Card Declined by Issuer";

        PaymentReceipt receipt = PaymentReceipt.builder()
                .transactionId(txId)
                .receiptNumber(receiptNo)
                .cardNumberMasked(maskedCard)
                .amountPaid(charge)
                .currency(currency != null ? currency.toUpperCase() : "INR")
                .paymentStatus(status)
                .paymentProvider("STRIPE_GATEWAY")
                .timestamp(LocalDateTime.now())
                .build();

        receiptRepository.save(receipt);

        return new PaymentReceiptDTO(
                txId,
                receiptNo,
                maskedCard,
                charge,
                receipt.getCurrency(),
                status,
                "STRIPE_GATEWAY",
                receipt.getTimestamp(),
                message
        );
    }

    private void generateReceipt(Long cardNumber, Double amount, String status, String provider, String message) {
        String cardStr = String.valueOf(cardNumber);
        String masked = "**** **** **** " + (cardStr.length() >= 4 ? cardStr.substring(cardStr.length() - 4) : "1234");

        PaymentReceipt receipt = PaymentReceipt.builder()
                .transactionId("tx_" + UUID.randomUUID().toString().substring(0, 12))
                .receiptNumber("REC-2026-" + ((int) (Math.random() * 90000) + 10000))
                .cardNumberMasked(masked)
                .amountPaid(amount)
                .currency("INR")
                .paymentStatus(status)
                .paymentProvider(provider)
                .timestamp(LocalDateTime.now())
                .build();

        receiptRepository.save(receipt);
    }
}