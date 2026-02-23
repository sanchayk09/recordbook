package com.urviclean.recordbook.utils;

import java.math.BigDecimal;

public class CommissionCalculator {

    /**
     * Calculate agent commission based on product code, rate, and quantity.
     *
     * Commission is calculated per unit based on rate, then multiplied by quantity.
     *
     * Rules per unit:
     * - N1/L1: Base 5 rs if rate <= 35, else 5 + 50% of (rate - 35)
     * - N500/L500: Base 5 rs if rate <= 25, else 5 + 50% of (rate - 25)
     * - N5/L5: Base 25 rs if rate <= 150, else 25 + 50% of (rate - 150)
     * - Others: 0 rs
     *
     * Total commission = per unit commission × quantity
     */
    public static BigDecimal calculateCommission(String productCode, BigDecimal rate, Integer quantity) {
        if (productCode == null || rate == null || quantity == null || quantity <= 0) {
            return BigDecimal.ZERO;
        }

        String code = productCode.toUpperCase().trim();
        BigDecimal perUnitCommission = BigDecimal.ZERO;

        if (code.equals("N1") || code.equals("L1")) {
            // Base 5 rs if rate <= 35
            if (rate.compareTo(BigDecimal.valueOf(35)) <= 0) {
                perUnitCommission = BigDecimal.valueOf(5);
            } else {
                // 5 + 50% of (rate - 35)
                BigDecimal excess = rate.subtract(BigDecimal.valueOf(35));
                BigDecimal excessCommission = excess.multiply(BigDecimal.valueOf(0.5));
                perUnitCommission = BigDecimal.valueOf(5).add(excessCommission);
            }
        } else if (code.equals("N500") || code.equals("L500")) {
            // Base 5 rs if rate <= 25
            if (rate.compareTo(BigDecimal.valueOf(25)) <= 0) {
                perUnitCommission = BigDecimal.valueOf(5);
            } else {
                // 5 + 50% of (rate - 25)
                BigDecimal excess = rate.subtract(BigDecimal.valueOf(25));
                BigDecimal excessCommission = excess.multiply(BigDecimal.valueOf(0.5));
                perUnitCommission = BigDecimal.valueOf(5).add(excessCommission);
            }
        } else if (code.equals("N5") || code.equals("L5")) {
            // Base 25 rs if rate <= 150
            if (rate.compareTo(BigDecimal.valueOf(150)) <= 0) {
                perUnitCommission = BigDecimal.valueOf(25);
            } else {
                // 25 + 50% of (rate - 150)
                BigDecimal excess = rate.subtract(BigDecimal.valueOf(150));
                BigDecimal excessCommission = excess.multiply(BigDecimal.valueOf(0.5));
                perUnitCommission = BigDecimal.valueOf(25).add(excessCommission);
            }
        }

        // Total commission = per unit commission × quantity
        return perUnitCommission.multiply(BigDecimal.valueOf(quantity));
    }
}

