package com.example.attendance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_attendance_summaries")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MonthlyAttendanceSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "year_month", nullable = false, length = 7)
    private String yearMonth;

    @Column(name = "total_work_minutes", nullable = false)
    @Builder.Default
    private Integer totalWorkMinutes = 0;

    @Column(name = "overtime_minutes", nullable = false)
    @Builder.Default
    private Integer overtimeMinutes = 0;

    @Column(name = "night_minutes", nullable = false)
    @Builder.Default
    private Integer nightMinutes = 0;

    @Column(name = "holiday_work_minutes", nullable = false)
    @Builder.Default
    private Integer holidayWorkMinutes = 0;

    @Column(name = "paid_leave_days", nullable = false, precision = 4, scale = 1)
    @Builder.Default
    private BigDecimal paidLeaveDays = BigDecimal.ZERO;

    @Column(name = "working_days", nullable = false)
    @Builder.Default
    private Integer workingDays = 0;

    @Version
    private Long version;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void updateSummary(Integer totalWorkMinutes, Integer overtimeMinutes,
                              Integer nightMinutes, Integer holidayWorkMinutes,
                              BigDecimal paidLeaveDays, Integer workingDays) {
        this.totalWorkMinutes = totalWorkMinutes;
        this.overtimeMinutes = overtimeMinutes;
        this.nightMinutes = nightMinutes;
        this.holidayWorkMinutes = holidayWorkMinutes;
        this.paidLeaveDays = paidLeaveDays;
        this.workingDays = workingDays;
    }
}
