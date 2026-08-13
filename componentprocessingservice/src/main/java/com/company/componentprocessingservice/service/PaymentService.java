package com.company.componentprocessingservice.service;

import com.company.componentprocessingservice.client.PaymentClient;
import com.company.componentprocessingservice.entity.Payment;
import com.company.componentprocessingservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentClient paymentClient;

    public String completeProcessing(Long requestId, Long creditCardNumber, Double creditLimit, Double processingCharge) {
        log.info("Initiating payment processing for Request ID: {}", requestId);

        Payment payment = new Payment(requestId.intValue(), creditCardNumber, creditLimit, processingCharge);
        paymentRepository.save(payment);

        log.info("Calling Payment Microservice for card: {}", creditCardNumber);

        double currentBalance = paymentClient.getCurrentBalance(creditCardNumber, processingCharge);

        if (currentBalance <= -1) {
            log.warn("Payment failed due to insufficient limit for Request ID: {}", requestId);
            return "We are sorry. Your payment could not be processed due to insufficient limit.";
        } else {
            log.info("Payment successful for Request ID: {}. Remaining balance: {}", requestId, currentBalance);
            return "Your Payment is successful. Thank you for using our service.";
        }
    }
}