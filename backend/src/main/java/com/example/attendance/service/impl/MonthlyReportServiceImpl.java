package com.example.attendance.service.impl;

import com.example.attendance.dto.MonthlySummaryResponse;
import com.example.attendance.entity.Employee;
import com.example.attendance.entity.LeaveRequest;
import com.example.attendance.entity.MonthlyAttendanceSummary;
import com.example.attendance.entity.enums.LeaveType;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.repository.LeaveRequestRepository;
import com.example.attendance.repository.MonthlyAttendanceSummaryRepository;
import com.example.attendance.service.MonthlyReportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MonthlyReportServiceImpl implements MonthlyReportService {

    private final MonthlyAttendanceSummaryRepository summaryRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    public MonthlyReportServiceImpl(MonthlyAttendanceSummaryRepository summaryRepository,
                                    EmployeeRepository employeeRepository,
                                    LeaveRequestRepository leaveRequestRepository) {
        this.summaryRepository = summaryRepository;
        this.employeeRepository = employeeRepository;
        this.leaveRequestRepository = leaveRequestRepository;
    }

    @Override
    public Page<MonthlySummaryResponse> getMonthlySummaries(String yearMonth, Long departmentId, Pageable pageable) {
        validateYearMonth(yearMonth);

        Page<MonthlyAttendanceSummary> page;
        if (departmentId != null) {
            page = summaryRepository.findByYearMonthAndEmployeeDepartmentId(yearMonth, departmentId, pageable);
        } else {
            page = summaryRepository.findByYearMonth(yearMonth, pageable);
        }
        return page.map(MonthlySummaryResponse::from);
    }

    @Override
    @Transactional
    public void generateSummaries(String yearMonth) {
        validateYearMonth(yearMonth);

        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd = ym.atEndOfMonth();

        List<Employee> employees = employeeRepository.findAllWithDepartment();

        Map<Long, List<LeaveRequest>> leavesByEmployee = leaveRequestRepository
                .findAllApprovedPaidLeavesInMonth(monthStart, monthEnd)
                .stream()
                .collect(Collectors.groupingBy(lr -> lr.getEmployee().getId()));

        for (Employee employee : employees) {
            List<LeaveRequest> leaves = leavesByEmployee.getOrDefault(employee.getId(), Collections.emptyList());
            BigDecimal paidLeaveDays = calculatePaidLeaveDays(leaves, monthStart, monthEnd);

            var existing = summaryRepository.findByEmployeeIdAndYearMonth(employee.getId(), yearMonth);

            if (existing.isPresent()) {
                existing.get().updateSummary(0, 0, 0, 0, paidLeaveDays, 0);
            } else {
                var summary = MonthlyAttendanceSummary.builder()
                        .employee(employee)
                        .yearMonth(yearMonth)
                        .totalWorkMinutes(0)
                        .overtimeMinutes(0)
                        .nightMinutes(0)
                        .holidayWorkMinutes(0)
                        .paidLeaveDays(paidLeaveDays)
                        .workingDays(0)
                        .build();
                summaryRepository.save(summary);
            }
        }
    }

    private BigDecimal calculatePaidLeaveDays(List<LeaveRequest> leaves, LocalDate monthStart, LocalDate monthEnd) {
        BigDecimal total = BigDecimal.ZERO;
        for (LeaveRequest leave : leaves) {
            long daysInMonth = countWeekdaysInRange(
                    maxDate(leave.getStartDate(), monthStart),
                    minDate(leave.getEndDate(), monthEnd));
            if (leave.getLeaveType() == LeaveType.HALF_AM || leave.getLeaveType() == LeaveType.HALF_PM) {
                total = total.add(new BigDecimal("0.5").multiply(BigDecimal.valueOf(daysInMonth)));
            } else {
                total = total.add(BigDecimal.valueOf(daysInMonth));
            }
        }
        return total;
    }

    private long countWeekdaysInRange(LocalDate start, LocalDate end) {
        long count = 0;
        LocalDate date = start;
        while (!date.isAfter(end)) {
            DayOfWeek dow = date.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                count++;
            }
            date = date.plusDays(1);
        }
        return count;
    }

    private LocalDate maxDate(LocalDate a, LocalDate b) {
        return a.isAfter(b) ? a : b;
    }

    private LocalDate minDate(LocalDate a, LocalDate b) {
        return a.isBefore(b) ? a : b;
    }

    private void validateYearMonth(String yearMonth) {
        if (yearMonth == null || !yearMonth.matches("^\\d{4}-(0[1-9]|1[0-2])$")) {
            throw new IllegalArgumentException("yearMonth の形式が不正です（YYYY-MM）");
        }
        try {
            YearMonth.parse(yearMonth);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("yearMonth の形式が不正です（YYYY-MM）");
        }
    }
}
