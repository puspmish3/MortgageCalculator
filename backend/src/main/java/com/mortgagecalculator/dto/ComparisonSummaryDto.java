package com.mortgagecalculator.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Summary of mortgage comparison results")
public record ComparisonSummaryDto(

        @Schema(description = "Best (lowest) monthly payment among all options", example = "3200.50") BigDecimal bestMonthlyPayment,

        @Schema(description = "Best (lowest) total interest among all options", example = "200000.00") BigDecimal bestTotalInterest,

        @Schema(description = "Detailed differences between mortgage options") List<ComparisonDifferenceDto> differences) {
}