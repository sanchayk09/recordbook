package com.urviclean.recordbook.models;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ExpenseCategoryConverter implements AttributeConverter<ExpenseCategory, String> {
    @Override
    public String convertToDatabaseColumn(ExpenseCategory attribute) {
        if (attribute == null) return null;
        return attribute.name().replace('_', ' ');
    }

    @Override
    public ExpenseCategory convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        String key = dbData.replace(' ', '_');
        try {
            return ExpenseCategory.valueOf(key);
        } catch (IllegalArgumentException ex) {
            return ExpenseCategory.Other;
        }
    }
}
