package com.company.packaginganddeliveryservice.exception;

public class ComponentTypeNotFoundException extends RuntimeException {

    public ComponentTypeNotFoundException(String componentType) {
        super("Invalid component type: '" + componentType + "'. Supported types are 'Integral' and 'Accessory'.");
    }
}