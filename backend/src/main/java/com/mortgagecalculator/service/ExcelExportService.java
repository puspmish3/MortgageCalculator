package com.mortgagecalculator.service;

import com.mortgagecalculator.dto.AmortizationEntryDto;
import com.mortgagecalculator.dto.MortgageCalculationDto;
import com.mortgagecalculator.dto.MortgageComparisonDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelExportService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    /**
     * Generate Excel for single mortgage calculation
     */
    public byte[] generateMortgageExcel(MortgageCalculationDto calculation) throws IOException {
        logger.info("Generating Excel for mortgage calculation: {}", calculation.calculationId());

        try (Workbook workbook = new XSSFWorkbook()) {
            // Create summary sheet
            createSummarySheet(workbook, calculation);

            // Create amortization schedule sheet
            createAmortizationSheet(workbook, calculation.amortizationSchedule());

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            logger.info("Excel generated successfully for calculation: {}", calculation.calculationId());
            return outputStream.toByteArray();
        }
    }

    /**
     * Generate Excel for mortgage comparison
     */
    public byte[] generateComparisonExcel(MortgageComparisonDto comparison) throws IOException {
        logger.info("Generating comparison Excel: {}", comparison.comparisonId());

        try (Workbook workbook = new XSSFWorkbook()) {
            // Create comprehensive comparison summary sheet
            createComprehensiveComparisonSummarySheet(workbook, comparison);

            // Create visual comparison data sheet
            createVisualComparisonSheet(workbook, comparison);

            // Create individual detailed sheets for each mortgage with full amortization
            for (int i = 0; i < comparison.mortgages().size(); i++) {
                MortgageCalculationDto mortgage = comparison.mortgages().get(i);
                String sheetName = "Option " + (i + 1) + " Details";
                createDetailedMortgageSheet(workbook, mortgage, sheetName);
            }

            // Create complete amortization schedules for all options
            for (int i = 0; i < comparison.mortgages().size(); i++) {
                MortgageCalculationDto mortgage = comparison.mortgages().get(i);
                String amortSheetName = "Option " + (i + 1) + " Schedule";
                createCompleteAmortizationSheet(workbook, mortgage.amortizationSchedule(), amortSheetName);
            }

            // Create yearly comparison sheet
            createYearlyComparisonSheet(workbook, comparison);

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            logger.info("Comparison Excel generated successfully: {}", comparison.comparisonId());
            return outputStream.toByteArray();
        }
    }

    private void createSummarySheet(Workbook workbook, MortgageCalculationDto calculation) {
        Sheet sheet = workbook.createSheet("Summary");

        // Create styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle percentStyle = createPercentStyle(workbook);

        int rowNum = 0;

        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Mortgage Calculation Summary");
        titleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

        // Generation date
        rowNum++;
        Row dateRow = sheet.createRow(rowNum++);
        dateRow.createCell(0).setCellValue("Generated on:");
        dateRow.createCell(1).setCellValue(LocalDate.now().format(DATE_FORMATTER));

        // Empty row
        rowNum++;

        // Loan details
        addSummaryRow(sheet, rowNum++, "Loan Amount:", calculation.summary().loanAmount(), currencyStyle);
        addSummaryRow(sheet, rowNum++, "Interest Rate:", calculation.summary().interestRate(), percentStyle);
        addSummaryRow(sheet, rowNum++, "Loan Term:", calculation.summary().loanTermYears() + " years", null);
        addSummaryRow(sheet, rowNum++, "Payment Frequency:", calculation.summary().paymentFrequency().getDisplayName(),
                null);

        // Empty row
        rowNum++;

        // Payment details
        addSummaryRow(sheet, rowNum++, "Monthly Payment:", calculation.monthlyPayment(), currencyStyle);
        addSummaryRow(sheet, rowNum++, "Total Interest:", calculation.totalInterest(), currencyStyle);
        addSummaryRow(sheet, rowNum++, "Total Amount Paid:", calculation.summary().totalAmountPaid(), currencyStyle);
        addSummaryRow(sheet, rowNum++, "Total Payments:", calculation.totalPayments(), null);

        // Auto-size columns
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createAmortizationSheet(Workbook workbook, List<AmortizationEntryDto> schedule) {
        Sheet sheet = workbook.createSheet("Amortization Schedule");

        // Create styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);

        int rowNum = 0;

        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = { "Payment #", "Date", "Principal", "Interest", "Total Payment", "Remaining Balance" };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        for (AmortizationEntryDto entry : schedule) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(entry.paymentNumber());

            Cell dateCell = row.createCell(1);
            dateCell.setCellValue(entry.paymentDate());
            dateCell.setCellStyle(dateStyle);

            Cell principalCell = row.createCell(2);
            principalCell.setCellValue(entry.principalPayment().doubleValue());
            principalCell.setCellStyle(currencyStyle);

            Cell interestCell = row.createCell(3);
            interestCell.setCellValue(entry.interestPayment().doubleValue());
            interestCell.setCellStyle(currencyStyle);

            Cell totalCell = row.createCell(4);
            totalCell.setCellValue(entry.totalPayment().doubleValue());
            totalCell.setCellStyle(currencyStyle);

            Cell balanceCell = row.createCell(5);
            balanceCell.setCellValue(entry.remainingBalance().doubleValue());
            balanceCell.setCellStyle(currencyStyle);
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Freeze the header row
        sheet.createFreezePane(0, 1);
    }

    private void createComparisonSummarySheet(Workbook workbook, MortgageComparisonDto comparison) {
        Sheet sheet = workbook.createSheet("Comparison Summary");

        // Create styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);

        int rowNum = 0;

        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Mortgage Comparison Summary");
        titleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, comparison.mortgages().size()));

        // Generation date
        rowNum++;
        Row dateRow = sheet.createRow(rowNum++);
        dateRow.createCell(0).setCellValue("Generated on:");
        dateRow.createCell(1).setCellValue(LocalDate.now().format(DATE_FORMATTER));

        // Empty row
        rowNum++;

        // Best options
        addSummaryRow(sheet, rowNum++, "Best Monthly Payment:", comparison.comparisonSummary().bestMonthlyPayment(),
                currencyStyle);
        addSummaryRow(sheet, rowNum++, "Lowest Total Interest:", comparison.comparisonSummary().bestTotalInterest(),
                currencyStyle);

        // Empty row
        rowNum++;

        // Comparison table
        Row compHeaderRow = sheet.createRow(rowNum++);
        compHeaderRow.createCell(0).setCellValue("Metric");
        compHeaderRow.getCell(0).setCellStyle(headerStyle);

        for (int i = 0; i < comparison.mortgages().size(); i++) {
            Cell cell = compHeaderRow.createCell(i + 1);
            cell.setCellValue("Option " + (i + 1));
            cell.setCellStyle(headerStyle);
        }

        // Monthly Payment row
        Row monthlyRow = sheet.createRow(rowNum++);
        monthlyRow.createCell(0).setCellValue("Monthly Payment");
        for (int i = 0; i < comparison.mortgages().size(); i++) {
            Cell cell = monthlyRow.createCell(i + 1);
            cell.setCellValue(comparison.mortgages().get(i).monthlyPayment().doubleValue());
            cell.setCellStyle(currencyStyle);
        }

        // Total Interest row
        Row interestRow = sheet.createRow(rowNum++);
        interestRow.createCell(0).setCellValue("Total Interest");
        for (int i = 0; i < comparison.mortgages().size(); i++) {
            Cell cell = interestRow.createCell(i + 1);
            cell.setCellValue(comparison.mortgages().get(i).totalInterest().doubleValue());
            cell.setCellStyle(currencyStyle);
        }

        // Total Amount Paid row
        Row totalRow = sheet.createRow(rowNum++);
        totalRow.createCell(0).setCellValue("Total Amount Paid");
        for (int i = 0; i < comparison.mortgages().size(); i++) {
            Cell cell = totalRow.createCell(i + 1);
            cell.setCellValue(comparison.mortgages().get(i).summary().totalAmountPaid().doubleValue());
            cell.setCellStyle(currencyStyle);
        }

        // Auto-size columns
        for (int i = 0; i <= comparison.mortgages().size(); i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createMortgageSheet(Workbook workbook, MortgageCalculationDto calculation, String sheetName) {
        // Create a simplified version for comparison sheets
        Sheet sheet = workbook.createSheet(sheetName);

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);

        int rowNum = 0;

        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(sheetName + " - Details");
        titleCell.setCellStyle(headerStyle);

        rowNum++;

        // Summary
        addSummaryRow(sheet, rowNum++, "Loan Amount:", calculation.summary().loanAmount(), currencyStyle);
        addSummaryRow(sheet, rowNum++, "Interest Rate:", calculation.summary().interestRate() + "%", null);
        addSummaryRow(sheet, rowNum++, "Loan Term:", calculation.summary().loanTermYears() + " years", null);
        addSummaryRow(sheet, rowNum++, "Monthly Payment:", calculation.monthlyPayment(), currencyStyle);
        addSummaryRow(sheet, rowNum++, "Total Interest:", calculation.totalInterest(), currencyStyle);
        addSummaryRow(sheet, rowNum++, "Total Amount Paid:", calculation.summary().totalAmountPaid(), currencyStyle);

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void addSummaryRow(Sheet sheet, int rowNum, String label, Object value, CellStyle valueStyle) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);

        Cell valueCell = row.createCell(1);
        if (value instanceof BigDecimal) {
            valueCell.setCellValue(((BigDecimal) value).doubleValue());
        } else if (value instanceof Number) {
            valueCell.setCellValue(((Number) value).doubleValue());
        } else {
            valueCell.setCellValue(value.toString());
        }

        if (valueStyle != null) {
            valueCell.setCellStyle(valueStyle);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("$#,##0.00"));
        return style;
    }

    private CellStyle createPercentStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("0.00%"));
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("mm/dd/yyyy"));
        return style;
    }

    private void createComprehensiveComparisonSummarySheet(Workbook workbook, MortgageComparisonDto comparison) {
        Sheet sheet = workbook.createSheet("Comprehensive Summary");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle percentStyle = createPercentStyle(workbook);

        int rowNum = 0;

        // Enhanced Title with gradient-like styling
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("🏡 Comprehensive Mortgage Comparison Report");

        CellStyle enhancedTitleStyle = createEnhancedTitleStyle(workbook);
        titleCell.setCellStyle(enhancedTitleStyle);
        sheet.addMergedRegion(
                new CellRangeAddress(rowNum - 1, rowNum - 1, 0, Math.max(5, comparison.mortgages().size())));

        rowNum++;

        // Executive Summary with enhanced styling
        Row execRow = sheet.createRow(rowNum++);
        Cell execCell = execRow.createCell(0);
        execCell.setCellValue("📊 Executive Summary");

        CellStyle summaryHeaderStyle = createSummaryHeaderStyle(workbook);
        execCell.setCellStyle(summaryHeaderStyle);

        Row summaryDescRow = sheet.createRow(rowNum++);
        summaryDescRow.createCell(0).setCellValue("Comparing " + comparison.mortgages().size() +
                " mortgage options with detailed analysis including monthly payments, total costs, and amortization schedules.");

        rowNum += 2;

        // Comprehensive comparison table
        Row compHeaderRow = sheet.createRow(rowNum++);
        compHeaderRow.createCell(0).setCellValue("Metric");
        compHeaderRow.getCell(0).setCellStyle(headerStyle);

        for (int i = 0; i < comparison.mortgages().size(); i++) {
            Cell cell = compHeaderRow.createCell(i + 1);
            cell.setCellValue("Option " + (i + 1));
            cell.setCellStyle(headerStyle);
        }

        // Add comprehensive metrics
        addComparisonMetricRow(sheet, rowNum++, "Loan Amount", comparison.mortgages(),
                m -> m.summary().loanAmount(), currencyStyle);
        addComparisonMetricRow(sheet, rowNum++, "Interest Rate", comparison.mortgages(),
                m -> m.summary().interestRate(), percentStyle);
        addComparisonMetricRow(sheet, rowNum++, "Loan Term (Years)", comparison.mortgages(),
                m -> BigDecimal.valueOf(m.summary().loanTermYears()), null);
        addComparisonMetricRow(sheet, rowNum++, "Monthly Payment", comparison.mortgages(),
                MortgageCalculationDto::monthlyPayment, currencyStyle);
        addComparisonMetricRow(sheet, rowNum++, "Total Interest", comparison.mortgages(),
                MortgageCalculationDto::totalInterest, currencyStyle);
        addComparisonMetricRow(sheet, rowNum++, "Total Amount Paid", comparison.mortgages(),
                m -> m.summary().totalAmountPaid(), currencyStyle);
        addComparisonMetricRow(sheet, rowNum++, "Total Payments", comparison.mortgages(),
                m -> BigDecimal.valueOf(m.totalPayments()), null);

        rowNum += 2;

        // Recommendations section
        Row recHeaderRow = sheet.createRow(rowNum++);
        recHeaderRow.createCell(0).setCellValue("Recommendations");
        recHeaderRow.getCell(0).setCellStyle(headerStyle);

        // Find best options
        int bestMonthlyIndex = findBestMonthlyPaymentIndex(comparison.mortgages());
        int bestTotalCostIndex = findBestTotalCostIndex(comparison.mortgages());

        Row bestMonthlyRow = sheet.createRow(rowNum++);
        bestMonthlyRow.createCell(0).setCellValue("Best Monthly Payment:");
        bestMonthlyRow.createCell(1).setCellValue("Option " + (bestMonthlyIndex + 1));

        Row bestCostRow = sheet.createRow(rowNum++);
        bestCostRow.createCell(0).setCellValue("Lowest Total Cost:");
        bestCostRow.createCell(1).setCellValue("Option " + (bestTotalCostIndex + 1));

        // Auto-size columns
        for (int i = 0; i <= comparison.mortgages().size(); i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createVisualComparisonSheet(Workbook workbook, MortgageComparisonDto comparison) {
        Sheet sheet = workbook.createSheet("Visual Comparison Data");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);

        int rowNum = 0;

        // Title
        Row titleRow = sheet.createRow(rowNum++);
        titleRow.createCell(0).setCellValue("Visual Comparison Data");
        titleRow.getCell(0).setCellStyle(headerStyle);

        rowNum++;

        // Cost breakdown section
        Row costBreakdownHeader = sheet.createRow(rowNum++);
        costBreakdownHeader.createCell(0).setCellValue("Cost Breakdown Analysis");
        costBreakdownHeader.getCell(0).setCellStyle(headerStyle);

        Row costHeaderRow = sheet.createRow(rowNum++);
        costHeaderRow.createCell(0).setCellValue("Option");
        costHeaderRow.createCell(1).setCellValue("Principal Amount");
        costHeaderRow.createCell(2).setCellValue("Interest Amount");
        costHeaderRow.createCell(3).setCellValue("Total Cost");
        costHeaderRow.createCell(4).setCellValue("Interest %");

        for (Cell cell : costHeaderRow) {
            cell.setCellStyle(headerStyle);
        }

        for (int i = 0; i < comparison.mortgages().size(); i++) {
            MortgageCalculationDto mortgage = comparison.mortgages().get(i);
            Row dataRow = sheet.createRow(rowNum++);

            dataRow.createCell(0).setCellValue("Option " + (i + 1));

            Cell principalCell = dataRow.createCell(1);
            principalCell.setCellValue(mortgage.summary().loanAmount().doubleValue());
            principalCell.setCellStyle(currencyStyle);

            Cell interestCell = dataRow.createCell(2);
            interestCell.setCellValue(mortgage.totalInterest().doubleValue());
            interestCell.setCellStyle(currencyStyle);

            Cell totalCell = dataRow.createCell(3);
            totalCell.setCellValue(mortgage.summary().totalAmountPaid().doubleValue());
            totalCell.setCellStyle(currencyStyle);

            // Calculate interest percentage
            BigDecimal interestPercentage = mortgage.totalInterest()
                    .divide(mortgage.summary().totalAmountPaid(), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            dataRow.createCell(4).setCellValue(interestPercentage.doubleValue() + "%");
        }

        rowNum += 2;

        // Savings analysis
        Row savingsHeader = sheet.createRow(rowNum++);
        savingsHeader.createCell(0).setCellValue("Savings Analysis");
        savingsHeader.getCell(0).setCellStyle(headerStyle);

        BigDecimal lowestCost = comparison.mortgages().stream()
                .map(m -> m.summary().totalAmountPaid())
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal highestCost = comparison.mortgages().stream()
                .map(m -> m.summary().totalAmountPaid())
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal maxSavings = highestCost.subtract(lowestCost);

        Row savingsRow = sheet.createRow(rowNum++);
        savingsRow.createCell(0).setCellValue("Maximum Potential Savings:");
        Cell savingsCell = savingsRow.createCell(1);
        savingsCell.setCellValue(maxSavings.doubleValue());
        savingsCell.setCellStyle(currencyStyle);

        // Auto-size columns
        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }

        // Add colorful charts to the sheet
        addColorfulChartsToSheet(sheet, comparison, rowNum + 3);
    }

    private void addColorfulChartsToSheet(Sheet sheet, MortgageComparisonDto comparison, int startRow) {
        Workbook workbook = sheet.getWorkbook();

        // Create chart data section
        int chartDataRow = startRow;

        // Chart Title
        Row titleRow = sheet.createRow(chartDataRow++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Visual Comparison Charts Data");

        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
        titleStyle.setFont(titleFont);
        titleStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        titleCell.setCellStyle(titleStyle);

        chartDataRow++;

        // Headers for chart data
        Row headerRow = sheet.createRow(chartDataRow++);
        CellStyle headerStyle = createColoredHeaderStyle(workbook);

        String[] headers = { "Option", "Monthly Payment", "Total Cost", "Total Interest", "Principal" };
        for (int i = 0; i < headers.length; i++) {
            Cell headerCell = headerRow.createCell(i);
            headerCell.setCellValue(headers[i]);
            headerCell.setCellStyle(headerStyle);
        }

        // Data rows with alternating colors
        CellStyle[] dataCellStyles = createAlternatingRowStyles(workbook);
        CellStyle currencyStyle = createColoredCurrencyStyle(workbook);

        for (int i = 0; i < comparison.mortgages().size(); i++) {
            MortgageCalculationDto mortgage = comparison.mortgages().get(i);
            Row dataRow = sheet.createRow(chartDataRow++);

            // Option name
            Cell optionCell = dataRow.createCell(0);
            optionCell.setCellValue("Option " + (i + 1));
            optionCell.setCellStyle(dataCellStyles[i % 2]);

            // Monthly Payment with currency styling
            Cell monthlyPaymentCell = dataRow.createCell(1);
            monthlyPaymentCell.setCellValue(mortgage.monthlyPayment().doubleValue());
            monthlyPaymentCell.setCellStyle(currencyStyle);

            // Total Cost
            Cell totalCostCell = dataRow.createCell(2);
            totalCostCell.setCellValue(mortgage.summary().totalAmountPaid().doubleValue());
            totalCostCell.setCellStyle(currencyStyle);

            // Total Interest
            Cell totalInterestCell = dataRow.createCell(3);
            totalInterestCell.setCellValue(mortgage.totalInterest().doubleValue());
            totalInterestCell.setCellStyle(currencyStyle);

            // Principal
            Cell principalCell = dataRow.createCell(4);
            principalCell.setCellValue(mortgage.summary().loanAmount().doubleValue());
            principalCell.setCellStyle(currencyStyle);
        }

        // Create visual data bars in Excel (using conditional formatting effect)
        addVisualDataBars(sheet, chartDataRow - comparison.mortgages().size(), comparison.mortgages().size());

        // Auto-size the chart data columns
        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private CellStyle createColoredHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle[] createAlternatingRowStyles(Workbook workbook) {
        CellStyle[] styles = new CellStyle[2];

        // Light gray style
        styles[0] = workbook.createCellStyle();
        styles[0].setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        styles[0].setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles[0].setAlignment(HorizontalAlignment.CENTER);
        styles[0].setBorderBottom(BorderStyle.THIN);
        styles[0].setBorderTop(BorderStyle.THIN);
        styles[0].setBorderRight(BorderStyle.THIN);
        styles[0].setBorderLeft(BorderStyle.THIN);

        // White style
        styles[1] = workbook.createCellStyle();
        styles[1].setFillForegroundColor(IndexedColors.WHITE.getIndex());
        styles[1].setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles[1].setAlignment(HorizontalAlignment.CENTER);
        styles[1].setBorderBottom(BorderStyle.THIN);
        styles[1].setBorderTop(BorderStyle.THIN);
        styles[1].setBorderRight(BorderStyle.THIN);
        styles[1].setBorderLeft(BorderStyle.THIN);

        return styles;
    }

    private CellStyle createColoredCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));
        Font font = workbook.createFont();
        font.setColor(IndexedColors.DARK_GREEN.getIndex());
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private void addVisualDataBars(Sheet sheet, int startRow, int numRows) {
        // Add colored backgrounds to simulate visual bars
        Workbook workbook = sheet.getWorkbook();

        // Colors for different options
        short[] colors = {
                IndexedColors.LIGHT_BLUE.getIndex(),
                IndexedColors.LIGHT_GREEN.getIndex(),
                IndexedColors.LIGHT_ORANGE.getIndex(),
                IndexedColors.ROSE.getIndex(),
                IndexedColors.LAVENDER.getIndex(),
                IndexedColors.PALE_BLUE.getIndex()
        };

        for (int i = 0; i < numRows && i < colors.length; i++) {
            Row row = sheet.getRow(startRow + i);
            if (row != null) {
                CellStyle barStyle = workbook.createCellStyle();
                barStyle.setFillForegroundColor(colors[i]);
                barStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                barStyle.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));
                barStyle.setAlignment(HorizontalAlignment.RIGHT);
                barStyle.setBorderBottom(BorderStyle.MEDIUM);
                barStyle.setBorderTop(BorderStyle.MEDIUM);
                barStyle.setBorderRight(BorderStyle.MEDIUM);
                barStyle.setBorderLeft(BorderStyle.MEDIUM);

                Font barFont = workbook.createFont();
                barFont.setBold(true);
                barFont.setColor(IndexedColors.BLACK.getIndex());
                barStyle.setFont(barFont);

                // Apply colorful style to monetary columns (1-4)
                for (int col = 1; col <= 4; col++) {
                    Cell cell = row.getCell(col);
                    if (cell != null) {
                        cell.setCellStyle(barStyle);
                    }
                }
            }
        }
    }

    private void createDetailedMortgageSheet(Workbook workbook, MortgageCalculationDto calculation, String sheetName) {
        Sheet sheet = workbook.createSheet(sheetName);

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle percentStyle = createPercentStyle(workbook);

        int rowNum = 0;

        // Title
        Row titleRow = sheet.createRow(rowNum++);
        titleRow.createCell(0).setCellValue(sheetName);
        titleRow.getCell(0).setCellStyle(headerStyle);

        rowNum++;

        // Detailed summary
        addSummaryRow(sheet, rowNum++, "Loan Amount:", calculation.summary().loanAmount(), currencyStyle);
        addSummaryRow(sheet, rowNum++, "Interest Rate:", calculation.summary().interestRate(), percentStyle);
        addSummaryRow(sheet, rowNum++, "Loan Term:", calculation.summary().loanTermYears() + " years", null);
        addSummaryRow(sheet, rowNum++, "Payment Frequency:", calculation.summary().paymentFrequency().getDisplayName(),
                null);
        addSummaryRow(sheet, rowNum++, "Monthly Payment:", calculation.monthlyPayment(), currencyStyle);
        addSummaryRow(sheet, rowNum++, "Total Interest:", calculation.totalInterest(), currencyStyle);
        addSummaryRow(sheet, rowNum++, "Total Amount Paid:", calculation.summary().totalAmountPaid(), currencyStyle);
        addSummaryRow(sheet, rowNum++, "Total Payments:", calculation.totalPayments(), null);

        rowNum += 2;

        // Add yearly breakdown
        createYearlyBreakdown(sheet, rowNum, calculation.amortizationSchedule(), headerStyle, currencyStyle);

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createCompleteAmortizationSheet(Workbook workbook, List<AmortizationEntryDto> schedule,
            String sheetName) {
        Sheet sheet = workbook.createSheet(sheetName);

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);
        CellStyle percentStyle = createPercentStyle(workbook);

        int rowNum = 0;

        // Headers - including interest rate column
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = { "Payment #", "Date", "Principal", "Interest", "Total Payment", "Remaining Balance",
                "Interest Rate" };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows - all payments
        for (AmortizationEntryDto entry : schedule) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(entry.paymentNumber());

            Cell dateCell = row.createCell(1);
            dateCell.setCellValue(entry.paymentDate());
            dateCell.setCellStyle(dateStyle);

            Cell principalCell = row.createCell(2);
            principalCell.setCellValue(entry.principalPayment().doubleValue());
            principalCell.setCellStyle(currencyStyle);

            Cell interestCell = row.createCell(3);
            interestCell.setCellValue(entry.interestPayment().doubleValue());
            interestCell.setCellStyle(currencyStyle);

            Cell totalCell = row.createCell(4);
            totalCell.setCellValue(entry.totalPayment().doubleValue());
            totalCell.setCellStyle(currencyStyle);

            Cell balanceCell = row.createCell(5);
            balanceCell.setCellValue(entry.remainingBalance().doubleValue());
            balanceCell.setCellStyle(currencyStyle);

            Cell rateCell = row.createCell(6);
            rateCell.setCellValue(entry.interestRate().doubleValue() / 100);
            rateCell.setCellStyle(percentStyle);
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Freeze the header row
        sheet.createFreezePane(0, 1);
    }

    private void createYearlyComparisonSheet(Workbook workbook, MortgageComparisonDto comparison) {
        Sheet sheet = workbook.createSheet("Yearly Comparison");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);

        int rowNum = 0;

        // Title
        Row titleRow = sheet.createRow(rowNum++);
        titleRow.createCell(0).setCellValue("Yearly Payment Comparison");
        titleRow.getCell(0).setCellStyle(headerStyle);

        rowNum++;

        // Create headers for each year (show first 10 years)
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Option");
        headerRow.getCell(0).setCellStyle(headerStyle);

        for (int year = 1; year <= 10; year++) {
            Cell cell = headerRow.createCell(year);
            cell.setCellValue("Year " + year + " Balance");
            cell.setCellStyle(headerStyle);
        }

        // Add data for each mortgage option
        for (int i = 0; i < comparison.mortgages().size(); i++) {
            MortgageCalculationDto mortgage = comparison.mortgages().get(i);
            Row dataRow = sheet.createRow(rowNum++);

            dataRow.createCell(0).setCellValue("Option " + (i + 1));

            List<AmortizationEntryDto> schedule = mortgage.amortizationSchedule();
            for (int year = 1; year <= 10; year++) {
                int paymentIndex = year * 12 - 1; // Last payment of the year
                if (paymentIndex < schedule.size()) {
                    Cell balanceCell = dataRow.createCell(year);
                    balanceCell.setCellValue(schedule.get(paymentIndex).remainingBalance().doubleValue());
                    balanceCell.setCellStyle(currencyStyle);
                } else {
                    dataRow.createCell(year).setCellValue(0); // Loan paid off
                }
            }
        }

        // Auto-size columns
        for (int i = 0; i <= 10; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createYearlyBreakdown(Sheet sheet, int startRow, List<AmortizationEntryDto> schedule,
            CellStyle headerStyle, CellStyle currencyStyle) {
        Row yearHeaderRow = sheet.createRow(startRow++);
        yearHeaderRow.createCell(0).setCellValue("Yearly Breakdown (First 10 Years)");
        yearHeaderRow.getCell(0).setCellStyle(headerStyle);

        startRow++;

        Row headers = sheet.createRow(startRow++);
        headers.createCell(0).setCellValue("Year");
        headers.createCell(1).setCellValue("Principal Paid");
        headers.createCell(2).setCellValue("Interest Paid");
        headers.createCell(3).setCellValue("Remaining Balance");

        for (Cell cell : headers) {
            cell.setCellStyle(headerStyle);
        }

        for (int year = 1; year <= Math.min(10, schedule.size() / 12 + 1); year++) {
            BigDecimal yearlyPrincipal = BigDecimal.ZERO;
            BigDecimal yearlyInterest = BigDecimal.ZERO;
            BigDecimal endBalance = BigDecimal.ZERO;

            int startIdx = (year - 1) * 12;
            int endIdx = Math.min(year * 12, schedule.size());

            for (int i = startIdx; i < endIdx; i++) {
                AmortizationEntryDto entry = schedule.get(i);
                yearlyPrincipal = yearlyPrincipal.add(entry.principalPayment());
                yearlyInterest = yearlyInterest.add(entry.interestPayment());
                endBalance = entry.remainingBalance();
            }

            Row yearRow = sheet.createRow(startRow++);
            yearRow.createCell(0).setCellValue(year);

            Cell principalCell = yearRow.createCell(1);
            principalCell.setCellValue(yearlyPrincipal.doubleValue());
            principalCell.setCellStyle(currencyStyle);

            Cell interestCell = yearRow.createCell(2);
            interestCell.setCellValue(yearlyInterest.doubleValue());
            interestCell.setCellStyle(currencyStyle);

            Cell balanceCell = yearRow.createCell(3);
            balanceCell.setCellValue(endBalance.doubleValue());
            balanceCell.setCellStyle(currencyStyle);
        }
    }

    private void addComparisonMetricRow(Sheet sheet, int rowNum, String metric, List<MortgageCalculationDto> mortgages,
            java.util.function.Function<MortgageCalculationDto, BigDecimal> valueExtractor,
            CellStyle valueStyle) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(metric);

        for (int i = 0; i < mortgages.size(); i++) {
            Cell cell = row.createCell(i + 1);
            BigDecimal value = valueExtractor.apply(mortgages.get(i));
            cell.setCellValue(value.doubleValue());
            if (valueStyle != null) {
                cell.setCellStyle(valueStyle);
            }
        }
    }

    private int findBestMonthlyPaymentIndex(List<MortgageCalculationDto> mortgages) {
        BigDecimal bestPayment = mortgages.get(0).monthlyPayment();
        int bestIndex = 0;

        for (int i = 1; i < mortgages.size(); i++) {
            if (mortgages.get(i).monthlyPayment().compareTo(bestPayment) < 0) {
                bestPayment = mortgages.get(i).monthlyPayment();
                bestIndex = i;
            }
        }

        return bestIndex;
    }

    private int findBestTotalCostIndex(List<MortgageCalculationDto> mortgages) {
        BigDecimal bestCost = mortgages.get(0).summary().totalAmountPaid();
        int bestIndex = 0;

        for (int i = 1; i < mortgages.size(); i++) {
            if (mortgages.get(i).summary().totalAmountPaid().compareTo(bestCost) < 0) {
                bestCost = mortgages.get(i).summary().totalAmountPaid();
                bestIndex = i;
            }
        }

        return bestIndex;
    }

    private CellStyle createEnhancedTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THICK);
        style.setBorderTop(BorderStyle.THICK);
        style.setBorderRight(BorderStyle.THICK);
        style.setBorderLeft(BorderStyle.THICK);
        return style;
    }

    private CellStyle createSummaryHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.MEDIUM);
        return style;
    }
}