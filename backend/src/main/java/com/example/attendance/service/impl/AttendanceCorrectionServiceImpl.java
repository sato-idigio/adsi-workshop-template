package com.example.attendance.service.impl;

import com.example.attendance.dto.CorrectionRequestCreate;
import com.example.attendance.dto.CorrectionRequestResponse;
import com.example.attendance.entity.AttendanceCorrectionRequest;
import com.example.attendance.exception.DuplicateResourceException;
import com.example.attendance.exception.ResourceNotFoundException;
import com.example.attendance.repository.AttendanceCorrectionRequestRepository;
import com.example.attendance.repository.AttendanceRecordRepository;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.service.AttendanceCorrectionService;
import com.example.attendance.util.WorkTimeCalculator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceCorrectionServiceImpl implements AttendanceCorrectionService {

    private final AttendanceCorrectionRequestRepository correctionRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EmployeeRepository employeeRepository;

    public AttendanceCorrectionServiceImpl(
            AttendanceCorrectionRequestRepository correctionRepository,
            AttendanceRecordRepository attendanceRecordRepository,
            EmployeeRepository employeeRepository) {
        this.correctionRepository = correctionRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public CorrectionRequestResponse create(String employeeNumber, CorrectionRequestCreate request) {
        var employee = employeeRepository.findByEmployeeNumber(employeeNumber)
                .orElseThrow(() -> new ResourceNotFoundException("社員が見つかりません"));

        var record = attendanceRecordRepository.findById(request.attendanceRecordId())
                .orElseThrow(() -> new ResourceNotFoundException("打刻レコードが見つかりません"));

        correctionRepository.findByAttendanceRecordIdAndStatus(request.attendanceRecordId(), "PENDING")
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("このレコードには承認待ちの申請が既にあります");
                });

        var correction = AttendanceCorrectionRequest.builder()
                .employee(employee)
                .attendanceRecord(record)
                .requestedClockIn(request.requestedClockIn())
                .requestedClockOut(request.requestedClockOut())
                .reason(request.reason())
                .build();

        var saved = correctionRepository.save(correction);
        return CorrectionRequestResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CorrectionRequestResponse> list(String employeeNumber, String role, String status, Pageable pageable) {
        var employee = employeeRepository.findByEmployeeNumber(employeeNumber)
                .orElseThrow(() -> new ResourceNotFoundException("社員が見つかりません"));

        Page<AttendanceCorrectionRequest> page;
        if ("ADMIN".equals(role)) {
            page = (status != null)
                    ? correctionRepository.findByStatus(status, pageable)
                    : correctionRepository.findAllOrderByCreatedAtDesc(pageable);
        } else {
            page = correctionRepository.findByEmployeeId(employee.getId(), pageable);
        }

        return page.map(CorrectionRequestResponse::from);
    }

    @Override
    @Transactional
    public CorrectionRequestResponse approve(String approverEmployeeNumber, Long requestId) {
        var approver = employeeRepository.findByEmployeeNumber(approverEmployeeNumber)
                .orElseThrow(() -> new ResourceNotFoundException("承認者が見つかりません"));

        var correction = correctionRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("申請が見つかりません"));

        if (correction.getEmployee().getId().equals(approver.getId())) {
            throw new IllegalArgumentException("自分の申請を承認することはできません");
        }

        correction.approve(approver);

        var record = correction.getAttendanceRecord();
        var newClockIn = correction.getRequestedClockIn() != null
                ? correction.getRequestedClockIn() : record.getClockIn();
        var newClockOut = correction.getRequestedClockOut() != null
                ? correction.getRequestedClockOut() : record.getClockOut();

        record.recordClockIn(newClockIn);
        if (newClockOut != null) {
            var result = WorkTimeCalculator.calculate(newClockIn, newClockOut);
            record.recordClockOut(newClockOut, result.workMinutes(), result.overtimeMinutes(), result.nightMinutes());
        }

        attendanceRecordRepository.save(record);
        var saved = correctionRepository.save(correction);
        return CorrectionRequestResponse.from(saved);
    }

    @Override
    @Transactional
    public CorrectionRequestResponse reject(String approverEmployeeNumber, Long requestId) {
        var approver = employeeRepository.findByEmployeeNumber(approverEmployeeNumber)
                .orElseThrow(() -> new ResourceNotFoundException("承認者が見つかりません"));

        var correction = correctionRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("申請が見つかりません"));

        if (correction.getEmployee().getId().equals(approver.getId())) {
            throw new IllegalArgumentException("自分の申請を却下することはできません");
        }

        correction.reject(approver);
        var saved = correctionRepository.save(correction);
        return CorrectionRequestResponse.from(saved);
    }
}
