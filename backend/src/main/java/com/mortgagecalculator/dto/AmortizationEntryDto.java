package com.mortgagecalculator.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Single payment entry in the amortization schedule")
public record AmortizationEntryDto(
        
        @Schema(description = "Payment number in sequence", example = "1")
        Integer paymentNumber,

        @Schema(description = "Date of the payment")
        LocalDate paymentDate,

        @Schema(description = "Principal portion of the payment", example = "1347.13")
        BigDecimal principalPayment,

        @Schema(description = "Interest portion of the payment", example = "2166.67")
        BigDecimal interestPayment,

        @Schema(description = "Additional principal payment amount", example = "500.00")
        BigDecimal additionalPrincipalPayment,

        @Schema(description = "Regular payment amount (principal + interest)", example = "3513.80")
        BigDecimal regularPayment,

        @Schema(description = "Total payment amount including additional principal", example = "4013.80")
        BigDecimal totalPayment,

        @Schema(description = "Remaining loan balance after payment", example = "398152.87")
        BigDecimal remainingBalance,

        @Schema(description = "Interest rate applicable for this payment period", example = "6.50")
        BigDecimal interestRate,

        @Schema(description = "Interest saved compared to regular payment schedule", example = "123.45")
        BigDecimal interestSaved
) {}