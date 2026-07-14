package com.example.attendance.dto;

import com.example.attendance.entity.LeaveRequest;
import com.example.attendance.entity.enums.ApprovalStatus;
import com.example.attendance.entity.enums.LeaveType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record LeaveRequestResponse(
        Long id,
        Long employeeId,
        String employeeName,
        LeaveType leaveType,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal days,
        String reason,
        ApprovalStatus status,
        LocalDateTime createdAt
) {
    public static LeaveRequestResponse from(LeaveRequest entity) {
        return new LeaveRequestResponse(
                entity.getId(),
                entity.getEmployee().getId(),
                entity.getEmployee().getLastName() + " " + entity.getEmployee().getFirstName(),
                entity.getLeaveType(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getDays(),
                entity.getReason(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
