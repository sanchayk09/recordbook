package com.urviclean.recordbook.utils;

import java.math.BigDecimal;

/**
 * Utility class to calculate volume sold based on product code and quantity.
 *
 * Volume calculation rules:
 * - n500 or l500: volume_in_quantity = 0.5
 * - n5 or l5: volume_in_quantity = 5
 * - n1 or l1: volume_in_quantity = 1
 * - Default: volume_in_quantity = 1
 *
 * volume_sold = quantity * volume_in_quantity
 */
public class VolumeCalculator {

    /**
     * Calculate volume sold based on product code and quantity.
     *
     * @param productCode The product code (case-insensitive)
     * @param quantity The quantity sold
     * @return BigDecimal representing the volume sold
     */
    public static BigDecimal calculateVolumeSold(String productCode, Integer quantity) {
        if (productCode == null || quantity == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal volumeInQuantity = getVolumeInQuantity(productCode);
        return volumeInQuantity.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Get volume_in_quantity based on product code.
     *
     * @param productCode The product code (case-insensitive)
     * @return BigDecimal representing the volume per unit
     */
    public static BigDecimal getVolumeInQuantity(String productCode) {
        if (productCode == null || productCode.isBlank()) {
            return BigDecimal.ONE;
        }

        String code = productCode.trim().toLowerCase();

        // Check for 500ml variants
        if (code.equals("n500") || code.equals("l500")) {
            return new BigDecimal("0.5");
        }

        // Check for 5L variants
        if (code.equals("n5") || code.equals("l5")) {
            return new BigDecimal("5");
        }

        // Check for 1L variants
        if (code.equals("n1") || code.equals("l1")) {
            return BigDecimal.ONE;
        }

        // Default to 1 for unknown product codes
        return BigDecimal.ONE;
    }
}

