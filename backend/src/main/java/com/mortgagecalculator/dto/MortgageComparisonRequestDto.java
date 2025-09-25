package com.mortgagecalculator.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Request to compare multiple mortgage options")
public record MortgageComparisonRequestDto(

        @NotEmpty(message = "At least two mortgages are required for comparison") @Size(min = 2, max = 5, message = "Can compare between 2 and 5 mortgages") @Valid @Schema(description = "List of mortgage options to compare") List<MortgageInputDto> mortgages) {
}