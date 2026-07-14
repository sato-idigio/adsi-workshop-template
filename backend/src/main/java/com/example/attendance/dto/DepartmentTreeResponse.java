package com.example.attendance.dto;

import java.util.List;

public record DepartmentTreeResponse(Long id, String name, Integer level, List<DepartmentTreeResponse> children) {
}
