package com.example.attendance.repository;

import com.example.attendance.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findByParentIsNull();

    List<Department> findByParent_Id(Long parentId);

    List<Department> findByLevel(Integer level);
}
