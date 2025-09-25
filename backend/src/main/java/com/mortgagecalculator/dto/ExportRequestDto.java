package com.mortgagecalculator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to export mortgage calculation results")
public record ExportRequestDto(

        @NotBlank(message = "Calculation ID is required") @Schema(description = "ID of the calculation to export", example = "calc_123456") String calculationId,

        @Pattern(regexp = "PDF|EXCEL", message = "Format must be either PDF or EXCEL") @Schema(description = "Export format", example = "PDF", allowableValues = {
                "PDF", "EXCEL" }) String format,

        @Schema(description = "Whether to include charts in the export", example = "true") Boolean includeChart){
    public ExportRequestDto {
        if (includeChart == null) {
            includeChart = false;
        }
    }
}