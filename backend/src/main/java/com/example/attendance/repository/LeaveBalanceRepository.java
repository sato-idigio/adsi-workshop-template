package com.example.attendance.repository;

import com.example.attendance.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    Optional<LeaveBalance> findByEmployeeIdAndFiscalYear(Long employeeId, Integer fiscalYear);
}
