package com.example.attendance.dto;

import com.example.attendance.entity.MonthlyAttendanceSummary;

import java.math.BigDecimal;

public record MonthlySummaryResponse(
        Long employeeId,
        String employeeName,
        String departmentName,
        String yearMonth,
        Integer totalWorkMinutes,
        Integer overtimeMinutes,
        Integer nightMinutes,
        Integer holidayWorkMinutes,
        BigDecimal paidLeaveDays,
        Integer workingDays
) {
    public static MonthlySummaryResponse from(MonthlyAttendanceSummary entity) {
        var employee = entity.getEmployee();
        return new MonthlySummaryResponse(
                employee.getId(),
                employee.getLastName() + " " + employee.getFirstName(),
                employee.getDepartment().getName(),
                entity.getYearMonth(),
                entity.getTotalWorkMinutes(),
                entity.getOvertimeMinutes(),
                entity.getNightMinutes(),
                entity.getHolidayWorkMinutes(),
                entity.getPaidLeaveDays(),
                entity.getWorkingDays()
        );
    }
}
