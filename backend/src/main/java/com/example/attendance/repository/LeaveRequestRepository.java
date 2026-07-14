package com.example.attendance.repository;

import com.example.attendance.entity.LeaveRequest;
import com.example.attendance.entity.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    @EntityGraph(attributePaths = "employee")
    Page<LeaveRequest> findByEmployeeId(Long employeeId, Pageable pageable);

    @EntityGraph(attributePaths = "employee")
    Page<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, ApprovalStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "employee")
    Page<LeaveRequest> findByStatus(ApprovalStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "employee")
    Page<LeaveRequest> findAll(Pageable pageable);

    @Query("SELECT lr FROM LeaveRequest lr JOIN FETCH lr.employee WHERE lr.id = :id")
    java.util.Optional<LeaveRequest> findByIdWithEmployee(@Param("id") Long id);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId " +
           "AND lr.status = com.example.attendance.entity.enums.ApprovalStatus.APPROVED " +
           "AND lr.leaveType IN (com.example.attendance.entity.enums.LeaveType.PAID, " +
           "com.example.attendance.entity.enums.LeaveType.HALF_AM, " +
           "com.example.attendance.entity.enums.LeaveType.HALF_PM) " +
           "AND lr.startDate <= :monthEnd AND lr.endDate >= :monthStart")
    List<LeaveRequest> findApprovedPaidLeavesInMonth(
            @Param("employeeId") Long employeeId,
            @Param("monthStart") LocalDate monthStart,
            @Param("monthEnd") LocalDate monthEnd);
}
