package com.mortgagecalculator.model;

public enum PaymentFrequency {
    MONTHLY(12, "Monthly"),
    BI_WEEKLY(26, "Bi-weekly"),
    WEEKLY(52, "Weekly");

    private final int paymentsPerYear;
    private final String displayName;

    PaymentFrequency(int paymentsPerYear, String displayName) {
        this.paymentsPerYear = paymentsPerYear;
        this.displayName = displayName;
    }

    public int getPaymentsPerYear() {
        return paymentsPerYear;
    }

    public String getDisplayName() {
        return displayName;
    }
}