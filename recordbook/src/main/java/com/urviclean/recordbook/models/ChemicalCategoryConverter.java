package com.urviclean.recordbook.models;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ChemicalCategoryConverter implements AttributeConverter<ChemicalCategory, String> {
    @Override
    public String convertToDatabaseColumn(ChemicalCategory attribute) {
        if (attribute == null) return null;
        // Convert enum name with underscores to DB value with spaces
        return attribute.name().replace('_', ' ');
    }

    @Override
    public ChemicalCategory convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        String key = dbData.replace(' ', '_');
        try {
            return ChemicalCategory.valueOf(key);
        } catch (IllegalArgumentException ex) {
            // Unknown value: return OTHER as fallback
            return ChemicalCategory.Other;
        }
    }
}
