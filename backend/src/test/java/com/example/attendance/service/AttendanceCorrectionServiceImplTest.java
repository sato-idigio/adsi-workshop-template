package com.example.attendance.service;

import com.example.attendance.dto.CorrectionRequestCreate;
import com.example.attendance.entity.AttendanceCorrectionRequest;
import com.example.attendance.entity.AttendanceRecord;
import com.example.attendance.entity.Department;
import com.example.attendance.entity.Employee;
import com.example.attendance.exception.DuplicateResourceException;
import com.example.attendance.exception.ResourceNotFoundException;
import com.example.attendance.repository.AttendanceCorrectionRequestRepository;
import com.example.attendance.repository.AttendanceRecordRepository;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.service.impl.AttendanceCorrectionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceCorrectionServiceImplTest {

    @Mock
    private AttendanceCorrectionRequestRepository correctionRepository;

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private AttendanceCorrectionServiceImpl service;

    private Employee employee;
    private Employee admin;
    private AttendanceRecord attendanceRecord;

    @BeforeEach
    void setUp() {
        service = new AttendanceCorrectionServiceImpl(
                correctionRepository, attendanceRecordRepository, employeeRepository);

        var dept = Department.builder().id(1L).name("開発部").build();
        employee = Employee.builder()
                .id(1L).employeeNumber("EMP001").lastName("田中").firstName("太郎")
                .email("tanaka@example.com").password("encoded").department(dept)
                .role("EMPLOYEE").hireDate(LocalDate.of(2020, 4, 1)).build();
        admin = Employee.builder()
                .id(2L).employeeNumber("EMP002").lastName("管理者").firstName("太郎")
                .email("admin@example.com").password("encoded").department(dept)
                .role("ADMIN").hireDate(LocalDate.of(2019, 4, 1)).build();
        attendanceRecord = AttendanceRecord.builder()
                .id(10L).employee(employee).date(LocalDate.of(2026, 7, 14))
                .clockIn(LocalDateTime.of(2026, 7, 14, 9, 0))
                .clockOut(LocalDateTime.of(2026, 7, 14, 17, 0))
                .workMinutes(480).overtimeMinutes(0).nightMinutes(0).build();
    }

    @Test
    @DisplayName("修正申請: 正常に作成される")
    void create_valid_returnsResponse() {
        when(employeeRepository.findByEmployeeNumber("EMP001")).thenReturn(Optional.of(employee));
        when(attendanceRecordRepository.findById(10L)).thenReturn(Optional.of(attendanceRecord));
        when(correctionRepository.findByAttendanceRecordIdAndStatus(10L, "PENDING"))
                .thenReturn(Optional.empty());
        when(correctionRepository.save(any(AttendanceCorrectionRequest.class)))
                .thenAnswer(inv -> {
                    var req = (AttendanceCorrectionRequest) inv.getArgument(0);
                    return AttendanceCorrectionRequest.builder()
                            .id(1L).employee(req.getEmployee())
                            .attendanceRecord(req.getAttendanceRecord())
                            .requestedClockIn(req.getRequestedClockIn())
                            .requestedClockOut(req.getRequestedClockOut())
                            .reason(req.getReason()).status("PENDING").build();
                });

        var request = new CorrectionRequestCreate(
                10L,
                LocalDateTime.of(2026, 7, 14, 8, 30),
                LocalDateTime.of(2026, 7, 14, 18, 0),
                "出勤時刻を修正");

        var result = service.create("EMP001", request);

        assertThat(result.status()).isEqualTo("PENDING");
        assertThat(result.reason()).isEqualTo("出勤時刻を修正");
    }

    @Test
    @DisplayName("修正申請: 存在しないレコードで404")
    void create_recordNotFound_throwsNotFound() {
        when(employeeRepository.findByEmployeeNumber("EMP001")).thenReturn(Optional.of(employee));
        when(attendanceRecordRepository.findById(999L)).thenReturn(Optional.empty());

        var request = new CorrectionRequestCreate(999L, null, null, "修正理由");

        assertThatThrownBy(() -> service.create("EMP001", request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("修正申請: 同一レコードにPENDING申請がある場合は重複エラー")
    void create_duplicatePending_throwsDuplicate() {
        when(employeeRepository.findByEmployeeNumber("EMP001")).thenReturn(Optional.of(employee));
        when(attendanceRecordRepository.findById(10L)).thenReturn(Optional.of(attendanceRecord));
        var existing = AttendanceCorrectionRequest.builder()
                .id(1L).employee(employee).attendanceRecord(attendanceRecord)
                .status("PENDING").reason("既存申請").build();
        when(correctionRepository.findByAttendanceRecordIdAndStatus(10L, "PENDING"))
                .thenReturn(Optional.of(existing));

        var request = new CorrectionRequestCreate(10L, null, null, "二重申請");

        assertThatThrownBy(() -> service.create("EMP001", request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("承認: AttendanceRecordが更新され勤務時間が再計算される")
    void approve_valid_updatesAttendanceRecord() {
        var correction = AttendanceCorrectionRequest.builder()
                .id(1L).employee(employee).attendanceRecord(attendanceRecord)
                .requestedClockIn(LocalDateTime.of(2026, 7, 14, 8, 30))
                .requestedClockOut(LocalDateTime.of(2026, 7, 14, 18, 30))
                .reason("修正").status("PENDING").build();

        when(employeeRepository.findByEmployeeNumber("EMP002")).thenReturn(Optional.of(admin));
        when(correctionRepository.findById(1L)).thenReturn(Optional.of(correction));
        when(correctionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(attendanceRecordRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.approve("EMP002", 1L);

        assertThat(result.status()).isEqualTo("APPROVED");
        verify(attendanceRecordRepository).save(any(AttendanceRecord.class));
    }

    @Test
    @DisplayName("承認: 自己承認は禁止")
    void approve_selfApproval_throwsException() {
        var correction = AttendanceCorrectionRequest.builder()
                .id(1L).employee(employee).attendanceRecord(attendanceRecord)
                .reason("修正").status("PENDING").build();

        when(employeeRepository.findByEmployeeNumber("EMP001")).thenReturn(Optional.of(employee));
        when(correctionRepository.findById(1L)).thenReturn(Optional.of(correction));

        assertThatThrownBy(() -> service.approve("EMP001", 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("却下: ステータスがREJECTEDに変更される")
    void reject_valid_changesStatusToRejected() {
        var correction = AttendanceCorrectionRequest.builder()
                .id(1L).employee(employee).attendanceRecord(attendanceRecord)
                .reason("修正").status("PENDING").build();

        when(employeeRepository.findByEmployeeNumber("EMP002")).thenReturn(Optional.of(admin));
        when(correctionRepository.findById(1L)).thenReturn(Optional.of(correction));
        when(correctionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.reject("EMP002", 1L);

        assertThat(result.status()).isEqualTo("REJECTED");
    }
}
