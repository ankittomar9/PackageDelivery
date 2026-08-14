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
@Tag(name = "Payment Controller", description = "Endpoints for inter-service balance checks and Stripe Payment Gateway execution")
@CrossOrigin(origins = "*")
public class CardController {

    private final CardService cardService;

    @Operation(summary = "Process Inter-Service Card Payment (Feign Compatible)")
    @GetMapping("/card/{cardNumber}/{charge}")
    public ResponseEntity<Double> getBalance(
            @PathVariable("cardNumber") Long cardNumber,
            @PathVariable("charge") Double charge
    ) {
        log.info("Received balance process request for card: {}, charge: {}", cardNumber, charge);
        double remainingBalance = cardService.processPayment(cardNumber, charge);
        return ResponseEntity.ok(remainingBalance);
    }

    @Operation(summary = "Execute Stripe Payment Gateway Charge & Generate Receipt")
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