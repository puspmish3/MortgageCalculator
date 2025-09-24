package com.mortgagecalculator.model;

/**
 * Enumeration of available buydown types
 */
public enum BuydownType {
    NONE("No Buydown"),
    TWO_ONE("2-1 Buydown"),
    THREE_TWO_ONE("3-2-1 Buydown");
    
    private final String displayName;
    
    BuydownType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}