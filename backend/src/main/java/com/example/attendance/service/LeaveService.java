package com.example.attendance.service;

import com.example.attendance.dto.LeaveBalanceResponse;
import com.example.attendance.dto.LeaveRequestCreate;
import com.example.attendance.dto.LeaveRequestResponse;
import com.example.attendance.entity.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LeaveService {

    LeaveRequestResponse create(LeaveRequestCreate request, String employeeNumber);

    Page<LeaveRequestResponse> findAll(ApprovalStatus status, Pageable pageable);

    Page<LeaveRequestResponse> findByEmployee(String employeeNumber, ApprovalStatus status, Pageable pageable);

    LeaveRequestResponse approve(Long id, String approverEmployeeNumber);

    LeaveRequestResponse reject(Long id, String approverEmployeeNumber);

    LeaveBalanceResponse getBalance(String employeeNumber, Integer fiscalYear);
}
