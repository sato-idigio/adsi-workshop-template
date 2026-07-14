package com.example.attendance.service;

import com.example.attendance.dto.MonthlySummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MonthlyReportService {

    Page<MonthlySummaryResponse> getMonthlySummaries(String yearMonth, Long departmentId, Pageable pageable);

    void generateSummaries(String yearMonth);
}
