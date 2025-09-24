package com.mortgagecalculator.model;

public enum MortgageType {
    FIXED("Fixed Rate Mortgage"),
    VARIABLE("Variable Rate Mortgage"),
    INTEREST_ONLY("Interest Only Mortgage");

    private final String displayName;

    MortgageType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}