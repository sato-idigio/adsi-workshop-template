package com.example.attendance.repository;

import com.example.attendance.entity.LeaveRequest;
import com.example.attendance.entity.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    Page<LeaveRequest> findByEmployeeId(Long employeeId, Pageable pageable);

    Page<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, ApprovalStatus status, Pageable pageable);

    Page<LeaveRequest> findByStatus(ApprovalStatus status, Pageable pageable);

    @Query("SELECT lr FROM LeaveRequest lr JOIN FETCH lr.employee WHERE lr.id = :id")
    java.util.Optional<LeaveRequest> findByIdWithEmployee(@Param("id") Long id);
}
