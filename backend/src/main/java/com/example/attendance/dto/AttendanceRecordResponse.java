package com.example.attendance.dto;

import com.example.attendance.entity.AttendanceRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AttendanceRecordResponse(
    Long id,
    Long employeeId,
    LocalDate date,
    LocalDateTime clockIn,
    LocalDateTime clockOut,
    Integer workMinutes,
    Integer overtimeMinutes,
    Integer nightMinutes
) {
    public static AttendanceRecordResponse from(AttendanceRecord record) {
        return new AttendanceRecordResponse(
            record.getId(),
            record.getEmployee().getId(),
            record.getDate(),
            record.getClockIn(),
            record.getClockOut(),
            record.getWorkMinutes(),
            record.getOvertimeMinutes(),
            record.getNightMinutes()
        );
    }
}
