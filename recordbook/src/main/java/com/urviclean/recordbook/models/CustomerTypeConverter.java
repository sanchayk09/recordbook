package com.urviclean.recordbook.models;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CustomerTypeConverter implements AttributeConverter<CustomerType, String> {
    @Override
    public String convertToDatabaseColumn(CustomerType attribute) {
        if (attribute == null) return null;
        return attribute.name();
    }

    @Override
    public CustomerType convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return CustomerType.valueOf(dbData);
        } catch (IllegalArgumentException ex) {
            return CustomerType.Shopkeeper; // fallback
        }
    }
}
