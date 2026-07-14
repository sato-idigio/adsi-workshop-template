package com.example.attendance.repository;

import com.example.attendance.entity.MonthlyAttendanceSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MonthlyAttendanceSummaryRepository extends JpaRepository<MonthlyAttendanceSummary, Long> {

    @EntityGraph(attributePaths = {"employee", "employee.department"})
    Page<MonthlyAttendanceSummary> findByYearMonth(String yearMonth, Pageable pageable);

    @EntityGraph(attributePaths = {"employee", "employee.department"})
    Page<MonthlyAttendanceSummary> findByYearMonthAndEmployeeDepartmentId(
            String yearMonth, Long departmentId, Pageable pageable);

    Optional<MonthlyAttendanceSummary> findByEmployeeIdAndYearMonth(Long employeeId, String yearMonth);
}
