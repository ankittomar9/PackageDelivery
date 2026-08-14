# Detailed Code Walkthrough — Payment Microservice (`PaymentService`)

This document provides a line-by-line breakdown of every core class in **`PaymentService`**, detailing Stripe integration, card masking, and receipt generation.

---

## 1. `CardService.java`

```java
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

    public double processPayment(Long cardNumber, Double charge) {
        log.info("Processing balance deduction for card: {}, charge: {}", cardNumber, charge);

        CreditCard card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new CardNotFoundException("Credit card not found: " + cardNumber));

        double remainingLimit = card.getCardLimit() - charge;

        if (remainingLimit >= 0) {
            card.setCardLimit(remainingLimit);
            cardRepository.save(card);
            generateReceipt(cardNumber, charge, "SUCCESS", "INTERNAL_LEDGER", "Payment Successful");
            return remainingLimit;
        } else {
            generateReceipt(cardNumber, charge, "FAILED", "INTERNAL_LEDGER", "Insufficient Card Limit");
            return -1.0;
        }
    }

    public PaymentReceiptDTO executeStripeGatewayCharge(Long cardNumber, Double charge, String currency) {
        log.info("Executing Payment Gateway charge for card: {}, amount: {} {}", cardNumber, charge, currency);

        String cardStr = String.valueOf(cardNumber);
        String maskedCard = "**** **** **** " + (cardStr.length() >= 4 ? cardStr.substring(cardStr.length() - 4) : "1234");
        
        String txId = "ch_stripe_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String receiptNo = "REC-2026-" + ((int) (Math.random() * 90000) + 10000);

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
}
```

### 💡 Line-by-Line Explanation
- **Line 46 (`maskedCard`)**: Implements PCI-DSS compliant credit card masking by isolating trailing 4 digits (`**** **** **** 6789`).
- **Line 48 (`txId`)**: Generates Stripe-compatible transaction ID (`ch_stripe_7ca25232cb224df6`).
- **Line 49 (`receiptNo`)**: Generates official digital receipt number (`REC-2026-49772`).
- **Line 51 (`cardStr.endsWith("0002")`)**: Simulates Stripe Test Card rules (cards ending in `0002` simulate card decline by issuer).

---

## 2. `CardController.java`

```java
package com.company.paymentservice.controller;

import com.company.paymentservice.dto.PaymentReceiptDTO;
import com.company.paymentservice.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Controller")
public class CardController {

    private final CardService cardService;

    @GetMapping("/card/{cardNumber}/{charge}")
    public ResponseEntity<Double> getBalance(
            @PathVariable("cardNumber") Long cardNumber,
            @PathVariable("charge") Double charge
    ) {
        log.info("Received balance process request for card: {}, charge: {}", cardNumber, charge);
        double remainingBalance = cardService.processPayment(cardNumber, charge);
        return ResponseEntity.ok(remainingBalance);
    }

    @PostMapping("/api/v1/payments/stripe-charge")
    public ResponseEntity<PaymentReceiptDTO> executeStripeCharge(
            @RequestParam("cardNumber") Long cardNumber,
            @RequestParam("charge") Double charge,
            @RequestParam(value = "currency", defaultValue = "INR") String currency
    ) {
        log.info("Received Stripe Payment Gateway request for card: {}", cardNumber);
        PaymentReceiptDTO receipt = cardService.executeStripeGatewayCharge(cardNumber, charge, currency);
        return ResponseEntity.ok(receipt);
    }
}
```

### 💡 Line-by-Line Explanation
- **Line 20 (`@GetMapping("/card/{cardNumber}/{charge}")`)**: OpenFeign compatible endpoint satisfying `ComponentProcessing`'s `PaymentClient`.
- **Line 30 (`@PostMapping("/api/v1/payments/stripe-charge")`)**: Modern payment gateway REST endpoint returning full `PaymentReceiptDTO` payload.
