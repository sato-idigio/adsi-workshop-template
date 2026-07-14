package com.example.attendance.service;

import com.example.attendance.dto.EmployeeCreateRequest;
import com.example.attendance.dto.EmployeeResponse;
import com.example.attendance.dto.EmployeeUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {

    Page<EmployeeResponse> findAll(Long departmentId, String keyword, Pageable pageable);

    EmployeeResponse findById(Long id);

    EmployeeResponse create(EmployeeCreateRequest request);

    EmployeeResponse update(Long id, EmployeeUpdateRequest request);
}
