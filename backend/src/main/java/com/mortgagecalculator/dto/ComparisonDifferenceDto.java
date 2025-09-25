package com.mortgagecalculator.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Difference between two mortgage options for a specific metric")
public record ComparisonDifferenceDto(

        @Schema(description = "Name of the metric being compared", example = "Monthly Payment") String metric,

        @Schema(description = "Value for first mortgage option", example = "3513.80") BigDecimal mortgage1,

        @Schema(description = "Value for second mortgage option", example = "3200.50") BigDecimal mortgage2,

        @Schema(description = "Absolute difference between the two", example = "313.30") BigDecimal difference,

        @Schema(description = "Percentage difference", example = "9.78") BigDecimal percentageDifference) {
}