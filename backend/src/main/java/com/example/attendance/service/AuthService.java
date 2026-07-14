package com.example.attendance.service;

import com.example.attendance.dto.LoginRequest;
import com.example.attendance.dto.LoginResponse;
import com.example.attendance.dto.EmployeeResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    EmployeeResponse getCurrentUser(String employeeNumber);
}
