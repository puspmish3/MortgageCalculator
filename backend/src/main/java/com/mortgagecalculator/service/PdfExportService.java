package com.mortgagecalculator.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.borders.SolidBorder;

import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.mortgagecalculator.dto.AmortizationEntryDto;
import com.mortgagecalculator.dto.MortgageCalculationDto;
import com.mortgagecalculator.dto.MortgageComparisonDto;
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
public class PdfExportService {

    private static final Logger logger = LoggerFactory.getLogger(PdfExportService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    /**
     * Generate PDF for single mortgage calculation
     */
    public byte[] generateMortgagePdf(MortgageCalculationDto calculation, boolean includeChart) throws IOException {
        logger.info("Generating PDF for mortgage calculation: {}", calculation.calculationId());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        try {
            // Add title and header
            addHeader(document, "Mortgage Calculation Report");

            // Add summary section
            addMortgageSummary(document, calculation);

            // Add amortization schedule table
            addAmortizationTable(document, calculation.amortizationSchedule());

            // Add footer
            addFooter(document);

        } finally {
            document.close();
        }

        logger.info("PDF generated successfully for calculation: {}", calculation.calculationId());
        return outputStream.toByteArray();
    }

    /**
     * Generate PDF for mortgage comparison
     */
    public byte[] generateComparisonPdf(MortgageComparisonDto comparison, boolean includeChart) throws IOException {
        logger.info("Generating comparison PDF: {}", comparison.comparisonId());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        try {
            // Add title and header
            addHeader(document, "Mortgage Comparison Report");

            // Add executive summary
            addExecutiveSummary(document, comparison);

            // Add comprehensive comparison summary table
            addComprehensiveComparisonSummary(document, comparison);

            // Add visual comparison section (placeholder for chart data)
            addVisualComparisonSection(document, comparison);

            // Add individual mortgage details with complete summaries
            for (int i = 0; i < comparison.mortgages().size(); i++) {
                document.add(new AreaBreak());
                addDetailedMortgageSummary(document, comparison.mortgages().get(i), "Option " + (i + 1));
            }

            // Add complete amortization schedules for all options
            for (int i = 0; i < comparison.mortgages().size(); i++) {
                document.add(new AreaBreak());
                addCompleteAmortizationTable(document, comparison.mortgages().get(i).amortizationSchedule(),
                        "Option " + (i + 1) + " - Complete Amortization Schedule");
            }

            // Add recommendations section
            addRecommendationsSection(document, comparison);

            // Add footer
            addFooter(document);

        } finally {
            document.close();
        }

        logger.info("Comparison PDF generated successfully: {}", comparison.comparisonId());
        return outputStream.toByteArray();
    }

    private void addHeader(Document document, String title) {
        // Main title with gradient-like background effect
        Paragraph titleParagraph = new Paragraph(title)
                .setFontSize(28)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(new DeviceRgb(41, 128, 185)) // Professional blue
                .setPadding(15)
                .setMarginBottom(5);
        document.add(titleParagraph);

        // Subtitle with lighter blue background
        Paragraph subtitleParagraph = new Paragraph("Comprehensive Mortgage Analysis & Comparison")
                .setFontSize(14)
                .setItalic()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(new DeviceRgb(52, 152, 219)) // Lighter blue
                .setPadding(8)
                .setMarginBottom(20);
        document.add(subtitleParagraph);

        // Generation date with accent color
        Paragraph dateParagraph = new Paragraph("Generated on: " + LocalDate.now().format(DATE_FORMATTER))
                .setFontSize(12)
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontColor(new DeviceRgb(127, 140, 141)) // Gray
                .setMarginBottom(30);
        document.add(dateParagraph);

        // Add a separator line
        LineSeparator separator = new LineSeparator(new SolidLine(2f))
                .setStrokeColor(new DeviceRgb(52, 152, 219));
        document.add(separator);
        document.add(new Paragraph().setMarginBottom(20));
    }

    private void addMortgageSummary(Document document, MortgageCalculationDto calculation) {
        addMortgageSummary(document, calculation, "Loan Summary");
    }

    private void addMortgageSummary(Document document, MortgageCalculationDto calculation, String sectionTitle) {
        // Section title with colorful background
        Paragraph sectionHeader = new Paragraph(sectionTitle)
                .setFontSize(18)
                .setBold()
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(new DeviceRgb(46, 204, 113)) // Green background
                .setPadding(10)
                .setMarginBottom(15);
        document.add(sectionHeader);

        // Summary table with alternating row colors
        Table summaryTable = new Table(UnitValue.createPercentArray(new float[] { 2, 3 }))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20)
                .setBorder(new SolidBorder(new DeviceRgb(189, 195, 199), 1));

        addColoredSummaryRow(summaryTable, "Loan Amount:", formatCurrency(calculation.summary().loanAmount()), true);
        addColoredSummaryRow(summaryTable, "Interest Rate:", calculation.summary().interestRate() + "%", false);
        addColoredSummaryRow(summaryTable, "Loan Term:", calculation.summary().loanTermYears() + " years", true);
        addColoredSummaryRow(summaryTable, "Payment Frequency:",
                calculation.summary().paymentFrequency().getDisplayName(), false);
        addColoredSummaryRow(summaryTable, "Monthly Payment:", formatCurrency(calculation.monthlyPayment()), true,
                new DeviceRgb(155, 89, 182)); // Purple highlight for important values
        addColoredSummaryRow(summaryTable, "Total Interest:", formatCurrency(calculation.totalInterest()), false,
                new DeviceRgb(231, 76, 60)); // Red for interest
        addColoredSummaryRow(summaryTable, "Total Amount Paid:",
                formatCurrency(calculation.summary().totalAmountPaid()), true, new DeviceRgb(52, 152, 219)); // Blue for
                                                                                                             // totals
        addColoredSummaryRow(summaryTable, "Total Payments:", String.valueOf(calculation.totalPayments()), false);

        document.add(summaryTable);
    }

