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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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

    @Nested
    @DisplayName("create - 休暇申請作成")
    class CreateTests {

        @Test
        @DisplayName("有給申請: 営業日数を正しく計算する（月〜金の5日間）")
        void create_paid_calculatesBusinessDays() {
            var request = new LeaveRequestCreate(
                    LeaveType.PAID,
                    LocalDate.of(2026, 7, 13),
                    LocalDate.of(2026, 7, 17),
                    "夏季休暇"
            );
            var balance = LeaveBalance.builder()
                    .id(1L).employee(employee).fiscalYear(2026)
                    .totalDays(new BigDecimal("20.0")).usedDays(BigDecimal.ZERO).build();

            when(employeeRepository.findByEmployeeNumber("EMP001")).thenReturn(Optional.of(employee));
            when(leaveBalanceRepository.findByEmployeeIdAndFiscalYear(1L, 2026)).thenReturn(Optional.of(balance));
            when(leaveRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            LeaveRequestResponse response = leaveService.create(request, "EMP001");

            assertThat(response.days()).isEqualByComparingTo(new BigDecimal("5"));
        }

        @Test
        @DisplayName("有給申請: 週末を含む期間で営業日のみカウントする")
        void create_paid_excludesWeekends() {
            var request = new LeaveRequestCreate(
                    LeaveType.PAID,
                    LocalDate.of(2026, 7, 10), // 金
                    LocalDate.of(2026, 7, 14), // 火
                    "連休"
            );
            var balance = LeaveBalance.builder()
                    .id(1L).employee(employee).fiscalYear(2026)
                    .totalDays(new BigDecimal("20.0")).usedDays(BigDecimal.ZERO).build();

            when(employeeRepository.findByEmployeeNumber("EMP001")).thenReturn(Optional.of(employee));
            when(leaveBalanceRepository.findByEmployeeIdAndFiscalYear(1L, 2026)).thenReturn(Optional.of(balance));
            when(leaveRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            LeaveRequestResponse response = leaveService.create(request, "EMP001");

            assertThat(response.days()).isEqualByComparingTo(new BigDecimal("3"));
        }

        @Test
        @DisplayName("半休申請: 0.5日が設定される")
        void create_halfAm_setsDaysToHalf() {
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

            LeaveRequestResponse response = leaveService.create(request, "EMP001");

            assertThat(response.days()).isEqualByComparingTo(new BigDecimal("0.5"));
        }

        @Test
        @DisplayName("午後半休: 0.5日が設定される")
        void create_halfPm_setsDaysToHalf() {
            var request = new LeaveRequestCreate(
                    LeaveType.HALF_PM,
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

            LeaveRequestResponse response = leaveService.create(request, "EMP001");

            assertThat(response.days()).isEqualByComparingTo(new BigDecimal("0.5"));
        }

        @Test
        @DisplayName("残日数不足時: InsufficientLeaveBalanceException をスロー")
        void create_insufficientBalance_throwsException() {
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

            assertThatThrownBy(() -> leaveService.create(request, "EMP001"))
                    .isInstanceOf(InsufficientLeaveBalanceException.class)
                    .hasMessageContaining("有給残日数が不足しています");
        }

        @Test
        @DisplayName("特別休暇: 残日数チェックをしない")
        void create_special_doesNotCheckBalance() {
            var request = new LeaveRequestCreate(
                    LeaveType.SPECIAL,
                    LocalDate.of(2026, 7, 14),
                    LocalDate.of(2026, 7, 14),
                    "慶弔休暇"
            );

            when(employeeRepository.findByEmployeeNumber("EMP001")).thenReturn(Optional.of(employee));
            when(leaveRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            LeaveRequestResponse response = leaveService.create(request, "EMP001");

            assertThat(response.leaveType()).isEqualTo(LeaveType.SPECIAL);
            assertThat(response.days()).isEqualByComparingTo(BigDecimal.ONE);
            verify(leaveBalanceRepository, never()).findByEmployeeIdAndFiscalYear(any(), any());
        }

        @Test
        @DisplayName("代休: 残日数チェックをしない")
        void create_compensatory_doesNotCheckBalance() {
            var request = new LeaveRequestCreate(
                    LeaveType.COMPENSATORY,
                    LocalDate.of(2026, 7, 14),
                    LocalDate.of(2026, 7, 14),
                    "代休取得"
            );

            when(employeeRepository.findByEmployeeNumber("EMP001")).thenReturn(Optional.of(employee));
            when(leaveRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            LeaveRequestResponse response = leaveService.create(request, "EMP001");

            assertThat(response.leaveType()).isEqualTo(LeaveType.COMPENSATORY);
            verify(leaveBalanceRepository, never()).findByEmployeeIdAndFiscalYear(any(), any());
        }

        @Test
        @DisplayName("存在しない社員番号: ResourceNotFoundException をスロー")
        void create_unknownEmployee_throwsException() {
            var request = new LeaveRequestCreate(
                    LeaveType.PAID,
                    LocalDate.of(2026, 7, 14),
                    LocalDate.of(2026, 7, 14),
                    "休み"
            );

            when(employeeRepository.findByEmployeeNumber("UNKNOWN")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> leaveService.create(request, "UNKNOWN"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("社員が見つかりません");
        }

        @Test
        @DisplayName("残日数データなし: ResourceNotFoundException をスロー")
        void create_noBalanceData_throwsException() {
            var request = new LeaveRequestCreate(
                    LeaveType.PAID,
                    LocalDate.of(2026, 7, 14),
                    LocalDate.of(2026, 7, 14),
                    "休み"
            );

            when(employeeRepository.findByEmployeeNumber("EMP001")).thenReturn(Optional.of(employee));
            when(leaveBalanceRepository.findByEmployeeIdAndFiscalYear(1L, 2026)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> leaveService.create(request, "EMP001"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("有給残日数データが見つかりません");
        }
    }

    @Nested
    @DisplayName("findAll - 全件検索")
    class FindAllTests {

        @Test
        @DisplayName("ステータス指定なし: 全件を返す")
        void findAll_noStatus_returnsAll() {
            var leaveRequest = LeaveRequest.builder()
                    .id(1L).employee(employee).leaveType(LeaveType.PAID)
                    .startDate(LocalDate.of(2026, 7, 14)).endDate(LocalDate.of(2026, 7, 14))
                    .days(BigDecimal.ONE).reason("休み").status(ApprovalStatus.PENDING).build();
            Page<LeaveRequest> page = new PageImpl<>(List.of(leaveRequest));
            Pageable pageable = PageRequest.of(0, 20);

            when(leaveRequestRepository.findAll(pageable)).thenReturn(page);

            Page<LeaveRequestResponse> result = leaveService.findAll(null, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).leaveType()).isEqualTo(LeaveType.PAID);
        }

        @Test
        @DisplayName("ステータス指定あり: フィルタした結果を返す")
        void findAll_withStatus_returnsFiltered() {
            var leaveRequest = LeaveRequest.builder()
                    .id(1L).employee(employee).leaveType(LeaveType.PAID)
                    .startDate(LocalDate.of(2026, 7, 14)).endDate(LocalDate.of(2026, 7, 14))
                    .days(BigDecimal.ONE).reason("休み").status(ApprovalStatus.PENDING).build();
            Page<LeaveRequest> page = new PageImpl<>(List.of(leaveRequest));
            Pageable pageable = PageRequest.of(0, 20);

            when(leaveRequestRepository.findByStatus(ApprovalStatus.PENDING, pageable)).thenReturn(page);

            Page<LeaveRequestResponse> result = leaveService.findAll(ApprovalStatus.PENDING, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).status()).isEqualTo(ApprovalStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("findByEmployee - 社員別検索")
    class FindByEmployeeTests {

        @Test
        @DisplayName("ステータス指定なし: 社員の全申請を返す")
        void findByEmployee_noStatus_returnsAll() {
            var leaveRequest = LeaveRequest.builder()
                    .id(1L).employee(employee).leaveType(LeaveType.PAID)
                    .startDate(LocalDate.of(2026, 7, 14)).endDate(LocalDate.of(2026, 7, 14))
                    .days(BigDecimal.ONE).reason("休み").status(ApprovalStatus.PENDING).build();
            Page<LeaveRequest> page = new PageImpl<>(List.of(leaveRequest));
            Pageable pageable = PageRequest.of(0, 20);

            when(employeeRepository.findByEmployeeNumber("EMP001")).thenReturn(Optional.of(employee));
            when(leaveRequestRepository.findByEmployeeId(1L, pageable)).thenReturn(page);

            Page<LeaveRequestResponse> result = leaveService.findByEmployee("EMP001", null, pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("ステータス指定あり: フィルタした結果を返す")
        void findByEmployee_withStatus_returnsFiltered() {
            var leaveRequest = LeaveRequest.builder()
                    .id(1L).employee(employee).leaveType(LeaveType.PAID)
                    .startDate(LocalDate.of(2026, 7, 14)).endDate(LocalDate.of(2026, 7, 14))
                    .days(BigDecimal.ONE).reason("休み").status(ApprovalStatus.APPROVED).build();
            Page<LeaveRequest> page = new PageImpl<>(List.of(leaveRequest));
            Pageable pageable = PageRequest.of(0, 20);

            when(employeeRepository.findByEmployeeNumber("EMP001")).thenReturn(Optional.of(employee));
            when(leaveRequestRepository.findByEmployeeIdAndStatus(1L, ApprovalStatus.APPROVED, pageable)).thenReturn(page);

            Page<LeaveRequestResponse> result = leaveService.findByEmployee("EMP001", ApprovalStatus.APPROVED, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).status()).isEqualTo(ApprovalStatus.APPROVED);
        }
    }

    @Nested
    @DisplayName("approve - 承認")
    class ApproveTests {

        @Test
        @DisplayName("正常承認: ステータスが APPROVED になり残日数が減算される")
        void approve_validRequest_updatesBalanceAndStatus() {
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

            LeaveRequestResponse response = leaveService.approve(1L, "ADM001");

            assertThat(response.status()).isEqualTo(ApprovalStatus.APPROVED);
            assertThat(balance.getUsedDays()).isEqualByComparingTo(BigDecimal.ONE);
            verify(leaveBalanceRepository).save(balance);
        }

        @Test
        @DisplayName("残日数がちょうど申請日数と同じ: 承認成功")
        void approve_exactBalance_succeeds() {
            var leaveRequest = LeaveRequest.builder()
                    .id(1L).employee(employee).leaveType(LeaveType.PAID)
                    .startDate(LocalDate.of(2026, 7, 13)).endDate(LocalDate.of(2026, 7, 17))
                    .days(new BigDecimal("5")).reason("休み").status(ApprovalStatus.PENDING).build();
            var balance = LeaveBalance.builder()
                    .id(1L).employee(employee).fiscalYear(2026)
                    .totalDays(new BigDecimal("20.0")).usedDays(new BigDecimal("15.0")).build();

            when(leaveRequestRepository.findByIdWithEmployee(1L)).thenReturn(Optional.of(leaveRequest));
            when(employeeRepository.findByEmployeeNumber("ADM001")).thenReturn(Optional.of(admin));
            when(leaveBalanceRepository.findByEmployeeIdAndFiscalYear(1L, 2026)).thenReturn(Optional.of(balance));
            when(leaveRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(leaveBalanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            LeaveRequestResponse response = leaveService.approve(1L, "ADM001");

            assertThat(response.status()).isEqualTo(ApprovalStatus.APPROVED);
            assertThat(balance.getUsedDays()).isEqualByComparingTo(new BigDecimal("20.0"));
        }

        @Test
        @DisplayName("承認時に残日数不足: InsufficientLeaveBalanceException をスロー")
        void approve_insufficientBalance_throwsException() {
            var leaveRequest = LeaveRequest.builder()
                    .id(1L).employee(employee).leaveType(LeaveType.PAID)
                    .startDate(LocalDate.of(2026, 7, 13)).endDate(LocalDate.of(2026, 7, 17))
                    .days(new BigDecimal("5")).reason("休み").status(ApprovalStatus.PENDING).build();
            var balance = LeaveBalance.builder()
                    .id(1L).employee(employee).fiscalYear(2026)
                    .totalDays(new BigDecimal("20.0")).usedDays(new BigDecimal("18.0")).build();

            when(leaveRequestRepository.findByIdWithEmployee(1L)).thenReturn(Optional.of(leaveRequest));
            when(employeeRepository.findByEmployeeNumber("ADM001")).thenReturn(Optional.of(admin));
            when(leaveBalanceRepository.findByEmployeeIdAndFiscalYear(1L, 2026)).thenReturn(Optional.of(balance));

            assertThatThrownBy(() -> leaveService.approve(1L, "ADM001"))
                    .isInstanceOf(InsufficientLeaveBalanceException.class)
                    .hasMessageContaining("有給残日数が不足しています");
        }

        @Test
        @DisplayName("承認時に残日数データなし: ResourceNotFoundException をスロー")
        void approve_noBalanceData_throwsException() {
            var leaveRequest = LeaveRequest.builder()
                    .id(1L).employee(employee).leaveType(LeaveType.PAID)
                    .startDate(LocalDate.of(2026, 7, 14)).endDate(LocalDate.of(2026, 7, 14))
                    .days(BigDecimal.ONE).reason("休み").status(ApprovalStatus.PENDING).build();

            when(leaveRequestRepository.findByIdWithEmployee(1L)).thenReturn(Optional.of(leaveRequest));
            when(employeeRepository.findByEmployeeNumber("ADM001")).thenReturn(Optional.of(admin));
            when(leaveBalanceRepository.findByEmployeeIdAndFiscalYear(1L, 2026)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> leaveService.approve(1L, "ADM001"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("有給残日数データが見つかりません");
        }

        @Test
        @DisplayName("自己承認防止: 自分の申請を承認するとエラー")
        void approve_selfApproval_throwsException() {
            var leaveRequest = LeaveRequest.builder()
                    .id(1L).employee(employee).leaveType(LeaveType.PAID)
                    .startDate(LocalDate.of(2026, 7, 14)).endDate(LocalDate.of(2026, 7, 14))
                    .days(BigDecimal.ONE).reason("休み").status(ApprovalStatus.PENDING).build();

            when(leaveRequestRepository.findByIdWithEmployee(1L)).thenReturn(Optional.of(leaveRequest));
            when(employeeRepository.findByEmployeeNumber("EMP001")).thenReturn(Optional.of(employee));

            assertThatThrownBy(() -> leaveService.approve(1L, "EMP001"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("自分の申請は承認できません");
        }

        @Test
        @DisplayName("承認済み申請の再承認: エラー")
        void approve_alreadyApproved_throwsException() {
            var leaveRequest = LeaveRequest.builder()
                    .id(1L).employee(employee).leaveType(LeaveType.PAID)
                    .startDate(LocalDate.of(2026, 7, 14)).endDate(LocalDate.of(2026, 7, 14))
                    .days(BigDecimal.ONE).reason("休み").status(ApprovalStatus.APPROVED).build();

            when(leaveRequestRepository.findByIdWithEmployee(1L)).thenReturn(Optional.of(leaveRequest));

            assertThatThrownBy(() -> leaveService.approve(1L, "ADM001"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("承認待ち状態の申請のみ承認できます");
        }

        @Test
        @DisplayName("却下済み申請の承認: エラー")
        void approve_alreadyRejected_throwsException() {
            var leaveRequest = LeaveRequest.builder()
                    .id(1L).employee(employee).leaveType(LeaveType.PAID)
                    .startDate(LocalDate.of(2026, 7, 14)).endDate(LocalDate.of(2026, 7, 14))
                    .days(BigDecimal.ONE).reason("休み").status(ApprovalStatus.REJECTED).build();

            when(leaveRequestRepository.findByIdWithEmployee(1L)).thenReturn(Optional.of(leaveRequest));

            assertThatThrownBy(() -> leaveService.approve(1L, "ADM001"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("承認待ち状態の申請のみ承認できます");
        }

        @Test
        @DisplayName("存在しない申請の承認: ResourceNotFoundException")
        void approve_notFound_throwsException() {
            when(leaveRequestRepository.findByIdWithEmployee(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> leaveService.approve(999L, "ADM001"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("休暇申請が見つかりません");
        }

        @Test
        @DisplayName("特別休暇の承認: 残日数チェックなし")
        void approve_special_doesNotCheckBalance() {
            var leaveRequest = LeaveRequest.builder()
                    .id(1L).employee(employee).leaveType(LeaveType.SPECIAL)
                    .startDate(LocalDate.of(2026, 7, 14)).endDate(LocalDate.of(2026, 7, 14))
                    .days(BigDecimal.ONE).reason("慶弔").status(ApprovalStatus.PENDING).build();

            when(leaveRequestRepository.findByIdWithEmployee(1L)).thenReturn(Optional.of(leaveRequest));
            when(employeeRepository.findByEmployeeNumber("ADM001")).thenReturn(Optional.of(admin));
            when(leaveRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            LeaveRequestResponse response = leaveService.approve(1L, "ADM001");

            assertThat(response.status()).isEqualTo(ApprovalStatus.APPROVED);
            verify(leaveBalanceRepository, never()).findByEmployeeIdAndFiscalYear(any(), any());
        }
    }

    @Nested
    @DisplayName("reject - 却下")
    class RejectTests {

        @Test
        @DisplayName("正常却下: ステータスが REJECTED になる")
        void reject_validRequest_updatesStatus() {
            var leaveRequest = LeaveRequest.builder()
                    .id(1L).employee(employee).leaveType(LeaveType.PAID)
                    .startDate(LocalDate.of(2026, 7, 14)).endDate(LocalDate.of(2026, 7, 14))
                    .days(BigDecimal.ONE).reason("休み").status(ApprovalStatus.PENDING).build();

            when(leaveRequestRepository.findByIdWithEmployee(1L)).thenReturn(Optional.of(leaveRequest));
            when(employeeRepository.findByEmployeeNumber("ADM001")).thenReturn(Optional.of(admin));
            when(leaveRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            LeaveRequestResponse response = leaveService.reject(1L, "ADM001");

            assertThat(response.status()).isEqualTo(ApprovalStatus.REJECTED);
        }

        @Test
        @DisplayName("自己却下防止: 自分の申請は却下できない")
        void reject_selfRejection_throwsException() {
            var leaveRequest = LeaveRequest.builder()
                    .id(1L).employee(employee).leaveType(LeaveType.PAID)
                    .startDate(LocalDate.of(2026, 7, 14)).endDate(LocalDate.of(2026, 7, 14))
                    .days(BigDecimal.ONE).reason("休み").status(ApprovalStatus.PENDING).build();

            when(leaveRequestRepository.findByIdWithEmployee(1L)).thenReturn(Optional.of(leaveRequest));
            when(employeeRepository.findByEmployeeNumber("EMP001")).thenReturn(Optional.of(employee));

            assertThatThrownBy(() -> leaveService.reject(1L, "EMP001"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("自分の申請は却下できません");
        }

        @Test
        @DisplayName("承認済み申請の却下: エラー")
        void reject_alreadyApproved_throwsException() {
            var leaveRequest = LeaveRequest.builder()
                    .id(1L).employee(employee).leaveType(LeaveType.PAID)
                    .startDate(LocalDate.of(2026, 7, 14)).endDate(LocalDate.of(2026, 7, 14))
                    .days(BigDecimal.ONE).reason("休み").status(ApprovalStatus.APPROVED).build();

            when(leaveRequestRepository.findByIdWithEmployee(1L)).thenReturn(Optional.of(leaveRequest));

            assertThatThrownBy(() -> leaveService.reject(1L, "ADM001"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("承認待ち状態の申請のみ却下できます");
        }

        @Test
        @DisplayName("却下済み申請の再却下: エラー")
        void reject_alreadyRejected_throwsException() {
            var leaveRequest = LeaveRequest.builder()
                    .id(1L).employee(employee).leaveType(LeaveType.PAID)
                    .startDate(LocalDate.of(2026, 7, 14)).endDate(LocalDate.of(2026, 7, 14))
                    .days(BigDecimal.ONE).reason("休み").status(ApprovalStatus.REJECTED).build();

            when(leaveRequestRepository.findByIdWithEmployee(1L)).thenReturn(Optional.of(leaveRequest));

            assertThatThrownBy(() -> leaveService.reject(1L, "ADM001"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("承認待ち状態の申請のみ却下できます");
        }

        @Test
        @DisplayName("存在しない申請の却下: ResourceNotFoundException")
        void reject_notFound_throwsException() {
            when(leaveRequestRepository.findByIdWithEmployee(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> leaveService.reject(999L, "ADM001"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("休暇申請が見つかりません");
        }
    }

    @Nested
    @DisplayName("getBalance - 残日数取得")
    class GetBalanceTests {

        @Test
        @DisplayName("年度指定あり: 正しい値が返される")
        void getBalance_withFiscalYear_returnsBalance() {
            var balance = LeaveBalance.builder()
                    .id(1L).employee(employee).fiscalYear(2026)
                    .totalDays(new BigDecimal("20.0")).usedDays(new BigDecimal("5.0")).build();

            when(employeeRepository.findByEmployeeNumber("EMP001")).thenReturn(Optional.of(employee));
            when(leaveBalanceRepository.findByEmployeeIdAndFiscalYear(1L, 2026)).thenReturn(Optional.of(balance));

            LeaveBalanceResponse response = leaveService.getBalance("EMP001", 2026);

            assertThat(response.totalDays()).isEqualByComparingTo(new BigDecimal("20.0"));
            assertThat(response.usedDays()).isEqualByComparingTo(new BigDecimal("5.0"));
            assertThat(response.remainingDays()).isEqualByComparingTo(new BigDecimal("15.0"));
        }

        @Test
        @DisplayName("年度指定なし: 当年度をデフォルトにする")
        void getBalance_nullFiscalYear_usesCurrentYear() {
            int currentYear = LocalDate.now().getYear();
            var balance = LeaveBalance.builder()
                    .id(1L).employee(employee).fiscalYear(currentYear)
                    .totalDays(new BigDecimal("20.0")).usedDays(BigDecimal.ZERO).build();

            when(employeeRepository.findByEmployeeNumber("EMP001")).thenReturn(Optional.of(employee));
            when(leaveBalanceRepository.findByEmployeeIdAndFiscalYear(1L, currentYear)).thenReturn(Optional.of(balance));

            LeaveBalanceResponse response = leaveService.getBalance("EMP001", null);

            assertThat(response.fiscalYear()).isEqualTo(currentYear);
        }

        @Test
        @DisplayName("残日数データなし: ResourceNotFoundException")
        void getBalance_noData_throwsException() {
            when(employeeRepository.findByEmployeeNumber("EMP001")).thenReturn(Optional.of(employee));
            when(leaveBalanceRepository.findByEmployeeIdAndFiscalYear(1L, 2026)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> leaveService.getBalance("EMP001", 2026))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("有給残日数データが見つかりません");
        }
    }
}
