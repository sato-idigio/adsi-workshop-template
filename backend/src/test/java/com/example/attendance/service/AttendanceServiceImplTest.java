package com.example.attendance.service;

import com.example.attendance.entity.AttendanceRecord;
import com.example.attendance.entity.Department;
import com.example.attendance.entity.Employee;
import com.example.attendance.exception.DuplicateResourceException;
import com.example.attendance.exception.ResourceNotFoundException;
import com.example.attendance.repository.AttendanceRecordRepository;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.service.impl.AttendanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTest {

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private AttendanceServiceImpl service;

    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        service = new AttendanceServiceImpl(attendanceRecordRepository, employeeRepository);
        var dept = Department.builder().id(1L).name("開発部").build();
        testEmployee = Employee.builder()
                .id(1L)
                .employeeNumber("EMP001")
                .lastName("田中")
                .firstName("太郎")
                .email("tanaka@example.com")
                .password("encoded")
                .department(dept)
                .role("EMPLOYEE")
                .hireDate(LocalDate.of(2020, 4, 1))
                .build();
    }

    @Test
    @DisplayName("出勤打刻: 当日未打刻の場合、出勤時刻が記録される")
    void clockIn_notYetClockedIn_recordsClockInTime() {
        when(employeeRepository.findByEmployeeNumber("EMP001"))
                .thenReturn(Optional.of(testEmployee));
        when(attendanceRecordRepository.findByEmployeeIdAndDate(1L, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(attendanceRecordRepository.save(any(AttendanceRecord.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var result = service.clockIn("EMP001");

        assertThat(result.clockIn()).isNotNull();
        assertThat(result.date()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("出勤打刻: 既に出勤済みの場合、DuplicateResourceExceptionが発生する")
    void clockIn_alreadyClockedIn_throwsDuplicate() {
        when(employeeRepository.findByEmployeeNumber("EMP001"))
                .thenReturn(Optional.of(testEmployee));
        var existingRecord = AttendanceRecord.builder()
                .id(1L)
                .employee(testEmployee)
                .date(LocalDate.now())
                .clockIn(LocalDateTime.now().minusHours(2))
                .build();
        when(attendanceRecordRepository.findByEmployeeIdAndDate(1L, LocalDate.now()))
                .thenReturn(Optional.of(existingRecord));

        assertThatThrownBy(() -> service.clockIn("EMP001"))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("退勤打刻: 出勤済みで未退勤の場合、退勤時刻と勤務時間が記録される")
    void clockOut_clockedInNotOut_recordsClockOutAndTime() {
        when(employeeRepository.findByEmployeeNumber("EMP001"))
                .thenReturn(Optional.of(testEmployee));
        var record = AttendanceRecord.builder()
                .id(1L)
                .employee(testEmployee)
                .date(LocalDate.now())
                .clockIn(LocalDateTime.now().minusHours(8))
                .build();
        when(attendanceRecordRepository.findByEmployeeIdAndDate(1L, LocalDate.now()))
                .thenReturn(Optional.of(record));
        when(attendanceRecordRepository.save(any(AttendanceRecord.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var result = service.clockOut("EMP001");

        assertThat(result.clockOut()).isNotNull();
        assertThat(result.workMinutes()).isNotNull();
    }

    @Test
    @DisplayName("退勤打刻: 出勤打刻がない場合、ResourceNotFoundExceptionが発生する")
    void clockOut_noClockedIn_throwsNotFound() {
        when(employeeRepository.findByEmployeeNumber("EMP001"))
                .thenReturn(Optional.of(testEmployee));
        when(attendanceRecordRepository.findByEmployeeIdAndDate(1L, LocalDate.now()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.clockOut("EMP001"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("退勤打刻: 既に退勤済みの場合、DuplicateResourceExceptionが発生する")
    void clockOut_alreadyClockedOut_throwsDuplicate() {
        when(employeeRepository.findByEmployeeNumber("EMP001"))
                .thenReturn(Optional.of(testEmployee));
        var record = AttendanceRecord.builder()
                .id(1L)
                .employee(testEmployee)
                .date(LocalDate.now())
                .clockIn(LocalDateTime.now().minusHours(8))
                .clockOut(LocalDateTime.now())
                .build();
        when(attendanceRecordRepository.findByEmployeeIdAndDate(1L, LocalDate.now()))
                .thenReturn(Optional.of(record));

        assertThatThrownBy(() -> service.clockOut("EMP001"))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("月次一覧: 指定年月のレコードがリストで返される")
    void getMonthly_validYearMonth_returnsList() {
        when(employeeRepository.findByEmployeeNumber("EMP001"))
                .thenReturn(Optional.of(testEmployee));
        var record = AttendanceRecord.builder()
                .id(1L)
                .employee(testEmployee)
                .date(LocalDate.of(2026, 7, 1))
                .clockIn(LocalDateTime.of(2026, 7, 1, 9, 0))
                .clockOut(LocalDateTime.of(2026, 7, 1, 17, 0))
                .workMinutes(480)
                .overtimeMinutes(0)
                .nightMinutes(0)
                .build();
        when(attendanceRecordRepository.findByEmployeeIdAndDateBetween(
                1L, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31)))
                .thenReturn(List.of(record));

        var result = service.getMonthly("EMP001", "2026-07");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).date()).isEqualTo(LocalDate.of(2026, 7, 1));
    }
}
