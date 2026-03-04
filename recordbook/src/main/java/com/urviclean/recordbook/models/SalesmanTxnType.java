package com.urviclean.recordbook.models;

/**
 * Enum for Salesman Ledger Transaction Types
 * Records all movements of stock with salesmen
 */
public enum SalesmanTxnType {
    ISSUE_FROM_WAREHOUSE("Stock issued from warehouse to salesman"),
    SOLD("Stock sold by salesman to customer"),
    RETURN_TO_WAREHOUSE("Stock returned by salesman to warehouse"),
    MANUAL_ADJUST("Manual adjustment of salesman stock"),
    DAMAGE("Damaged stock with salesman");

    private final String description;

    SalesmanTxnType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

