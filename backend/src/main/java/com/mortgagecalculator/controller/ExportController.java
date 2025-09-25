package com.mortgagecalculator.controller;

import com.mortgagecalculator.dto.MortgageCalculationDto;
import com.mortgagecalculator.dto.MortgageComparisonDto;
import com.mortgagecalculator.service.ExcelExportService;
import com.mortgagecalculator.service.MortgageCalculationService;
import com.mortgagecalculator.service.PdfExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/export")
@Tag(name = "Export", description = "Document export operations")
public class ExportController {

    private static final Logger logger = LoggerFactory.getLogger(ExportController.class);
    private static final DateTimeFormatter FILENAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Autowired
    private MortgageCalculationService mortgageCalculationService;

    @Autowired
    private PdfExportService pdfExportService;

    @Autowired
    private ExcelExportService excelExportService;

    @Operation(summary = "Export mortgage calculation as PDF")
    @PostMapping("/mortgage/pdf")
    public ResponseEntity<ByteArrayResource> exportMortgagePdf(
            @Parameter(description = "Mortgage calculation data") @RequestBody MortgageCalculationDto calculation) {

        logger.info("Exporting mortgage calculation to PDF: {}", calculation.calculationId());

        try {
            byte[] pdfContent = pdfExportService.generateMortgagePdf(calculation, true);
            String filename = generateFilename("mortgage_calculation", "pdf");

            ByteArrayResource resource = new ByteArrayResource(pdfContent);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfContent.length)
                    .body(resource);

        } catch (Exception e) {
            logger.error("Error generating PDF for mortgage calculation: {}", calculation.calculationId(), e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    @Operation(summary = "Export mortgage calculation as Excel")
    @PostMapping("/mortgage/excel")
    public ResponseEntity<ByteArrayResource> exportMortgageExcel(
            @Parameter(description = "Mortgage calculation data") @RequestBody MortgageCalculationDto calculation) {

        logger.info("Exporting mortgage calculation to Excel: {}", calculation.calculationId());

        try {
            byte[] excelContent = excelExportService.generateMortgageExcel(calculation);
            String filename = generateFilename("mortgage_calculation", "xlsx");

            ByteArrayResource resource = new ByteArrayResource(excelContent);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(excelContent.length)
                    .body(resource);

        } catch (Exception e) {
            logger.error("Error generating Excel for mortgage calculation: {}", calculation.calculationId(), e);
            throw new RuntimeException("Failed to generate Excel", e);
        }
    }

    @Operation(summary = "Export mortgage comparison as PDF")
    @PostMapping("/comparison/pdf")
    public ResponseEntity<ByteArrayResource> exportComparisonPdf(
            @Parameter(description = "Mortgage comparison data") @RequestBody MortgageComparisonDto comparison) {

        logger.info("Exporting mortgage comparison to PDF: {}", comparison.comparisonId());

        try {
            byte[] pdfContent = pdfExportService.generateComparisonPdf(comparison, true);
            String filename = generateFilename("mortgage_comparison", "pdf");

            ByteArrayResource resource = new ByteArrayResource(pdfContent);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfContent.length)
                    .body(resource);

        } catch (Exception e) {
            logger.error("Error generating PDF for mortgage comparison: {}", comparison.comparisonId(), e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    @Operation(summary = "Export mortgage comparison as Excel")
    @PostMapping("/comparison/excel")
    public ResponseEntity<ByteArrayResource> exportComparisonExcel(
            @Parameter(description = "Mortgage comparison data") @RequestBody MortgageComparisonDto comparison) {

        logger.info("Exporting mortgage comparison to Excel: {}", comparison.comparisonId());

        try {
            byte[] excelContent = excelExportService.generateComparisonExcel(comparison);
            String filename = generateFilename("mortgage_comparison", "xlsx");

            ByteArrayResource resource = new ByteArrayResource(excelContent);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(excelContent.length)
                    .body(resource);

        } catch (Exception e) {
            logger.error("Error generating Excel for mortgage comparison: {}", comparison.comparisonId(), e);
            throw new RuntimeException("Failed to generate Excel", e);
        }
    }

    @Operation(summary = "Get mortgage calculation by ID for export")
    @GetMapping("/mortgage/{calculationId}")
    public ResponseEntity<MortgageCalculationDto> getMortgageForExport(
            @Parameter(description = "Calculation ID") @PathVariable String calculationId) {

        logger.info("Retrieving mortgage calculation for export: {}", calculationId);

        try {
            // For now, return a basic response since we don't have persistence yet
            // This endpoint would be useful when we add database integration
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving mortgage calculation: {}", calculationId, e);
            throw new RuntimeException("Failed to retrieve calculation", e);
        }
    }

    @Operation(summary = "Get mortgage comparison by ID for export")
    @GetMapping("/comparison/{comparisonId}")
    public ResponseEntity<MortgageComparisonDto> getComparisonForExport(
            @Parameter(description = "Comparison ID") @PathVariable String comparisonId) {

        logger.info("Retrieving mortgage comparison for export: {}", comparisonId);

        try {
            // For now, return a basic response since we don't have persistence yet
            // This endpoint would be useful when we add database integration
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving mortgage comparison: {}", comparisonId, e);
            throw new RuntimeException("Failed to retrieve comparison", e);
        }
    }

    private String generateFilename(String prefix, String extension) {
        String timestamp = LocalDateTime.now().format(FILENAME_FORMATTER);
        return String.format("%s_%s.%s", prefix, timestamp, extension);
    }
}