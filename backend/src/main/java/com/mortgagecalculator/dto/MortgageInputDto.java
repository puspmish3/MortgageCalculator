package com.mortgagecalculator.dto;

import com.mortgagecalculator.model.MortgageType;
import com.mortgagecalculator.model.PaymentFrequency;
import com.mortgagecalculator.model.BuydownType;
import com.mortgagecalculator.enums.AdditionalPaymentFrequency;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Mortgage calculation input parameters")
public record MortgageInputDto(
        
        @NotNull(message = "Loan amount is required")
        @DecimalMin(value = "1000.0", message = "Loan amount must be at least $1,000")
        @DecimalMax(value = "10000000.0", message = "Loan amount cannot exceed $10,000,000")
        @Schema(description = "Total loan amount", example = "400000.00")
        BigDecimal loanAmount,

        @NotNull(message = "Interest rate is required")
        @DecimalMin(value = "0.1", message = "Interest rate must be at least 0.1%")
        @DecimalMax(value = "30.0", message = "Interest rate cannot exceed 30%")
        @Schema(description = "Annual interest rate as percentage", example = "6.50")
        BigDecimal interestRate,

        @NotNull(message = "Loan term is required")
        @Min(value = 1, message = "Loan term must be at least 1 year")
        @Max(value = 50, message = "Loan term cannot exceed 50 years")
        @Schema(description = "Loan term in years", example = "30")
        Integer loanTermYears,

        @DecimalMin(value = "0.0", message = "Down payment cannot be negative")
        @Schema(description = "Down payment amount", example = "100000.00")
        BigDecimal downPayment,

        @DecimalMin(value = "0.0", message = "Property value cannot be negative")
        @Schema(description = "Total property value", example = "500000.00")
        BigDecimal propertyValue,

        @NotNull(message = "Mortgage type is required")
        @Schema(description = "Type of mortgage")
        MortgageType mortgageType,

        @NotNull(message = "Payment frequency is required")
        @Schema(description = "How often payments are made")
        PaymentFrequency paymentFrequency,

        @Schema(description = "Type of buydown option", example = "TWO_ONE")
        BuydownType buydownType,

        @DecimalMin(value = "0.0", message = "Additional principal payment cannot be negative")
        @Schema(description = "Additional principal payment per period", example = "200.00")
        BigDecimal additionalPrincipalPayment,

        @Schema(description = "Frequency of additional principal payments", example = "MONTHLY")
        AdditionalPaymentFrequency additionalPaymentFrequency
) {
    // Custom validation method
    public void validate() {
        if (downPayment != null && propertyValue != null && 
            downPayment.compareTo(propertyValue) > 0) {
            throw new IllegalArgumentException("Down payment cannot be greater than property value");
        }
        
        if (propertyValue != null && loanAmount != null &&
            propertyValue.subtract(downPayment != null ? downPayment : BigDecimal.ZERO)
                .compareTo(loanAmount) != 0) {
            // Allow some tolerance for calculation differences
            BigDecimal expectedLoanAmount = propertyValue.subtract(downPayment != null ? downPayment : BigDecimal.ZERO);
            BigDecimal difference = loanAmount.subtract(expectedLoanAmount).abs();
            if (difference.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new IllegalArgumentException("Loan amount should equal property value minus down payment");
            }
        }
    }
}