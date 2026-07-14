package com.example.attendance.controller;

import com.example.attendance.dto.MonthlySummaryResponse;
import com.example.attendance.service.MonthlyReportService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/reports")
public class MonthlyReportController {

    private final MonthlyReportService monthlyReportService;

    public MonthlyReportController(MonthlyReportService monthlyReportService) {
        this.monthlyReportService = monthlyReportService;
    }

    @GetMapping("/monthly")
    public ResponseEntity<Page<MonthlySummaryResponse>> getMonthlyReport(
            @RequestParam @NotNull @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "yearMonth の形式が不正です（YYYY-MM）") String yearMonth,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        var result = monthlyReportService.getMonthlySummaries(yearMonth, departmentId, PageRequest.of(page, size));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/monthly/generate")
    public ResponseEntity<Void> generateMonthlyReport(
            @RequestParam @NotNull @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "yearMonth の形式が不正です（YYYY-MM）") String yearMonth) {
        monthlyReportService.generateSummaries(yearMonth);
        return ResponseEntity.ok().build();
    }
}
