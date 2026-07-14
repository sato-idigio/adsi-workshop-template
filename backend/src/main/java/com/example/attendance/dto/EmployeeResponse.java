package com.example.attendance.dto;

import com.example.attendance.entity.Employee;

import java.time.LocalDate;

public record EmployeeResponse(
        Long id,
        String employeeNumber,
        String lastName,
        String firstName,
        String email,
        DepartmentResponse department,
        String position,
        String role,
        LocalDate hireDate
) {
    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getEmployeeNumber(),
                employee.getLastName(),
                employee.getFirstName(),
                employee.getEmail(),
                DepartmentResponse.from(employee.getDepartment()),
                employee.getPosition(),
                employee.getRole(),
                employee.getHireDate()
        );
    }
}
