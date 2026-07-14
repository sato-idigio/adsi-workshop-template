package com.example.attendance.dto;

import com.example.attendance.entity.LeaveBalance;

import java.math.BigDecimal;

public record LeaveBalanceResponse(
        Integer fiscalYear,
        BigDecimal totalDays,
        BigDecimal usedDays,
        BigDecimal remainingDays
) {
    public static LeaveBalanceResponse from(LeaveBalance entity) {
        return new LeaveBalanceResponse(
                entity.getFiscalYear(),
                entity.getTotalDays(),
                entity.getUsedDays(),
                entity.getRemainingDays()
        );
    }
}
