package com.example.attendance.service.impl;

import com.example.attendance.dto.EmployeeResponse;
import com.example.attendance.dto.LoginRequest;
import com.example.attendance.dto.LoginResponse;
import com.example.attendance.entity.Employee;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.service.AuthService;
import com.example.attendance.util.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(EmployeeRepository employeeRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Employee employee = employeeRepository.findByEmployeeNumber(request.employeeNumber())
                .orElseThrow(() -> new BadCredentialsException("社員番号またはパスワードが正しくありません"));

        if (!passwordEncoder.matches(request.password(), employee.getPassword())) {
            throw new BadCredentialsException("社員番号またはパスワードが正しくありません");
        }

        String token = jwtUtil.generateToken(employee.getEmployeeNumber(), employee.getRole());
        return new LoginResponse(token, EmployeeResponse.from(employee));
    }

    @Override
    public EmployeeResponse getCurrentUser(String employeeNumber) {
        Employee employee = employeeRepository.findByEmployeeNumber(employeeNumber)
                .orElseThrow(() -> new BadCredentialsException("ユーザーが見つかりません"));
        return EmployeeResponse.from(employee);
    }
}
