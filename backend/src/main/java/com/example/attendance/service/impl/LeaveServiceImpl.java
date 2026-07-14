package com.example.attendance.service.impl;

import com.example.attendance.dto.LeaveBalanceResponse;
import com.example.attendance.dto.LeaveRequestCreate;
import com.example.attendance.dto.LeaveRequestResponse;
import com.example.attendance.entity.Employee;
import com.example.attendance.entity.LeaveBalance;
import com.example.attendance.entity.LeaveRequest;
import com.example.attendance.entity.enums.ApprovalStatus;
import com.example.attendance.entity.enums.LeaveType;
import com.example.attendance.exception.InsufficientLeaveBalanceException;
import com.example.attendance.exception.ResourceNotFoundException;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.repository.LeaveBalanceRepository;
import com.example.attendance.repository.LeaveRequestRepository;
import com.example.attendance.service.LeaveService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;

    public LeaveServiceImpl(LeaveRequestRepository leaveRequestRepository,
                            LeaveBalanceRepository leaveBalanceRepository,
                            EmployeeRepository employeeRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public LeaveRequestResponse create(LeaveRequestCreate request, String employeeNumber) {
        Employee employee = findEmployeeByNumber(employeeNumber);
        BigDecimal days = calculateDays(request.leaveType(), request.startDate(), request.endDate());

        if (requiresBalanceCheck(request.leaveType())) {
            int fiscalYear = request.startDate().getYear();
            LeaveBalance balance = leaveBalanceRepository
                    .findByEmployeeIdAndFiscalYear(employee.getId(), fiscalYear)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "有給残日数データが見つかりません（年度: " + fiscalYear + "）"));

            if (balance.getRemainingDays().compareTo(days) < 0) {
                throw new InsufficientLeaveBalanceException(
                        "有給残日数が不足しています（残: " + balance.getRemainingDays() + "日, 申請: " + days + "日）");
            }
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(request.leaveType())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .days(days)
                .reason(request.reason())
                .build();

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return LeaveRequestResponse.from(saved);
    }

    @Override
    public Page<LeaveRequestResponse> findAll(ApprovalStatus status, Pageable pageable) {
        if (status != null) {
            return leaveRequestRepository.findByStatus(status, pageable)
                    .map(LeaveRequestResponse::from);
        }
        return leaveRequestRepository.findAll(pageable).map(LeaveRequestResponse::from);
    }

    @Override
    public Page<LeaveRequestResponse> findByEmployee(String employeeNumber, ApprovalStatus status, Pageable pageable) {
        Employee employee = findEmployeeByNumber(employeeNumber);
        if (status != null) {
            return leaveRequestRepository.findByEmployeeIdAndStatus(employee.getId(), status, pageable)
                    .map(LeaveRequestResponse::from);
        }
        return leaveRequestRepository.findByEmployeeId(employee.getId(), pageable)
                .map(LeaveRequestResponse::from);
    }

    @Override
    @Transactional
    public LeaveRequestResponse approve(Long id, String approverEmployeeNumber) {
        LeaveRequest leaveRequest = leaveRequestRepository.findByIdWithEmployee(id)
                .orElseThrow(() -> new ResourceNotFoundException("休暇申請が見つかりません: ID=" + id));

        if (leaveRequest.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalArgumentException("承認待ち状態の申請のみ承認できます");
        }

        Employee approver = findEmployeeByNumber(approverEmployeeNumber);

        if (leaveRequest.getEmployee().getId().equals(approver.getId())) {
            throw new IllegalArgumentException("自分の申請は承認できません");
        }

        leaveRequest.approve(approver);

        if (requiresBalanceCheck(leaveRequest.getLeaveType())) {
            int fiscalYear = leaveRequest.getStartDate().getYear();
            LeaveBalance balance = leaveBalanceRepository
                    .findByEmployeeIdAndFiscalYear(leaveRequest.getEmployee().getId(), fiscalYear)
                    .orElseThrow(() -> new ResourceNotFoundException("有給残日数データが見つかりません"));
            balance.addUsedDays(leaveRequest.getDays());
            leaveBalanceRepository.save(balance);
        }

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return LeaveRequestResponse.from(saved);
    }

    @Override
    @Transactional
    public LeaveRequestResponse reject(Long id, String approverEmployeeNumber) {
        LeaveRequest leaveRequest = leaveRequestRepository.findByIdWithEmployee(id)
                .orElseThrow(() -> new ResourceNotFoundException("休暇申請が見つかりません: ID=" + id));

        if (leaveRequest.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalArgumentException("承認待ち状態の申請のみ却下できます");
        }

        Employee approver = findEmployeeByNumber(approverEmployeeNumber);
        leaveRequest.reject(approver);

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return LeaveRequestResponse.from(saved);
    }

    @Override
    public LeaveBalanceResponse getBalance(String employeeNumber, Integer fiscalYear) {
        Employee employee = findEmployeeByNumber(employeeNumber);
        int year = fiscalYear != null ? fiscalYear : LocalDate.now().getYear();

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndFiscalYear(employee.getId(), year)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "有給残日数データが見つかりません（年度: " + year + "）"));

        return LeaveBalanceResponse.from(balance);
    }

    private Employee findEmployeeByNumber(String employeeNumber) {
        return employeeRepository.findByEmployeeNumber(employeeNumber)
                .orElseThrow(() -> new ResourceNotFoundException("社員が見つかりません: " + employeeNumber));
    }

    private boolean requiresBalanceCheck(LeaveType leaveType) {
        return leaveType == LeaveType.PAID
                || leaveType == LeaveType.HALF_AM
                || leaveType == LeaveType.HALF_PM;
    }

    BigDecimal calculateDays(LeaveType leaveType, LocalDate startDate, LocalDate endDate) {
        if (leaveType == LeaveType.HALF_AM || leaveType == LeaveType.HALF_PM) {
            return new BigDecimal("0.5");
        }
        return BigDecimal.valueOf(countBusinessDays(startDate, endDate));
    }

    private long countBusinessDays(LocalDate start, LocalDate end) {
        long count = 0;
        LocalDate date = start;
        while (!date.isAfter(end)) {
            DayOfWeek dow = date.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                count++;
            }
            date = date.plusDays(1);
        }
        return count;
    }
}
