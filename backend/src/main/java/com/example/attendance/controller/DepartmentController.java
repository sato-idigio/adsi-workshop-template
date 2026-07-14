package com.example.attendance.controller;

import com.example.attendance.dto.DepartmentCreateRequest;
import com.example.attendance.dto.DepartmentResponse;
import com.example.attendance.dto.DepartmentTreeResponse;
import com.example.attendance.dto.DepartmentUpdateRequest;
import com.example.attendance.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public ResponseEntity<List<DepartmentTreeResponse>> list() {
        List<DepartmentTreeResponse> tree = departmentService.findAllAsTree();
        return ResponseEntity.ok(tree);
    }

    @PostMapping
    public ResponseEntity<DepartmentResponse> create(@Valid @RequestBody DepartmentCreateRequest request) {
        DepartmentResponse response = departmentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody DepartmentUpdateRequest request) {
        DepartmentResponse response = departmentService.update(id, request);
        return ResponseEntity.ok(response);
    }
}
