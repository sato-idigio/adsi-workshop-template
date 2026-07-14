package com.example.attendance.repository;

import com.example.attendance.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @EntityGraph(attributePaths = "department")
    @Query("SELECT e FROM Employee e")
    List<Employee> findAllWithDepartment();

    Optional<Employee> findByEmployeeNumber(String employeeNumber);

    Optional<Employee> findByEmail(String email);

    Page<Employee> findByDepartmentId(Long departmentId, Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE e.department.id = :departmentId " +
           "OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(e.employeeNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Employee> searchByDepartmentOrKeyword(@Param("departmentId") Long departmentId,
                                               @Param("keyword") String keyword,
                                               Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE " +
           "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(e.employeeNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Employee> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByEmployeeNumber(String employeeNumber);

    boolean existsByEmail(String email);
}
