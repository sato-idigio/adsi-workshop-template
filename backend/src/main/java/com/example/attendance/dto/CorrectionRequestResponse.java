package com.example.attendance.dto;

import com.example.attendance.entity.AttendanceCorrectionRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CorrectionRequestResponse(
    Long id,
    Long employeeId,
    String employeeName,
    Long attendanceRecordId,
    LocalDate date,
    LocalDateTime currentClockIn,
    LocalDateTime currentClockOut,
    LocalDateTime requestedClockIn,
    LocalDateTime requestedClockOut,
    String reason,
    String status,
    LocalDateTime createdAt
) {
    public static CorrectionRequestResponse from(AttendanceCorrectionRequest request) {
        var record = request.getAttendanceRecord();
        var employee = request.getEmployee();
        return new CorrectionRequestResponse(
            request.getId(),
            employee.getId(),
            employee.getLastName() + " " + employee.getFirstName(),
            record.getId(),
            record.getDate(),
            record.getClockIn(),
            record.getClockOut(),
            request.getRequestedClockIn(),
            request.getRequestedClockOut(),
            request.getReason(),
            request.getStatus(),
            request.getCreatedAt()
        );
    }
}
