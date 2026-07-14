package com.example.attendance.service.impl;

import com.example.attendance.dto.AttendanceRecordResponse;
import com.example.attendance.entity.AttendanceRecord;
import com.example.attendance.exception.DuplicateResourceException;
import com.example.attendance.exception.ResourceNotFoundException;
import com.example.attendance.repository.AttendanceRecordRepository;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.util.WorkTimeCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private static final DateTimeFormatter YEAR_MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EmployeeRepository employeeRepository;

    public AttendanceServiceImpl(AttendanceRecordRepository attendanceRecordRepository,
                                 EmployeeRepository employeeRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public AttendanceRecordResponse clockIn(String employeeNumber) {
        var employee = employeeRepository.findByEmployeeNumber(employeeNumber)
                .orElseThrow(() -> new ResourceNotFoundException("社員が見つかりません"));

        var today = LocalDate.now();
        var existing = attendanceRecordRepository.findByEmployeeIdAndDate(employee.getId(), today);
        if (existing.isPresent()) {
            throw new DuplicateResourceException("本日は既に出勤打刻済みです");
        }

        var record = AttendanceRecord.builder()
                .employee(employee)
                .date(today)
                .clockIn(LocalDateTime.now())
                .build();

        var saved = attendanceRecordRepository.save(record);
        return AttendanceRecordResponse.from(saved);
    }

    @Override
    public AttendanceRecordResponse clockOut(String employeeNumber) {
        var employee = employeeRepository.findByEmployeeNumber(employeeNumber)
                .orElseThrow(() -> new ResourceNotFoundException("社員が見つかりません"));

        var today = LocalDate.now();
        var record = attendanceRecordRepository.findByEmployeeIdAndDate(employee.getId(), today)
                .orElseThrow(() -> new ResourceNotFoundException("本日の出勤打刻がありません"));

        if (record.getClockOut() != null) {
            throw new DuplicateResourceException("本日は既に退勤打刻済みです");
        }

        var clockOut = LocalDateTime.now();
        var result = WorkTimeCalculator.calculate(record.getClockIn(), clockOut);
        record.recordClockOut(clockOut, result.workMinutes(), result.overtimeMinutes(), result.nightMinutes());

        var saved = attendanceRecordRepository.save(record);
        return AttendanceRecordResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceRecordResponse getToday(String employeeNumber) {
        var employee = employeeRepository.findByEmployeeNumber(employeeNumber)
                .orElseThrow(() -> new ResourceNotFoundException("社員が見つかりません"));

        return attendanceRecordRepository.findByEmployeeIdAndDate(employee.getId(), LocalDate.now())
                .map(AttendanceRecordResponse::from)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> getMonthly(String employeeNumber, String yearMonth) {
        var employee = employeeRepository.findByEmployeeNumber(employeeNumber)
                .orElseThrow(() -> new ResourceNotFoundException("社員が見つかりません"));

        var ym = YearMonth.parse(yearMonth, YEAR_MONTH_FORMAT);
        var startDate = ym.atDay(1);
        var endDate = ym.atEndOfMonth();

        return attendanceRecordRepository.findByEmployeeIdAndDateBetween(
                        employee.getId(), startDate, endDate)
                .stream()
                .map(AttendanceRecordResponse::from)
                .toList();
    }
}
