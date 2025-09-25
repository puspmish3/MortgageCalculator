package com.mortgagecalculator.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Complete mortgage calculation results")
public record MortgageCalculationDto(

        @Schema(description = "Regular payment amount", example = "3513.80") BigDecimal monthlyPayment,

        @Schema(description = "Total interest paid over life of loan", example = "263616.00") BigDecimal totalInterest,

        @Schema(description = "Total number of payments", example = "360") Integer totalPayments,

        @Schema(description = "Complete amortization schedule") List<AmortizationEntryDto> amortizationSchedule,

        @Schema(description = "Loan summary information") MortgageSummaryDto summary,

        @Schema(description = "Unique identifier for this calculation") String calculationId) {
}