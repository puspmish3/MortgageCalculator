package com.mortgagecalculator.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Results of comparing multiple mortgage options")
public record MortgageComparisonDto(

        @Schema(description = "Calculated results for each mortgage option") List<MortgageCalculationDto> mortgages,

        @Schema(description = "Summary of comparison with best options highlighted") ComparisonSummaryDto comparisonSummary,

        @Schema(description = "Unique identifier for this comparison") String comparisonId) {
}