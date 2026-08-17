package com.company.notificationservice.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationConsumer {

    @KafkaListener(topics = "return-orders.paid", groupId = "notification-group")
    public void consumeOrderPaidEvent(Object eventPayload) {
        log.info("----------------------------------------------------------------");
        log.info(" KAFKA EVENT CONSUMED BY NOTIFICATION SERVICE!");
        log.info("Payload: {}", eventPayload);
        log.info("Dispatching SMS & Email Receipt to Customer...");
        log.info("----------------------------------------------------------------");
    }
}