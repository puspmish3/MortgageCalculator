package com.mortgagecalculator.controller;

import com.mortgagecalculator.dto.*;
import com.mortgagecalculator.service.MortgageCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mortgage")
@Tag(name = "Mortgage Calculator", description = "APIs for calculating mortgage payments and amortization schedules")
public class MortgageController {

    private static final Logger logger = LoggerFactory.getLogger(MortgageController.class);

    private final MortgageCalculationService calculationService;

    @Autowired
    public MortgageController(MortgageCalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @Operation(
        summary = "Calculate mortgage payment and amortization schedule",
        description = "Calculates monthly payment, total interest, and generates complete amortization schedule for a mortgage"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Calculation completed successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MortgageCalculationDto.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid input parameters",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/calculate")
    public ResponseEntity<MortgageCalculationDto> calculateMortgage(
        @Parameter(description = "Mortgage calculation input parameters", required = true)
        @Valid @RequestBody MortgageInputDto input
    ) {
        try {
            logger.info("Received mortgage calculation request for loan amount: {}", input.loanAmount());
            
            MortgageCalculationDto result = calculationService.calculateMortgage(input);
            
            logger.info("Mortgage calculation completed successfully with ID: {}", result.calculationId());
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid input for mortgage calculation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error calculating mortgage", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
        summary = "Compare multiple mortgage options",
        description = "Compares 2-5 different mortgage options side by side and highlights the best choices"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Comparison completed successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MortgageComparisonDto.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid input parameters or insufficient mortgage options",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/compare")
    public ResponseEntity<MortgageComparisonDto> compareMortgages(
        @Parameter(description = "List of mortgage options to compare (2-5 mortgages)", required = true)
        @Valid @RequestBody MortgageComparisonRequestDto request
    ) {
        try {
            logger.info("Received mortgage comparison request for {} options", request.mortgages().size());
            
            MortgageComparisonDto result = calculationService.compareMortgages(request);
            
            logger.info("Mortgage comparison completed successfully with ID: {}", result.comparisonId());
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid input for mortgage comparison: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error comparing mortgages", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
        summary = "Health check endpoint",
        description = "Returns the health status of the mortgage calculation service"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Service is healthy",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        logger.debug("Health check requested");
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Mortgage Calculator API",
            "timestamp", java.time.Instant.now().toString()
        ));
    }

    @Operation(
        summary = "Get supported mortgage types",
        description = "Returns list of all supported mortgage types"
    )
    @GetMapping("/types")
    public ResponseEntity<Map<String, Object>> getMortgageTypes() {
        return ResponseEntity.ok(Map.of(
            "mortgageTypes", com.mortgagecalculator.model.MortgageType.values(),
            "paymentFrequencies", com.mortgagecalculator.model.PaymentFrequency.values()
        ));
    }
}