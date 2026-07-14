package com.example.attendance.service;

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
import com.example.attendance.service.impl.LeaveServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveServiceImplTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private LeaveServiceImpl leaveService;

    private Employee employee;
    private Employee admin;

    @BeforeEach
    void setUp() {
        leaveService = new LeaveServiceImpl(leaveRequestRepository, leaveBalanceRepository, employeeRepository);

        employee = Employee.builder()
                .id(1L)
                .employeeNumber("EMP001")
                .lastName("田中")
                .firstName("太郎")
                .build();

        admin = Employee.builder()
                .id(2L)
                .employeeNumber("ADM001")
                .lastName("管理")
                .firstName("者")
                .role("ADMIN")
                .build();
    }

    @Test
    @DisplayName("有給申請: 営業日数を正しく計算する（月〜金の5日間）")
    void createLeaveRequest_paid_calculatesBusinessDays() {
        // Arrange
        var request = new LeaveRequestCreate(
                LeaveType.PAID,
                LocalDate.of(2026, 7, 13), // 月
                LocalDate.of(2026, 7, 17), // 金
                "夏季休暇"
        );
        var balance = LeaveBalance.builder()
                .id(1L).employee(employee).fiscalYear(2026)
                .totalDays(new BigDecimal("20.0")).usedDays(BigDecimal.ZERO).build();

        when(employeeRepository.findByEmployeeNumber("EMP001")).thenReturn(Optional.of(employee));
        when(leaveBalanceRepository.findByEmployeeIdAndFiscalYear(1L, 2026)).thenReturn(Optional.of(balance));
        when(leaveRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        LeaveRequestResponse response = leaveService.create(request, "EMP001");

        // Assert
        assertThat(response.days()).isEqualByComparingTo(new BigDecimal("5"));
    }

    @Test
    @DisplayName("半休申請: 0.5日が設定される")
    void createLeaveRequest_halfAm_setsDaysToHalf() {
        // Arrange
        var request = new LeaveRequestCreate(
                LeaveType.HALF_AM,
                LocalDate.of(2026, 7, 14),
                LocalDate.of(2026, 7, 14),
                "通院"
        );
        var balance = LeaveBalance.builder()
                .id(1L).employee(employee).fiscalYear(2026)
                .totalDays(new BigDecimal("20.0")).usedDays(BigDecimal.ZERO).build();

        when(employeeRepository.findByEmployeeNumber("EMP001")).thenReturn(Optional.of(employee));
        when(leaveBalanceRepository.findByEmployeeIdAndFiscalYear(1L, 2026)).thenReturn(Optional.of(balance));
        when(leaveRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        LeaveRequestResponse response = leaveService.create(request, "EMP001");

        // Assert
        assertThat(response.days()).isEqualByComparingTo(new BigDecimal("0.5"));
    }

    @Test
    @DisplayName("残日数不足時: InsufficientLeaveBalanceException をスロー")
    void createLeaveRequest_insufficientBalance_throwsException() {
        // Arrange
        var request = new LeaveRequestCreate(
                LeaveType.PAID,
                LocalDate.of(2026, 7, 13),
                LocalDate.of(2026, 7, 17),
                "休暇"
        );
        var balance = LeaveBalance.builder()
                .id(1L).employee(employee).fiscalYear(2026)
                .totalDays(new BigDecimal("20.0")).usedDays(new BigDecimal("18.0")).build();

        when(employeeRepository.findByEmployeeNumber("EMP001")).thenReturn(Optional.of(employee));
        when(leaveBalanceRepository.findByEmployeeIdAndFiscalYear(1L, 2026)).thenReturn(Optional.of(balance));

        // Act & Assert
        assertThatThrownBy(() -> leaveService.create(request, "EMP001"))
                .isInstanceOf(InsufficientLeaveBalanceException.class)
                .hasMessageContaining("有給残日数が不足しています");
    }

    @Test
    @DisplayName("特別休暇: 残日数チェックをしない")
    void createLeaveRequest_special_doesNotCheckBalance() {
        // Arrange
        var request = new LeaveRequestCreate(
                LeaveType.SPECIAL,
                LocalDate.of(2026, 7, 14),
                LocalDate.of(2026, 7, 14),
                "慶弔休暇"
        );

        when(employeeRepository.findByEmployeeNumber("EMP001")).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        LeaveRequestResponse response = leaveService.create(request, "EMP001");

        // Assert
        assertThat(response.leaveType()).isEqualTo(LeaveType.SPECIAL);
        assertThat(response.days()).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    @DisplayName("承認: ステータスが APPROVED になり残日数が減算される")
    void approve_validRequest_updatesBalanceAndStatus() {
        // Arrange
        var leaveRequest = LeaveRequest.builder()
                .id(1L).employee(employee).leaveType(LeaveType.PAID)
                .startDate(LocalDate.of(2026, 7, 14)).endDate(LocalDate.of(2026, 7, 14))
                .days(BigDecimal.ONE).reason("休み").status(ApprovalStatus.PENDING).build();
        var balance = LeaveBalance.builder()
                .id(1L).employee(employee).fiscalYear(2026)
                .totalDays(new BigDecimal("20.0")).usedDays(BigDecimal.ZERO).build();

        when(leaveRequestRepository.findByIdWithEmployee(1L)).thenReturn(Optional.of(leaveRequest));
        when(employeeRepository.findByEmployeeNumber("ADM001")).thenReturn(Optional.of(admin));
        when(leaveBalanceRepository.findByEmployeeIdAndFiscalYear(1L, 2026)).thenReturn(Optional.of(balance));
        when(leaveRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(leaveBalanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        LeaveRequestResponse response = leaveService.approve(1L, "ADM001");

        // Assert
        assertThat(response.status()).isEqualTo(ApprovalStatus.APPROVED);
        assertThat(balance.getUsedDays()).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    @DisplayName("自己承認防止: 自分の申請を承認するとエラー")
    void approve_selfApproval_throwsException() {
        // Arrange
        var leaveRequest = LeaveRequest.builder()
                .id(1L).employee(employee).leaveType(LeaveType.PAID)
                .startDate(LocalDate.of(2026, 7, 14)).endDate(LocalDate.of(2026, 7, 14))
                .days(BigDecimal.ONE).reason("休み").status(ApprovalStatus.PENDING).build();

        when(leaveRequestRepository.findByIdWithEmployee(1L)).thenReturn(Optional.of(leaveRequest));
        when(employeeRepository.findByEmployeeNumber("EMP001")).thenReturn(Optional.of(employee));

        // Act & Assert
        assertThatThrownBy(() -> leaveService.approve(1L, "EMP001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("自分の申請は承認できません");
    }

    @Test
    @DisplayName("承認済み申請の再承認: エラー")
    void approve_nonPending_throwsException() {
        // Arrange
        var leaveRequest = LeaveRequest.builder()
                .id(1L).employee(employee).leaveType(LeaveType.PAID)
                .startDate(LocalDate.of(2026, 7, 14)).endDate(LocalDate.of(2026, 7, 14))
                .days(BigDecimal.ONE).reason("休み").status(ApprovalStatus.APPROVED).build();

        when(leaveRequestRepository.findByIdWithEmployee(1L)).thenReturn(Optional.of(leaveRequest));

        // Act & Assert
        assertThatThrownBy(() -> leaveService.approve(1L, "ADM001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("承認待ち状態の申請のみ承認できます");
    }

    @Test
    @DisplayName("却下: ステータスが REJECTED になる")
    void reject_validRequest_updatesStatus() {
        // Arrange
        var leaveRequest = LeaveRequest.builder()
                .id(1L).employee(employee).leaveType(LeaveType.PAID)
                .startDate(LocalDate.of(2026, 7, 14)).endDate(LocalDate.of(2026, 7, 14))
                .days(BigDecimal.ONE).reason("休み").status(ApprovalStatus.PENDING).build();

        when(leaveRequestRepository.findByIdWithEmployee(1L)).thenReturn(Optional.of(leaveRequest));
        when(employeeRepository.findByEmployeeNumber("ADM001")).thenReturn(Optional.of(admin));
        when(leaveRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        LeaveRequestResponse response = leaveService.reject(1L, "ADM001");

        // Assert
        assertThat(response.status()).isEqualTo(ApprovalStatus.REJECTED);
    }

    @Test
    @DisplayName("残日数取得: 正しい値が返される")
    void getBalance_existingData_returnsBalance() {
        // Arrange
        var balance = LeaveBalance.builder()
                .id(1L).employee(employee).fiscalYear(2026)
                .totalDays(new BigDecimal("20.0")).usedDays(new BigDecimal("5.0")).build();

        when(employeeRepository.findByEmployeeNumber("EMP001")).thenReturn(Optional.of(employee));
        when(leaveBalanceRepository.findByEmployeeIdAndFiscalYear(1L, 2026)).thenReturn(Optional.of(balance));

        // Act
        LeaveBalanceResponse response = leaveService.getBalance("EMP001", 2026);

        // Assert
        assertThat(response.totalDays()).isEqualByComparingTo(new BigDecimal("20.0"));
        assertThat(response.usedDays()).isEqualByComparingTo(new BigDecimal("5.0"));
        assertThat(response.remainingDays()).isEqualByComparingTo(new BigDecimal("15.0"));
    }
}