    private void addComparisonSummary(Document document, MortgageComparisonDto comparison) {
        // Section title
        Paragraph sectionHeader = new Paragraph("Comparison Summary")
                .setFontSize(18)
                .setBold()
                .setMarginBottom(15);
        document.add(sectionHeader);

        // Best options summary
        Table bestOptionsTable = new Table(UnitValue.createPercentArray(new float[] { 2, 3 }))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        addSummaryRow(bestOptionsTable, "Best Monthly Payment:",
                formatCurrency(comparison.comparisonSummary().bestMonthlyPayment()));
        addSummaryRow(bestOptionsTable, "Lowest Total Interest:",
                formatCurrency(comparison.comparisonSummary().bestTotalInterest()));

        document.add(bestOptionsTable);

        // Comparison table
        if (comparison.mortgages().size() >= 2) {
            Table comparisonTable = new Table(UnitValue.createPercentArray(
                    new float[] { 2, 1.5f, 1.5f, 1.5f, 1.5f }))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(20);

            // Headers
            comparisonTable.addHeaderCell(new Cell().add(new Paragraph("Metric").setBold()));
            for (int i = 0; i < Math.min(comparison.mortgages().size(), 4); i++) {
                comparisonTable.addHeaderCell(new Cell().add(new Paragraph("Option " + (i + 1)).setBold()));
            }

            // Monthly Payment row
            comparisonTable.addCell(new Cell().add(new Paragraph("Monthly Payment")));
            for (int i = 0; i < Math.min(comparison.mortgages().size(), 4); i++) {
                comparisonTable.addCell(new Cell().add(new Paragraph(
                        formatCurrency(comparison.mortgages().get(i).monthlyPayment()))));
            }

            // Total Interest row
            comparisonTable.addCell(new Cell().add(new Paragraph("Total Interest")));
            for (int i = 0; i < Math.min(comparison.mortgages().size(), 4); i++) {
                comparisonTable.addCell(new Cell().add(new Paragraph(
                        formatCurrency(comparison.mortgages().get(i).totalInterest()))));
            }

            document.add(comparisonTable);
        }
    }

