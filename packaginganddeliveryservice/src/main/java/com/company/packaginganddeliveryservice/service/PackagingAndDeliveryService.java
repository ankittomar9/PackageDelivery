package com.company.packaginganddeliveryservice.service;

import com.company.packaginganddeliveryservice.exception.ComponentTypeNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PackagingAndDeliveryService {

    // Tariff Constants (in INR)
    public static final double PROTECTIVE_SHEATH_CHARGE = 50.0;

    public static final double INTEGRAL_PACKING_CHARGE = 100.0;
    public static final double INTEGRAL_DELIVERY_CHARGE = 200.0;

    public static final double ACCESSORY_PACKING_CHARGE = 50.0;
    public static final double ACCESSORY_DELIVERY_CHARGE = 100.0;

    public double getPackingAndDeliveryCharge(String componentType, int count) {
        log.info("Calculating packaging and delivery charges for type: '{}', count: {}", componentType, count);

        if (count <= 0) {
            throw new IllegalArgumentException("Count must be greater than 0");
        }

        if ("integral".equalsIgnoreCase(componentType)) {
            // (50 + 100 + 200) * count = ₹350 per item
            double itemCost = PROTECTIVE_SHEATH_CHARGE + INTEGRAL_PACKING_CHARGE + INTEGRAL_DELIVERY_CHARGE;
            double totalCharge = itemCost * count;
            log.info("Integral component packaging charge: {} x {} = {}", itemCost, count, totalCharge);
            return totalCharge;

        } else if ("accessory".equalsIgnoreCase(componentType)) {
            // (50 + 50 + 100) * count = ₹200 per item
            double itemCost = PROTECTIVE_SHEATH_CHARGE + ACCESSORY_PACKING_CHARGE + ACCESSORY_DELIVERY_CHARGE;
            double totalCharge = itemCost * count;
            log.info("Accessory component packaging charge: {} x {} = {}", itemCost, count, totalCharge);
            return totalCharge;

        } else {
            log.warn("Invalid component type requested: '{}'", componentType);
            throw new ComponentTypeNotFoundException(componentType);
        }
    }
}