package com.example.attendance.service;

import com.example.attendance.dto.CorrectionRequestCreate;
import com.example.attendance.dto.CorrectionRequestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AttendanceCorrectionService {

    CorrectionRequestResponse create(String employeeNumber, CorrectionRequestCreate request);

    Page<CorrectionRequestResponse> list(String employeeNumber, String role, String status, Pageable pageable);

    CorrectionRequestResponse approve(String approverEmployeeNumber, Long requestId);

    CorrectionRequestResponse reject(String approverEmployeeNumber, Long requestId);
}
