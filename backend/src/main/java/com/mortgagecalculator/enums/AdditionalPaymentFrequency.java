package com.mortgagecalculator.enums;

import com.mortgagecalculator.model.PaymentFrequency;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Frequency options for additional principal payments")
public enum AdditionalPaymentFrequency {
    
    @Schema(description = "Additional payment made monthly")
    MONTHLY(12, "Monthly"),
    
    @Schema(description = "Additional payment made bi-weekly (26 times per year)")
    BI_WEEKLY(26, "Bi-Weekly"),
    
    @Schema(description = "Additional payment made quarterly (4 times per year)")
    QUARTERLY(4, "Quarterly"),
    
    @Schema(description = "Additional payment made semi-annually (2 times per year)")
    SEMI_ANNUALLY(2, "Semi-Annually"),
    
    @Schema(description = "Additional payment made annually (once per year)")
    ANNUALLY(1, "Annually"),
    
    @Schema(description = "One-time additional payment")
    ONE_TIME(0, "One-Time Payment");

    private final int paymentsPerYear;
    private final String displayName;

    AdditionalPaymentFrequency(int paymentsPerYear, String displayName) {
        this.paymentsPerYear = paymentsPerYear;
        this.displayName = displayName;
    }

    /**
     * Get the number of additional payments made per year for this frequency
     * @return Number of payments per year (0 for one-time payments)
     */
    public int getPaymentsPerYear() {
        return paymentsPerYear;
    }

    /**
     * Get the display name for this frequency
     * @return Human-readable name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Check if this is a one-time payment
     * @return true if this is a one-time payment frequency
     */
    public boolean isOneTime() {
        return this == ONE_TIME;
    }

    /**
     * Calculate how many regular payment periods occur between additional payments
     * @param regularPaymentFrequency The regular mortgage payment frequency
     * @return Number of regular payments between additional payments (0 for one-time)
     */
    public int getPaymentInterval(PaymentFrequency regularPaymentFrequency) {
        if (isOneTime()) {
            return 0; // Special case for one-time payments
        }
        
        int regularPaymentsPerYear = regularPaymentFrequency.getPaymentsPerYear();
        
        if (paymentsPerYear == 0 || regularPaymentsPerYear % paymentsPerYear != 0) {
            return regularPaymentsPerYear; // Default to once per year if not evenly divisible
        }
        
        return regularPaymentsPerYear / paymentsPerYear;
    }
}