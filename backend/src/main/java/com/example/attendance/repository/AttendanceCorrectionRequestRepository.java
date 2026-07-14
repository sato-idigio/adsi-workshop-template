package com.example.attendance.repository;

import com.example.attendance.entity.AttendanceCorrectionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AttendanceCorrectionRequestRepository extends JpaRepository<AttendanceCorrectionRequest, Long> {

    @Query("SELECT c FROM AttendanceCorrectionRequest c WHERE c.employee.id = :employeeId ORDER BY c.createdAt DESC")
    Page<AttendanceCorrectionRequest> findByEmployeeId(@Param("employeeId") Long employeeId, Pageable pageable);

    @Query("SELECT c FROM AttendanceCorrectionRequest c WHERE c.status = :status ORDER BY c.createdAt DESC")
    Page<AttendanceCorrectionRequest> findByStatus(@Param("status") String status, Pageable pageable);

    @Query("SELECT c FROM AttendanceCorrectionRequest c ORDER BY c.createdAt DESC")
    Page<AttendanceCorrectionRequest> findAllOrderByCreatedAtDesc(Pageable pageable);

    Optional<AttendanceCorrectionRequest> findByAttendanceRecordIdAndStatus(Long attendanceRecordId, String status);
}
