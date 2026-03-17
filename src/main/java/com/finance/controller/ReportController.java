package com.finance.controller;

import com.finance.service.ReportService;
import com.lowagie.text.DocumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // GET /api/reports/csv?month=3&year=2026
    // Content-Disposition header tells browser to download as file
    @GetMapping("/csv")
    public ResponseEntity<byte[]> csv(
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year,
            @AuthenticationPrincipal UserDetails userDetails)
            throws IOException {

        LocalDate now = LocalDate.now();
        byte[] data = reportService.exportCsv(
                userDetails.getUsername(),
                month == 0 ? now.getMonthValue() : month,
                year  == 0 ? now.getYear()       : year);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=transactions.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }

    // GET /api/reports/pdf?month=3&year=2026
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdf(
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year,
            @AuthenticationPrincipal UserDetails userDetails)
            throws DocumentException {

        LocalDate now = LocalDate.now();
        byte[] data = reportService.exportPdf(
                userDetails.getUsername(),
                month == 0 ? now.getMonthValue() : month,
                year  == 0 ? now.getYear()       : year);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }
}