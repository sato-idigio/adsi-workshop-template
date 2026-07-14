package com.example.attendance.dto;

import com.example.attendance.entity.Department;

public record DepartmentResponse(Long id, String name, Integer level, Long parentId) {

    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getLevel(),
                department.getParentId()
        );
    }
}
