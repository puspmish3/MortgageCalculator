package com.mortgagecalculator.service;

import com.mortgagecalculator.dto.*;
import com.mortgagecalculator.model.MortgageType;
import com.mortgagecalculator.model.PaymentFrequency;
import com.mortgagecalculator.model.BuydownType;
import com.mortgagecalculator.model.Buydown;
import com.mortgagecalculator.enums.AdditionalPaymentFrequency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MortgageCalculationService {

    private static final Logger logger = LoggerFactory.getLogger(MortgageCalculationService.class);
    private static final MathContext PRECISION = new MathContext(10, RoundingMode.HALF_UP);
    private static final int SCALE = 2;

    /**
     * Calculate mortgage payment and generate amortization schedule
     */
    public MortgageCalculationDto calculateMortgage(MortgageInputDto input) {
        logger.info("Calculating mortgage for loan amount: {}, rate: {}%, term: {} years, buydown: {}", 
                   input.loanAmount(), input.interestRate(), input.loanTermYears(), input.buydownType());

        // Validate input
        input.validate();

        // Create buydown configuration if applicable
        Buydown buydown = createBuydown(input);

        // For buydown mortgages, we need to use the permanent rate for monthly payment calculation
        // The actual payments will vary during the buydown period
        BigDecimal calculationRate = buydown != null ? buydown.getPermanentRate() : input.interestRate();
        MortgageInputDto calculationInput = new MortgageInputDto(
            input.loanAmount(),
            calculationRate,
            input.loanTermYears(),
            input.downPayment(),
            input.propertyValue(),
            input.mortgageType(),
            input.paymentFrequency(),
            input.buydownType(),
            BigDecimal.ZERO, // Don't use additional principal for base payment calculation
            input.additionalPaymentFrequency()
        );

        // Calculate basic payment information using permanent rate
        BigDecimal baseMonthlyPayment = calculateMonthlyPayment(calculationInput);
        int totalPayments = input.loanTermYears() * input.paymentFrequency().getPaymentsPerYear();
        
        // Generate amortization schedule (this will handle variable rates for buydown)
        List<AmortizationEntryDto> schedule = generateAmortizationSchedule(input, baseMonthlyPayment, buydown);
        
        // Calculate totals from actual schedule
        BigDecimal totalInterest = schedule.stream()
            .map(AmortizationEntryDto::interestPayment)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmountPaid = input.loanAmount().add(totalInterest);

        // For display purposes, use the first year's payment as "monthly payment"
        BigDecimal displayMonthlyPayment = schedule.isEmpty() ? baseMonthlyPayment : schedule.get(0).totalPayment();

        // Create summary
        MortgageSummaryDto summary = new MortgageSummaryDto(
            input.loanAmount(),
            totalInterest,
            totalAmountPaid,
            displayMonthlyPayment,
            input.interestRate(),
            input.loanTermYears(),
            input.paymentFrequency()
        );

        String calculationId = generateCalculationId();

        return new MortgageCalculationDto(
            displayMonthlyPayment,
            totalInterest,
            totalPayments,
            schedule,
            summary,
            calculationId
        );
    }

    /**
     * Compare multiple mortgage options
     */
    public MortgageComparisonDto compareMortgages(MortgageComparisonRequestDto request) {
        logger.info("Comparing {} mortgage options", request.mortgages().size());

        List<MortgageCalculationDto> calculations = request.mortgages().stream()
            .map(this::calculateMortgage)
            .toList();

        ComparisonSummaryDto summary = generateComparisonSummary(calculations);
        String comparisonId = generateCalculationId();

        return new MortgageComparisonDto(calculations, summary, comparisonId);
    }

    /**
     * Create buydown configuration based on input type
     */
    private Buydown createBuydown(MortgageInputDto input) {
        if (input.buydownType() == null || input.buydownType() == BuydownType.NONE) {
            return null;
        }
        
        return switch (input.buydownType()) {
            case TWO_ONE -> Buydown.createTwoOneBydown(input.interestRate());
            case THREE_TWO_ONE -> Buydown.createThreeTwoOneBuydown(input.interestRate());
            default -> null;
        };
    }

    /**
     * Calculate monthly payment using standard mortgage formula
     */
    private BigDecimal calculateMonthlyPayment(MortgageInputDto input) {
        BigDecimal principal = input.loanAmount();
        BigDecimal annualRate = input.interestRate().divide(BigDecimal.valueOf(100), PRECISION);
        int paymentsPerYear = input.paymentFrequency().getPaymentsPerYear();
        BigDecimal periodicRate = annualRate.divide(BigDecimal.valueOf(paymentsPerYear), PRECISION);
        int totalPayments = input.loanTermYears() * paymentsPerYear;

        // Handle special case for interest-only mortgages
        if (input.mortgageType() == MortgageType.INTEREST_ONLY) {
            return principal.multiply(periodicRate).setScale(SCALE, RoundingMode.HALF_UP);
        }

        // Standard mortgage payment calculation: P * [r(1+r)^n] / [(1+r)^n - 1]
        if (periodicRate.compareTo(BigDecimal.ZERO) == 0) {
            // No interest case
            return principal.divide(BigDecimal.valueOf(totalPayments), SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal onePlusRate = BigDecimal.ONE.add(periodicRate);
        BigDecimal onePlusRatePowN = onePlusRate.pow(totalPayments, PRECISION);
        
        BigDecimal numerator = periodicRate.multiply(onePlusRatePowN, PRECISION);
        BigDecimal denominator = onePlusRatePowN.subtract(BigDecimal.ONE, PRECISION);
        
        BigDecimal paymentMultiplier = numerator.divide(denominator, PRECISION);
        
        return principal.multiply(paymentMultiplier, PRECISION).setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Generate complete amortization schedule
     */
    private List<AmortizationEntryDto> generateAmortizationSchedule(MortgageInputDto input, BigDecimal baseMonthlyPayment) {
        return generateAmortizationSchedule(input, baseMonthlyPayment, null);
    }

    /**
     * Generate complete amortization schedule with buydown support
     */
    private List<AmortizationEntryDto> generateAmortizationSchedule(MortgageInputDto input, BigDecimal baseMonthlyPayment, Buydown buydown) {
        List<AmortizationEntryDto> schedule = new ArrayList<>();
        
        BigDecimal remainingBalance = input.loanAmount();
        int totalPayments = input.loanTermYears() * input.paymentFrequency().getPaymentsPerYear();
        int paymentsPerYear = input.paymentFrequency().getPaymentsPerYear();
        
        LocalDate currentDate = LocalDate.now();
        int daysIncrement = 365 / paymentsPerYear;
        
        // Additional principal payment configuration
        BigDecimal additionalPrincipalAmount = input.additionalPrincipalPayment() != null ? 
            input.additionalPrincipalPayment() : BigDecimal.ZERO;
        AdditionalPaymentFrequency additionalFrequency = input.additionalPaymentFrequency() != null ?
            input.additionalPaymentFrequency() : AdditionalPaymentFrequency.MONTHLY;

        // Calculate payment interval for additional payments
        int additionalPaymentInterval = additionalFrequency.isOneTime() ? 0 : 
            additionalFrequency.getPaymentInterval(input.paymentFrequency());
        
        // Track when one-time payment has been applied
        boolean oneTimePaymentApplied = false;

        // Calculate interest savings for cumulative tracking
        BigDecimal cumulativeInterestSaved = BigDecimal.ZERO;

        for (int paymentNumber = 1; paymentNumber <= totalPayments && remainingBalance.compareTo(BigDecimal.ZERO) > 0; paymentNumber++) {
            // Determine current year for buydown calculation
            int currentYear = ((paymentNumber - 1) / paymentsPerYear) + 1;
            
            // Get applicable interest rate for this payment
            BigDecimal currentAnnualRate;
            if (buydown != null && currentYear <= buydown.getDuration()) {
                currentAnnualRate = buydown.getRateForYear(currentYear);
            } else {
                currentAnnualRate = input.interestRate();
            }
            
            BigDecimal periodicRate = currentAnnualRate.divide(BigDecimal.valueOf(100), PRECISION)
                .divide(BigDecimal.valueOf(paymentsPerYear), PRECISION);
            
            // Calculate interest for this period using current rate
            BigDecimal interestPayment = remainingBalance.multiply(periodicRate, PRECISION).setScale(SCALE, RoundingMode.HALF_UP);
            
            // Calculate payment amounts
            BigDecimal principalPayment;
            BigDecimal regularPayment;
            BigDecimal actualAdditionalPrincipal = BigDecimal.ZERO;
            
            if (input.mortgageType() == MortgageType.INTEREST_ONLY) {
                // For interest-only mortgages, principal payment is 0 until the end
                principalPayment = BigDecimal.ZERO;
                regularPayment = interestPayment;
                
                // On the last payment, add remaining balance
                if (paymentNumber == totalPayments) {
                    principalPayment = remainingBalance;
                    regularPayment = interestPayment.add(principalPayment);
                }
            } else {
                // For buydown mortgages, calculate payment based on current rate
                if (buydown != null && currentYear <= buydown.getDuration()) {
                    // During buydown period, calculate payment with reduced rate
                    MortgageInputDto tempInput = new MortgageInputDto(
                        remainingBalance,
                        currentAnnualRate,
                        input.loanTermYears() - (currentYear - 1),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        input.mortgageType(),
                        input.paymentFrequency(),
                        BuydownType.NONE,
                        BigDecimal.ZERO, // No additional principal for temp calculation
                        input.additionalPaymentFrequency()
                    );
                    BigDecimal buydownPayment = calculateMonthlyPayment(tempInput);
                    principalPayment = buydownPayment.subtract(interestPayment);
                } else {
                    // Use base payment calculation
                    principalPayment = baseMonthlyPayment.subtract(interestPayment);
                }
                
                regularPayment = interestPayment.add(principalPayment);
                
                // Add additional principal payment based on frequency
                if (additionalPrincipalAmount.compareTo(BigDecimal.ZERO) > 0) {
                    boolean shouldApplyAdditionalPayment = false;
                    
                    if (additionalFrequency.isOneTime()) {
                        // Apply one-time payment on first payment if not yet applied
                        shouldApplyAdditionalPayment = !oneTimePaymentApplied;
                        if (shouldApplyAdditionalPayment) {
                            oneTimePaymentApplied = true;
                        }
                    } else {
                        // Apply additional payment based on frequency interval
                        shouldApplyAdditionalPayment = (paymentNumber % additionalPaymentInterval == 0);
                    }
                    
                    if (shouldApplyAdditionalPayment) {
                        actualAdditionalPrincipal = additionalPrincipalAmount;
                        
                        // Don't pay more than remaining balance
                        BigDecimal totalPrincipal = principalPayment.add(actualAdditionalPrincipal);
                        if (totalPrincipal.compareTo(remainingBalance) > 0) {
                            actualAdditionalPrincipal = remainingBalance.subtract(principalPayment);
                            if (actualAdditionalPrincipal.compareTo(BigDecimal.ZERO) < 0) {
                                actualAdditionalPrincipal = BigDecimal.ZERO;
                            }
                        }
                    }
                }
                
                // Ensure we don't pay more principal than remaining balance
                BigDecimal totalPrincipalPayment = principalPayment.add(actualAdditionalPrincipal);
                if (totalPrincipalPayment.compareTo(remainingBalance) > 0) {
                    totalPrincipalPayment = remainingBalance;
                    // Adjust principal payment and additional payment proportionally
                    if (principalPayment.add(actualAdditionalPrincipal).compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal ratio = totalPrincipalPayment.divide(principalPayment.add(actualAdditionalPrincipal), PRECISION);
                        principalPayment = principalPayment.multiply(ratio).setScale(SCALE, RoundingMode.HALF_UP);
                        actualAdditionalPrincipal = totalPrincipalPayment.subtract(principalPayment);
                    }
                }
            }

            // Calculate total payment including additional principal
            BigDecimal totalPayment = regularPayment.add(actualAdditionalPrincipal);

            // Update remaining balance
            remainingBalance = remainingBalance.subtract(principalPayment).subtract(actualAdditionalPrincipal);
            
            // Ensure balance doesn't go negative due to rounding
            if (remainingBalance.compareTo(BigDecimal.ZERO) < 0) {
                remainingBalance = BigDecimal.ZERO;
            }

            // Calculate interest saved (approximate - the interest we won't pay on the additional principal)
            BigDecimal interestSavedThisPayment = BigDecimal.ZERO;
            if (actualAdditionalPrincipal.compareTo(BigDecimal.ZERO) > 0) {
                // Approximate interest saved over remaining term
                int remainingPayments = totalPayments - paymentNumber;
                if (remainingPayments > 0) {
                    interestSavedThisPayment = actualAdditionalPrincipal
                        .multiply(periodicRate)
                        .multiply(BigDecimal.valueOf(remainingPayments))
                        .setScale(SCALE, RoundingMode.HALF_UP);
                }
                cumulativeInterestSaved = cumulativeInterestSaved.add(interestSavedThisPayment);
            }

            // Calculate payment date
            LocalDate paymentDate = currentDate.plusDays((long) daysIncrement * (paymentNumber - 1));

            schedule.add(new AmortizationEntryDto(
                paymentNumber,
                paymentDate,
                principalPayment,
                interestPayment,
                actualAdditionalPrincipal,
                regularPayment,
                totalPayment,
                remainingBalance,
                currentAnnualRate,
                cumulativeInterestSaved
            ));

            // Break if balance is paid off
            if (remainingBalance.compareTo(BigDecimal.ZERO) == 0) {
                break;
            }
        }

        return schedule;
    }

    /**
     * Generate comparison summary highlighting best options
     */
    private ComparisonSummaryDto generateComparisonSummary(List<MortgageCalculationDto> calculations) {
        BigDecimal bestMonthlyPayment = calculations.stream()
            .map(MortgageCalculationDto::monthlyPayment)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);

        BigDecimal bestTotalInterest = calculations.stream()
            .map(MortgageCalculationDto::totalInterest)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);

        // Generate detailed differences for the first two mortgages as an example
        List<ComparisonDifferenceDto> differences = new ArrayList<>();
        if (calculations.size() >= 2) {
            MortgageCalculationDto first = calculations.get(0);
            MortgageCalculationDto second = calculations.get(1);

            differences.add(createDifference("Monthly Payment", first.monthlyPayment(), second.monthlyPayment()));
            differences.add(createDifference("Total Interest", first.totalInterest(), second.totalInterest()));
            differences.add(createDifference("Total Amount Paid", first.summary().totalAmountPaid(), second.summary().totalAmountPaid()));
        }

        return new ComparisonSummaryDto(bestMonthlyPayment, bestTotalInterest, differences);
    }

    /**
     * Create a comparison difference entry
     */
    private ComparisonDifferenceDto createDifference(String metric, BigDecimal value1, BigDecimal value2) {
        BigDecimal difference = value1.subtract(value2).abs();
        BigDecimal percentageDifference = BigDecimal.ZERO;
        
        if (value2.compareTo(BigDecimal.ZERO) != 0) {
            percentageDifference = difference.divide(value2, PRECISION)
                .multiply(BigDecimal.valueOf(100))
                .setScale(SCALE, RoundingMode.HALF_UP);
        }

        return new ComparisonDifferenceDto(metric, value1, value2, difference, percentageDifference);
    }

    /**
     * Generate unique calculation ID
     */
    private String generateCalculationId() {
        return "calc_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}