package com.example.attendance.service.impl;

import com.example.attendance.dto.DepartmentCreateRequest;
import com.example.attendance.dto.DepartmentResponse;
import com.example.attendance.dto.DepartmentTreeResponse;
import com.example.attendance.dto.DepartmentUpdateRequest;
import com.example.attendance.entity.Department;
import com.example.attendance.exception.ResourceNotFoundException;
import com.example.attendance.repository.DepartmentRepository;
import com.example.attendance.service.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public List<DepartmentTreeResponse> findAllAsTree() {
        List<Department> all = departmentRepository.findAll();
        Map<Long, List<Department>> byParent = all.stream()
                .filter(d -> d.getParent() != null)
                .collect(Collectors.groupingBy(d -> d.getParent().getId()));

        List<Department> roots = all.stream()
                .filter(d -> d.getParent() == null)
                .toList();

        return roots.stream()
                .map(root -> buildTree(root, byParent))
                .toList();
    }

    @Override
    @Transactional
    public DepartmentResponse create(DepartmentCreateRequest request) {
        Department parent = null;
        int level = 1;

        if (request.parentId() != null) {
            parent = departmentRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("親部署が見つかりません: ID=" + request.parentId()));
            level = parent.getLevel() + 1;
            if (level > 3) {
                throw new IllegalArgumentException("部署階層は3レベルまでです");
            }
        }

        Department department = Department.builder()
                .name(request.name())
                .parent(parent)
                .level(level)
                .build();

        Department saved = departmentRepository.save(department);
        return DepartmentResponse.from(saved);
    }

    @Override
    @Transactional
    public DepartmentResponse update(Long id, DepartmentUpdateRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("部署が見つかりません: ID=" + id));

        Department parent = null;
        int level = 1;

        if (request.parentId() != null) {
            parent = departmentRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("親部署が見つかりません: ID=" + request.parentId()));
            level = parent.getLevel() + 1;
            if (level > 3) {
                throw new IllegalArgumentException("部署階層は3レベルまでです");
            }
        }

        Department updated = Department.builder()
                .id(department.getId())
                .name(request.name())
                .parent(parent)
                .level(level)
                .version(request.version())
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .build();

        Department saved = departmentRepository.save(updated);
        return DepartmentResponse.from(saved);
    }

    private DepartmentTreeResponse buildTree(Department dept, Map<Long, List<Department>> byParent) {
        List<Department> children = byParent.getOrDefault(dept.getId(), new ArrayList<>());
        List<DepartmentTreeResponse> childResponses = children.stream()
                .map(child -> buildTree(child, byParent))
                .toList();
        return new DepartmentTreeResponse(dept.getId(), dept.getName(), dept.getLevel(), childResponses);
    }
}
