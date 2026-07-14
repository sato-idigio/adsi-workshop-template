package com.example.attendance.service.impl;

import com.example.attendance.dto.EmployeeCreateRequest;
import com.example.attendance.dto.EmployeeResponse;
import com.example.attendance.dto.EmployeeUpdateRequest;
import com.example.attendance.entity.Department;
import com.example.attendance.entity.Employee;
import com.example.attendance.exception.DuplicateResourceException;
import com.example.attendance.exception.ResourceNotFoundException;
import com.example.attendance.repository.DepartmentRepository;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               DepartmentRepository departmentRepository,
                               PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Page<EmployeeResponse> findAll(Long departmentId, String keyword, Pageable pageable) {
        if (departmentId != null && keyword != null && !keyword.isBlank()) {
            return employeeRepository.searchByDepartmentOrKeyword(departmentId, keyword, pageable)
                    .map(EmployeeResponse::from);
        }
        if (departmentId != null) {
            return employeeRepository.findByDepartmentId(departmentId, pageable)
                    .map(EmployeeResponse::from);
        }
        if (keyword != null && !keyword.isBlank()) {
            return employeeRepository.searchByKeyword(keyword, pageable)
                    .map(EmployeeResponse::from);
        }
        return employeeRepository.findAll(pageable).map(EmployeeResponse::from);
    }

    @Override
    public EmployeeResponse findById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("社員が見つかりません: ID=" + id));
        return EmployeeResponse.from(employee);
    }

    @Override
    @Transactional
    public EmployeeResponse create(EmployeeCreateRequest request) {
        if (employeeRepository.existsByEmployeeNumber(request.employeeNumber())) {
            throw new DuplicateResourceException("社員番号が既に使用されています: " + request.employeeNumber());
        }
        if (employeeRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("メールアドレスが既に使用されています: " + request.email());
        }

        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("部署が見つかりません: ID=" + request.departmentId()));

        Employee employee = Employee.builder()
                .employeeNumber(request.employeeNumber())
                .lastName(request.lastName())
                .firstName(request.firstName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .department(department)
                .position(request.position())
                .role(request.role())
                .hireDate(request.hireDate())
                .build();

        Employee saved = employeeRepository.save(employee);
        return EmployeeResponse.from(saved);
    }

    @Override
    @Transactional
    public EmployeeResponse update(Long id, EmployeeUpdateRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("社員が見つかりません: ID=" + id));

        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("部署が見つかりません: ID=" + request.departmentId()));

        employee.updateInfo(
                request.lastName(),
                request.firstName(),
                request.email(),
                department,
                request.position(),
                request.role(),
                request.hireDate()
        );

        Employee saved = employeeRepository.save(employee);
        return EmployeeResponse.from(saved);
    }
}
