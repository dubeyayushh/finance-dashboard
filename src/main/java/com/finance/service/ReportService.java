package com.finance.service;

import com.finance.model.Transaction;
import com.finance.model.User;
import com.finance.model.enums.TransactionType;
import com.finance.repository.TransactionRepository;
import com.finance.repository.UserRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    // ── CSV Export ────────────────────────────────────────────────────────
    // Returns raw bytes — controller streams it as a downloadable file
    public byte[] exportCsv(String email, int month, int year)
            throws IOException {

        List<Transaction> transactions = getTransactions(email, month, year);

        StringWriter sw = new StringWriter();
        try (CSVWriter writer = new CSVWriter(sw)) {

            // Header row
            writer.writeNext(new String[]{
                    "Date", "Category", "Type", "Amount (INR)", "Description"
            });

            // Data rows
            for (Transaction t : transactions) {
                writer.writeNext(new String[]{
                        t.getTransactionDate().toString(),
                        t.getCategory().getName(),
                        t.getType().name(),
                        t.getAmount().toPlainString(),
                        t.getDescription() != null ? t.getDescription() : ""
                });
            }
        }

        return sw.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ── PDF Export ────────────────────────────────────────────────────────
    // Generates a styled PDF with a colored table and summary footer
    public byte[] exportPdf(String email, int month, int year)
            throws DocumentException {

        List<Transaction> transactions = getTransactions(email, month, year);
        User user = userRepository.findByEmail(email).orElseThrow();

        // Write PDF into memory — return as byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, baos);
        doc.open();

        // ── Fonts ─────────────────────────────────────────────────────────
        Font titleFont  = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font headerFont = new Font(Font.HELVETICA, 11, Font.BOLD);
        Font smallFont  = new Font(Font.HELVETICA,  9);

        // ── Report Header ─────────────────────────────────────────────────
        String monthName = Month.of(month)
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        doc.add(new Paragraph(
                "Finance Report — " + monthName + " " + year, titleFont));
        doc.add(new Paragraph(
                "User: " + user.getName() + " | " + user.getEmail(), smallFont));
        doc.add(new Paragraph(
                "Generated: " + LocalDate.now(), smallFont));
        doc.add(Chunk.NEWLINE);

        // ── Table ─────────────────────────────────────────────────────────
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2f, 2f, 1.5f, 1.5f, 3f});

        // Table header row — indigo background
        java.awt.Color headerBg = new java.awt.Color(99, 102, 241);
        for (String h : new String[]{
                "Date", "Category", "Type", "Amount", "Description"}) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(headerBg);
            cell.setPadding(6);
            table.addCell(cell);
        }

        // ── Data rows ─────────────────────────────────────────────────────
        BigDecimal totalIncome   = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (Transaction t : transactions) {

            // Green rows for income, red rows for expenses
            java.awt.Color rowColor = t.getType() == TransactionType.INCOME
                    ? new java.awt.Color(220, 252, 231)   // light green
                    : new java.awt.Color(254, 226, 226);  // light red

            String[] values = {
                    t.getTransactionDate().toString(),
                    t.getCategory().getName(),
                    t.getType().name(),
                    "Rs." + t.getAmount().toPlainString(),
                    t.getDescription() != null ? t.getDescription() : ""
            };

            for (String val : values) {
                PdfPCell cell = new PdfPCell(new Phrase(val, smallFont));
                cell.setBackgroundColor(rowColor);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Running totals for summary footer
            if (t.getType() == TransactionType.INCOME) {
                totalIncome = totalIncome.add(t.getAmount());
            } else {
                totalExpenses = totalExpenses.add(t.getAmount());
            }
        }

        doc.add(table);
        doc.add(Chunk.NEWLINE);

        // ── Summary Footer ────────────────────────────────────────────────
        doc.add(new Paragraph(
                "Total Income:   Rs." + totalIncome, headerFont));
        doc.add(new Paragraph(
                "Total Expenses: Rs." + totalExpenses, headerFont));
        doc.add(new Paragraph(
                "Net Savings:    Rs." + totalIncome.subtract(totalExpenses),
                headerFont));

        doc.close();
        return baos.toByteArray();
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private List<Transaction> getTransactions(
            String email, int month, int year) {

        User user = userRepository.findByEmail(email).orElseThrow();
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end   = start.withDayOfMonth(start.lengthOfMonth());

        return transactionRepository
                .findByUserAndTransactionDateBetweenOrderByTransactionDateDesc(
                        user, start, end);
    }
}