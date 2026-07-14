package com.example.attendance.service;

import com.example.attendance.dto.DepartmentCreateRequest;
import com.example.attendance.dto.DepartmentResponse;
import com.example.attendance.dto.DepartmentTreeResponse;
import com.example.attendance.dto.DepartmentUpdateRequest;

import java.util.List;

public interface DepartmentService {

    List<DepartmentTreeResponse> findAllAsTree();

    DepartmentResponse create(DepartmentCreateRequest request);

    DepartmentResponse update(Long id, DepartmentUpdateRequest request);
}