    private void addAmortizationTable(Document document, List<AmortizationEntryDto> schedule) {
        // Section title
        Paragraph sectionHeader = new Paragraph("Amortization Schedule")
                .setFontSize(18)
                .setBold()
                .setMarginBottom(15);
        document.add(sectionHeader);

        // Create table with appropriate columns
        Table table = new Table(UnitValue.createPercentArray(new float[] { 1, 2, 1.5f, 1.5f, 1.5f, 2 }))
                .setWidth(UnitValue.createPercentValue(100))
                .setFontSize(9);

        // Add headers
        table.addHeaderCell(new Cell().add(new Paragraph("Payment #").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Date").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Principal").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Interest").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Total Payment").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Remaining Balance").setBold()));

        // Add data rows (limit to first 120 payments for PDF readability)
        int maxRows = Math.min(schedule.size(), 120);
        for (int i = 0; i < maxRows; i++) {
            AmortizationEntryDto entry = schedule.get(i);

            table.addCell(new Cell().add(new Paragraph(String.valueOf(entry.paymentNumber()))));
            table.addCell(new Cell().add(new Paragraph(entry.paymentDate().format(DATE_FORMATTER))));
            table.addCell(new Cell().add(new Paragraph(formatCurrency(entry.principalPayment()))));
            table.addCell(new Cell().add(new Paragraph(formatCurrency(entry.interestPayment()))));
            table.addCell(new Cell().add(new Paragraph(formatCurrency(entry.totalPayment()))));
            table.addCell(new Cell().add(new Paragraph(formatCurrency(entry.remainingBalance()))));
        }

        document.add(table);

        // Add note if schedule was truncated
        if (schedule.size() > maxRows) {
            Paragraph note = new Paragraph(
                    "Note: Showing first " + maxRows + " payments of " + schedule.size() + " total payments.")
                    .setFontSize(10)
                    .setItalic()
                    .setMarginTop(10);
            document.add(note);
        }
    }

