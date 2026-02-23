package com.urviclean.recordbook.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DailySaleCustomerType {
    CUSTOMER("CUSTOMER"),
    SHOPKEEPER("SHOPKEEPER");

    private final String jsonValue;

    DailySaleCustomerType(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonValue
    public String getJsonValue() {
        return jsonValue;
    }

    @JsonCreator
    public static DailySaleCustomerType fromJson(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        if ("C".equals(normalized) || "CUSTOMER".equals(normalized)) {
            return CUSTOMER;
        }
        if ("S".equals(normalized) || "SHOPKEEPER".equals(normalized) || "SHOP_KEEPER".equals(normalized)) {
            return SHOPKEEPER;
        }
        throw new IllegalArgumentException("Unsupported customerType: " + value);
    }
}
