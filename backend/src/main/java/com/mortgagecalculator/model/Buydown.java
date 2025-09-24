package com.mortgagecalculator.model;

import java.math.BigDecimal;

/**
 * Represents a buydown configuration for reduced interest rates during initial years
 */
public class Buydown {
    private int duration; // Duration in years (2 or 3)
    private BigDecimal firstYearRate; // Reduced rate for first year
    private BigDecimal secondYearRate; // Reduced rate for second year (if applicable)
    private BigDecimal thirdYearRate; // Reduced rate for third year (if 3-year buydown)
    private BigDecimal permanentRate; // Standard rate after buydown period
    
    public Buydown() {}
    
    public Buydown(int duration, BigDecimal firstYearRate, BigDecimal secondYearRate, 
                   BigDecimal thirdYearRate, BigDecimal permanentRate) {
        this.duration = duration;
        this.firstYearRate = firstYearRate;
        this.secondYearRate = secondYearRate;
        this.thirdYearRate = thirdYearRate;
        this.permanentRate = permanentRate;
    }
    
    /**
     * Creates a 2-1 buydown (2% reduction first year, 1% reduction second year)
     */
    public static Buydown createTwoOneBydown(BigDecimal standardRate) {
        return new Buydown(
            2,
            standardRate.subtract(BigDecimal.valueOf(2.0)), // First year: -2%
            standardRate.subtract(BigDecimal.valueOf(1.0)), // Second year: -1%
            null, // No third year
            standardRate // Permanent rate
        );
    }
    
    /**
     * Creates a 3-2-1 buydown (3% reduction first year, 2% reduction second year, 1% reduction third year)
     */
    public static Buydown createThreeTwoOneBuydown(BigDecimal standardRate) {
        return new Buydown(
            3,
            standardRate.subtract(BigDecimal.valueOf(3.0)), // First year: -3%
            standardRate.subtract(BigDecimal.valueOf(2.0)), // Second year: -2%
            standardRate.subtract(BigDecimal.valueOf(1.0)), // Third year: -1%
            standardRate // Permanent rate
        );
    }
    
    /**
     * Gets the applicable interest rate for a given year
     */
    public BigDecimal getRateForYear(int year) {
        return switch (year) {
            case 1 -> firstYearRate;
            case 2 -> secondYearRate != null ? secondYearRate : permanentRate;
            case 3 -> thirdYearRate != null ? thirdYearRate : permanentRate;
            default -> permanentRate;
        };
    }
    
    // Getters and setters
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    public BigDecimal getFirstYearRate() {
        return firstYearRate;
    }
    
    public void setFirstYearRate(BigDecimal firstYearRate) {
        this.firstYearRate = firstYearRate;
    }
    
    public BigDecimal getSecondYearRate() {
        return secondYearRate;
    }
    
    public void setSecondYearRate(BigDecimal secondYearRate) {
        this.secondYearRate = secondYearRate;
    }
    
    public BigDecimal getThirdYearRate() {
        return thirdYearRate;
    }
    
    public void setThirdYearRate(BigDecimal thirdYearRate) {
        this.thirdYearRate = thirdYearRate;
    }
    
    public BigDecimal getPermanentRate() {
        return permanentRate;
    }
    
    public void setPermanentRate(BigDecimal permanentRate) {
        this.permanentRate = permanentRate;
    }
}