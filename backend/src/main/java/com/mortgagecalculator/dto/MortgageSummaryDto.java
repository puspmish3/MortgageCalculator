package com.mortgagecalculator.dto;

import com.mortgagecalculator.model.PaymentFrequency;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Summary of mortgage loan details and totals")
public record MortgageSummaryDto(

        @Schema(description = "Total loan amount", example = "400000.00") BigDecimal loanAmount,

        @Schema(description = "Total interest paid over life of loan", example = "263616.00") BigDecimal totalInterestPaid,

        @Schema(description = "Total amount paid (principal + interest)", example = "663616.00") BigDecimal totalAmountPaid,

        @Schema(description = "Regular payment amount", example = "3513.80") BigDecimal monthlyPayment,

        @Schema(description = "Annual interest rate", example = "6.50") BigDecimal interestRate,

        @Schema(description = "Loan term in years", example = "30") Integer loanTermYears,

        @Schema(description = "Payment frequency") PaymentFrequency paymentFrequency) {
}