    private void addSummaryRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold()));
        table.addCell(new Cell().add(new Paragraph(value)));
    }

    private void addColoredSummaryRow(Table table, String label, String value, boolean alternateRow) {
        addColoredSummaryRow(table, label, value, alternateRow, null);
    }

    private void addColoredSummaryRow(Table table, String label, String value, boolean alternateRow,
            DeviceRgb highlightColor) {
        DeviceRgb backgroundColor = alternateRow ? new DeviceRgb(247, 249, 250) : // Light gray for alternate rows
                new DeviceRgb(255, 255, 255); // White

        Color textColor = highlightColor != null ? ColorConstants.WHITE : ColorConstants.BLACK;
        Color cellColor = highlightColor != null ? highlightColor : backgroundColor;

        Cell labelCell = new Cell()
                .add(new Paragraph(label).setBold().setFontColor(textColor))
                .setBackgroundColor(cellColor)
                .setPadding(8)
                .setBorder(new SolidBorder(new DeviceRgb(189, 195, 199), 0.5f));

        Cell valueCell = new Cell()
                .add(new Paragraph(value).setFontColor(textColor))
                .setBackgroundColor(cellColor)
                .setPadding(8)
                .setBorder(new SolidBorder(new DeviceRgb(189, 195, 199), 0.5f));

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addBarChart(Document document, String title, MortgageComparisonDto comparison, String metric) {
        // Chart title
        Paragraph chartTitle = new Paragraph(title)
                .setFontSize(16)
                .setBold()
                .setMarginBottom(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(new DeviceRgb(52, 73, 94));
        document.add(chartTitle);

        // Create visual bar chart using tables
        Table chartTable = new Table(UnitValue.createPercentArray(new float[] { 2, 8 }))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(30);

        // Colors for different options
        DeviceRgb[] colors = {
                new DeviceRgb(52, 152, 219), // Blue
                new DeviceRgb(46, 204, 113), // Green
                new DeviceRgb(155, 89, 182), // Purple
                new DeviceRgb(231, 76, 60), // Red
        };

        // Calculate max value for scaling
        double maxValue = getMaxValueForMetric(comparison, metric);

        for (int i = 0; i < comparison.mortgages().size(); i++) {
            MortgageCalculationDto mortgage = comparison.mortgages().get(i);
            double value = getValueForMetric(mortgage, metric);

            // Option label
            Cell labelCell = new Cell()
                    .add(new Paragraph("Option " + (i + 1)).setBold())
                    .setBackgroundColor(new DeviceRgb(236, 240, 241))
                    .setPadding(8)
                    .setTextAlignment(TextAlignment.CENTER);

            // Bar representation
            double barWidth = (value / maxValue) * 80; // Scale to 80% max width
            String barValue = formatValueForMetric(value, metric);

            Cell barCell = new Cell()
                    .setPadding(4);

            // Create visual bar using nested table
            Table barTable = new Table(
                    UnitValue.createPercentArray(new float[] { (float) barWidth, (float) (100 - barWidth) }))
                    .setWidth(UnitValue.createPercentValue(100));

            Cell colorBar = new Cell()
                    .setBackgroundColor(colors[i % colors.length])
                    .setPadding(6)
                    .add(new Paragraph(barValue)
                            .setFontColor(ColorConstants.WHITE)
                            .setBold()
                            .setTextAlignment(TextAlignment.CENTER));

            Cell emptySpace = new Cell()
                    .setBackgroundColor(new DeviceRgb(245, 245, 245))
                    .setPadding(6);

            barTable.addCell(colorBar);
            if (barWidth < 100) {
                barTable.addCell(emptySpace);
            }

            barCell.add(barTable);

            chartTable.addCell(labelCell);
            chartTable.addCell(barCell);
        }

        document.add(chartTable);
    }

    private double getMaxValueForMetric(MortgageComparisonDto comparison, String metric) {
        return comparison.mortgages().stream()
                .mapToDouble(mortgage -> getValueForMetric(mortgage, metric))
                .max()
                .orElse(0.0);
    }

    private double getValueForMetric(MortgageCalculationDto mortgage, String metric) {
        switch (metric) {
            case "monthlyPayment":
                return mortgage.monthlyPayment().doubleValue();
            case "totalCost":
                return mortgage.summary().totalAmountPaid().doubleValue();
            case "totalInterest":
                return mortgage.totalInterest().doubleValue();
            default:
                return 0.0;
        }
    }

    private String formatValueForMetric(double value, String metric) {
        return formatCurrency(BigDecimal.valueOf(value));
    }

    private void addFooter(Document document) {
        // Add separator line before footer
        LineSeparator separator = new LineSeparator(new SolidLine(1f))
                .setStrokeColor(new DeviceRgb(189, 195, 199))
                .setMarginTop(20);
        document.add(separator);

        Paragraph footer = new Paragraph(
                "Generated by Mortgage Calculator Application | Professional Financial Analysis")
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(new DeviceRgb(127, 140, 141))
                .setMarginTop(15);
        document.add(footer);
    }

    private void addExecutiveSummary(Document document, MortgageComparisonDto comparison) {
        // Executive Summary Section
        Paragraph sectionHeader = new Paragraph("Executive Summary")
                .setFontSize(18)
                .setBold()
                .setMarginBottom(15);
        document.add(sectionHeader);

        // Key findings
        Paragraph summary = new Paragraph()
                .setMarginBottom(20);

        summary.add("This report compares " + comparison.mortgages().size() + " mortgage options. ");
        summary.add(
                "The analysis includes monthly payment comparisons, total cost analysis, and detailed amortization schedules for each option. ");
        summary.add("Key recommendations are provided based on different financial scenarios and priorities.");

        document.add(summary);
    }

    private void addComprehensiveComparisonSummary(Document document, MortgageComparisonDto comparison) {
        // Reset row counter for this table
        comparisonRowCounter = 0;

        // Enhanced Comprehensive Comparison Summary Header
        Paragraph sectionHeader = new Paragraph("📋 Detailed Comparison Summary")
                .setFontSize(18)
                .setBold()
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(new DeviceRgb(41, 128, 185)) // Professional blue
                .setPadding(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(sectionHeader);

        // Create comprehensive comparison table with dynamic column widths
        int numOptions = comparison.mortgages().size();
        float[] columnWidths = new float[numOptions + 1]; // +1 for the metric column
        columnWidths[0] = 2.5f; // Metric column (wider)
        for (int i = 1; i <= numOptions; i++) {
            columnWidths[i] = 1.5f; // Option columns (equal width)
        }

        Table comparisonTable = new Table(UnitValue.createPercentArray(columnWidths))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        // Enhanced Headers with proper styling
        Cell metricHeaderCell = new Cell()
                .add(new Paragraph("Metric").setBold().setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(new DeviceRgb(52, 73, 94))
                .setPadding(10)
                .setTextAlignment(TextAlignment.CENTER);
        comparisonTable.addHeaderCell(metricHeaderCell);

        for (int i = 0; i < comparison.mortgages().size(); i++) {
            Cell optionHeaderCell = new Cell()
                    .add(new Paragraph("Option " + (i + 1)).setBold().setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(new DeviceRgb(52, 73, 94))
                    .setPadding(10)
                    .setTextAlignment(TextAlignment.CENTER);
            comparisonTable.addHeaderCell(optionHeaderCell);
        }

        // Add rows for all key metrics
        addComparisonRow(comparisonTable, "Loan Amount", comparison.mortgages(),
                m -> formatCurrency(m.summary().loanAmount()));
        addComparisonRow(comparisonTable, "Interest Rate", comparison.mortgages(),
                m -> m.summary().interestRate() + "%");
        addComparisonRow(comparisonTable, "Loan Term", comparison.mortgages(),
                m -> m.summary().loanTermYears() + " years");
        addComparisonRow(comparisonTable, "Monthly Payment", comparison.mortgages(),
                m -> formatCurrency(m.monthlyPayment()));
        addComparisonRow(comparisonTable, "Total Interest", comparison.mortgages(),
                m -> formatCurrency(m.totalInterest()));
        addComparisonRow(comparisonTable, "Total Amount Paid", comparison.mortgages(),
                m -> formatCurrency(m.summary().totalAmountPaid()));
        addComparisonRow(comparisonTable, "Total Payments", comparison.mortgages(),
                m -> String.valueOf(m.totalPayments()));

        document.add(comparisonTable);
    }

    private void addVisualComparisonSection(Document document, MortgageComparisonDto comparison) {
        // Visual Comparison Section with Enhanced Charts
        Paragraph sectionHeader = new Paragraph("Visual Comparison Charts & Analysis")
                .setFontSize(20)
                .setBold()
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(new DeviceRgb(230, 126, 34)) // Orange background
                .setPadding(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(25);
        document.add(sectionHeader);

        // Monthly Payment Comparison Chart (Bar Chart representation)
        addBarChart(document, "Monthly Payment Comparison", comparison, "monthlyPayment");

        // Total Cost Comparison Chart
        addBarChart(document, "Total Cost Comparison", comparison, "totalCost");

        // Interest Comparison Chart
        addBarChart(document, "Total Interest Comparison", comparison, "totalInterest");

        // Cost breakdown analysis with enhanced styling
        Paragraph breakdownTitle = new Paragraph("Detailed Cost Breakdown")
                .setFontSize(16)
                .setBold()
                .setMarginTop(30)
                .setMarginBottom(15)
                .setFontColor(new DeviceRgb(52, 73, 94));
        document.add(breakdownTitle);

        Table costBreakdownTable = new Table(UnitValue.createPercentArray(new float[] { 2, 2, 2 }))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        // Enhanced headers with color
        Cell optionHeader = new Cell()
                .add(new Paragraph("Option").setBold().setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(new DeviceRgb(52, 73, 94))
                .setPadding(10)
                .setTextAlignment(TextAlignment.CENTER);
        Cell principalHeader = new Cell()
                .add(new Paragraph("Principal Amount").setBold().setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(new DeviceRgb(52, 73, 94))
                .setPadding(10)
                .setTextAlignment(TextAlignment.CENTER);
        Cell interestHeader = new Cell()
                .add(new Paragraph("Interest Amount").setBold().setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(new DeviceRgb(52, 73, 94))
                .setPadding(10)
                .setTextAlignment(TextAlignment.CENTER);

        costBreakdownTable.addHeaderCell(optionHeader);
        costBreakdownTable.addHeaderCell(principalHeader);
        costBreakdownTable.addHeaderCell(interestHeader);

        DeviceRgb[] rowColors = {
                new DeviceRgb(247, 249, 250), // Light gray
                new DeviceRgb(255, 255, 255) // White
        };

        for (int i = 0; i < comparison.mortgages().size(); i++) {
            MortgageCalculationDto mortgage = comparison.mortgages().get(i);
            DeviceRgb rowColor = rowColors[i % 2];

            Cell optionCell = new Cell()
                    .add(new Paragraph("Option " + (i + 1)).setBold())
                    .setBackgroundColor(rowColor)
                    .setPadding(8)
                    .setTextAlignment(TextAlignment.CENTER);
            Cell principalCell = new Cell()
                    .add(new Paragraph(formatCurrency(mortgage.summary().loanAmount())))
                    .setBackgroundColor(rowColor)
                    .setPadding(8)
                    .setTextAlignment(TextAlignment.RIGHT);
            Cell interestCell = new Cell()
                    .add(new Paragraph(formatCurrency(mortgage.totalInterest()))
                            .setFontColor(new DeviceRgb(231, 76, 60))) // Red for interest
                    .setBackgroundColor(rowColor)
                    .setPadding(8)
                    .setTextAlignment(TextAlignment.RIGHT);

            costBreakdownTable.addCell(optionCell);
            costBreakdownTable.addCell(principalCell);
            costBreakdownTable.addCell(interestCell);
        }

        document.add(costBreakdownTable);

        // Savings analysis with enhanced styling
        BigDecimal lowestTotalCost = comparison.mortgages().stream()
                .map(m -> m.summary().totalAmountPaid())
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal highestTotalCost = comparison.mortgages().stream()
                .map(m -> m.summary().totalAmountPaid())
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal maxSavings = highestTotalCost.subtract(lowestTotalCost);

        Paragraph savingsAnalysis = new Paragraph(
                "Potential Savings: Choosing the most cost-effective option could save you "
                        + formatCurrency(maxSavings) + " over the life of the loan.")
                .setMarginBottom(20)
                .setBold();
        document.add(savingsAnalysis);
    }

    private void addDetailedMortgageSummary(Document document, MortgageCalculationDto calculation,
            String sectionTitle) {
        // Enhanced version of mortgage summary with more details
        Paragraph sectionHeader = new Paragraph(sectionTitle + " - Detailed Summary")
                .setFontSize(18)
                .setBold()
                .setMarginBottom(15);
        document.add(sectionHeader);

        // Create detailed summary table
        Table summaryTable = new Table(UnitValue.createPercentArray(new float[] { 2, 3 }))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        addSummaryRow(summaryTable, "Loan Amount:", formatCurrency(calculation.summary().loanAmount()));
        addSummaryRow(summaryTable, "Interest Rate:", calculation.summary().interestRate() + "%");
        addSummaryRow(summaryTable, "Loan Term:", calculation.summary().loanTermYears() + " years");
        addSummaryRow(summaryTable, "Payment Frequency:", calculation.summary().paymentFrequency().getDisplayName());
        addSummaryRow(summaryTable, "Monthly Payment:", formatCurrency(calculation.monthlyPayment()));
        addSummaryRow(summaryTable, "Total Interest:", formatCurrency(calculation.totalInterest()));
        addSummaryRow(summaryTable, "Total Amount Paid:", formatCurrency(calculation.summary().totalAmountPaid()));
        addSummaryRow(summaryTable, "Total Payments:", String.valueOf(calculation.totalPayments()));

        document.add(summaryTable);

        // Add payment breakdown by year
        addPaymentBreakdownByYear(document, calculation.amortizationSchedule());
    }

    private void addCompleteAmortizationTable(Document document, List<AmortizationEntryDto> schedule, String title) {
        // Section title
        Paragraph sectionHeader = new Paragraph(title)
                .setFontSize(16)
                .setBold()
                .setMarginBottom(15);
        document.add(sectionHeader);

        // Create table with all columns including interest rate
        Table table = new Table(UnitValue.createPercentArray(new float[] { 0.8f, 1.8f, 1.2f, 1.2f, 1.2f, 1.5f, 1.0f }))
                .setWidth(UnitValue.createPercentValue(100))
                .setFontSize(8);

        // Add headers
        table.addHeaderCell(new Cell().add(new Paragraph("Payment").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Date").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Principal").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Interest").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Total Payment").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Balance").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Rate %").setBold()));

        // Add all data rows
        for (AmortizationEntryDto entry : schedule) {
            table.addCell(new Cell().add(new Paragraph(String.valueOf(entry.paymentNumber()))));
            table.addCell(new Cell().add(new Paragraph(entry.paymentDate().format(DATE_FORMATTER))));
            table.addCell(new Cell().add(new Paragraph(formatCurrency(entry.principalPayment()))));
            table.addCell(new Cell().add(new Paragraph(formatCurrency(entry.interestPayment()))));
            table.addCell(new Cell().add(new Paragraph(formatCurrency(entry.totalPayment()))));
            table.addCell(new Cell().add(new Paragraph(formatCurrency(entry.remainingBalance()))));
            table.addCell(new Cell().add(new Paragraph(String.format("%.3f", entry.interestRate()))));
        }

        document.add(table);
    }

    private void addRecommendationsSection(Document document, MortgageComparisonDto comparison) {
        // Recommendations Section
        Paragraph sectionHeader = new Paragraph("Recommendations")
                .setFontSize(18)
                .setBold()
                .setMarginBottom(15);
        document.add(sectionHeader);

        // Find best options
        int bestMonthlyIndex = findBestMonthlyPaymentIndex(comparison.mortgages());
        int bestTotalCostIndex = findBestTotalCostIndex(comparison.mortgages());

        // Add recommendations
        List<String> recommendations = List.of(
                String.format("For lowest monthly payment: Choose Option %d with $%s/month",
                        bestMonthlyIndex + 1,
                        formatCurrency(comparison.mortgages().get(bestMonthlyIndex).monthlyPayment())),
                String.format("For lowest total cost: Choose Option %d with total cost of $%s",
                        bestTotalCostIndex + 1,
                        formatCurrency(comparison.mortgages().get(bestTotalCostIndex).summary().totalAmountPaid())),
                "Consider your cash flow needs and long-term financial goals when making your decision.",
                "Consult with a financial advisor for personalized recommendations.");

        for (String recommendation : recommendations) {
            Paragraph recParagraph = new Paragraph("• " + recommendation)
                    .setMarginLeft(20)
                    .setMarginBottom(10);
            document.add(recParagraph);
        }
    }

    private void addPaymentBreakdownByYear(Document document, List<AmortizationEntryDto> schedule) {
        Paragraph yearBreakdownHeader = new Paragraph("Annual Payment Breakdown")
                .setFontSize(14)
                .setBold()
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(yearBreakdownHeader);

        // Group payments by year and calculate totals
        Table yearTable = new Table(UnitValue.createPercentArray(new float[] { 1, 2, 2, 2 }))
                .setWidth(UnitValue.createPercentValue(100))
                .setFontSize(10);

        yearTable.addHeaderCell(new Cell().add(new Paragraph("Year").setBold()));
        yearTable.addHeaderCell(new Cell().add(new Paragraph("Principal Paid").setBold()));
        yearTable.addHeaderCell(new Cell().add(new Paragraph("Interest Paid").setBold()));
        yearTable.addHeaderCell(new Cell().add(new Paragraph("Balance End of Year").setBold()));

        // Calculate yearly totals (show first 10 years)
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

            yearTable.addCell(new Cell().add(new Paragraph(String.valueOf(year))));
            yearTable.addCell(new Cell().add(new Paragraph(formatCurrency(yearlyPrincipal))));
            yearTable.addCell(new Cell().add(new Paragraph(formatCurrency(yearlyInterest))));
            yearTable.addCell(new Cell().add(new Paragraph(formatCurrency(endBalance))));
        }

        document.add(yearTable);
    }

    private int comparisonRowCounter = 0; // Instance variable for row counting

    private void addComparisonRow(Table table, String metric, List<MortgageCalculationDto> mortgages,
            java.util.function.Function<MortgageCalculationDto, String> valueExtractor) {
        comparisonRowCounter++;

        DeviceRgb rowColor = (comparisonRowCounter % 2 == 0) ? new DeviceRgb(247, 249, 250) : // Light gray for even
                                                                                              // rows
                new DeviceRgb(255, 255, 255); // White for odd rows

        // Metric column with enhanced styling
        Cell metricCell = new Cell()
                .add(new Paragraph(metric).setBold())
                .setBackgroundColor(rowColor)
                .setPadding(8)
                .setTextAlignment(TextAlignment.LEFT);
        table.addCell(metricCell);

        // Value columns with proper alignment
        for (MortgageCalculationDto mortgage : mortgages) {
            String value = valueExtractor.apply(mortgage);
            TextAlignment alignment = value.contains("$") || value.contains("%") ? TextAlignment.RIGHT
                    : TextAlignment.CENTER;

            Cell valueCell = new Cell()
                    .add(new Paragraph(value))
                    .setBackgroundColor(rowColor)
                    .setPadding(8)
                    .setTextAlignment(alignment);
            table.addCell(valueCell);
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

    private String formatCurrency(BigDecimal amount) {
        return String.format("$%,.2f", amount);
    }
}