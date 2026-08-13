package com.company.packaginganddeliveryservice.service;

import com.company.packaginganddeliveryservice.exception.ComponentTypeNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PackagingAndDeliveryServiceTest {

    private PackagingAndDeliveryService service;

    @BeforeEach
    void setUp() {
        service = new PackagingAndDeliveryService();
    }

    @Test
    @DisplayName("Should calculate correct tariff for Integral Component (Count 1)")
    void getPackingAndDeliveryCharge_Integral_Success() {
        // (50 protective + 100 packing + 200 delivery) * 1 = 350.0
        double result = service.getPackingAndDeliveryCharge("Integral", 1);
        assertEquals(350.0, result);
    }

    @Test
    @DisplayName("Should calculate correct tariff for Accessory Component (Count 2)")
    void getPackingAndDeliveryCharge_Accessory_Success() {
        // (50 protective + 50 packing + 100 delivery) * 2 = 400.0
        double result = service.getPackingAndDeliveryCharge("Accessory", 2);
        assertEquals(400.0, result);
    }

    @Test
    @DisplayName("Should handle case-insensitive component type string ('integral')")
    void getPackingAndDeliveryCharge_CaseInsensitive_Success() {
        double result = service.getPackingAndDeliveryCharge("integral", 1);
        assertEquals(350.0, result);
    }

    @Test
    @DisplayName("Should throw ComponentTypeNotFoundException for unknown component type")
    void getPackingAndDeliveryCharge_UnknownType_ThrowsException() {
        assertThrows(ComponentTypeNotFoundException.class,
                () -> service.getPackingAndDeliveryCharge("UnknownType", 1));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when count is zero or negative")
    void getPackingAndDeliveryCharge_InvalidCount_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getPackingAndDeliveryCharge("Integral", 0));
    }
}