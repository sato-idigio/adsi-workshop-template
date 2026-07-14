package com.example.attendance.service;

import com.example.attendance.dto.MonthlySummaryResponse;
import com.example.attendance.entity.Department;
import com.example.attendance.entity.Employee;
import com.example.attendance.entity.LeaveRequest;
import com.example.attendance.entity.MonthlyAttendanceSummary;
import com.example.attendance.entity.enums.ApprovalStatus;
import com.example.attendance.entity.enums.LeaveType;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.repository.LeaveRequestRepository;
import com.example.attendance.repository.MonthlyAttendanceSummaryRepository;
import com.example.attendance.service.impl.MonthlyReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
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
class MonthlyReportServiceImplTest {

    @Mock
    private MonthlyAttendanceSummaryRepository summaryRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    private MonthlyReportServiceImpl service;

    private Department department;
    private Employee employee1;
    private Employee employee2;

    @BeforeEach
    void setUp() {
        service = new MonthlyReportServiceImpl(summaryRepository, employeeRepository, leaveRequestRepository);

        department = Department.builder()
                .id(1L)
                .name("開発部")
                .build();

        employee1 = Employee.builder()
                .id(1L)
                .employeeNumber("EMP001")
                .lastName("田中")
                .firstName("太郎")
                .department(department)
                .build();

        employee2 = Employee.builder()
                .id(2L)
                .employeeNumber("EMP002")
                .lastName("佐藤")
                .firstName("花子")
                .department(department)
                .build();
    }

    @Nested
    @DisplayName("getMonthlySummaries - 月次集計取得")
    class GetMonthlySummaries {

        @Test
        @DisplayName("フィルターなしでページ結果を返す")
        void getMonthlySummaries_noFilter_returnsPagedResults() {
            var summary = MonthlyAttendanceSummary.builder()
                    .id(1L)
                    .employee(employee1)
                    .yearMonth("2026-07")
                    .totalWorkMinutes(9600)
                    .overtimeMinutes(300)
                    .nightMinutes(0)
                    .holidayWorkMinutes(0)
                    .paidLeaveDays(new BigDecimal("1.0"))
                    .workingDays(20)
                    .build();

            var page = new PageImpl<>(List.of(summary), PageRequest.of(0, 20), 1);
            when(summaryRepository.findByYearMonth("2026-07", PageRequest.of(0, 20))).thenReturn(page);

            Page<MonthlySummaryResponse> result = service.getMonthlySummaries("2026-07", null, PageRequest.of(0, 20));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).employeeName()).isEqualTo("田中 太郎");
            assertThat(result.getContent().get(0).departmentName()).isEqualTo("開発部");
        }

        @Test
        @DisplayName("部署フィルター付きで結果を返す")
        void getMonthlySummaries_withDepartmentFilter_filtersResults() {
            var page = new PageImpl<MonthlyAttendanceSummary>(Collections.emptyList(), PageRequest.of(0, 20), 0);
            when(summaryRepository.findByYearMonthAndEmployeeDepartmentId("2026-07", 1L, PageRequest.of(0, 20)))
                    .thenReturn(page);

            Page<MonthlySummaryResponse> result = service.getMonthlySummaries("2026-07", 1L, PageRequest.of(0, 20));

            assertThat(result.getContent()).isEmpty();
            verify(summaryRepository).findByYearMonthAndEmployeeDepartmentId("2026-07", 1L, PageRequest.of(0, 20));
        }

        @Test
        @DisplayName("結果が空の場合は空ページを返す")
        void getMonthlySummaries_emptyResult_returnsEmptyPage() {
            var page = new PageImpl<MonthlyAttendanceSummary>(Collections.emptyList(), PageRequest.of(0, 20), 0);
            when(summaryRepository.findByYearMonth("2026-01", PageRequest.of(0, 20))).thenReturn(page);

            Page<MonthlySummaryResponse> result = service.getMonthlySummaries("2026-01", null, PageRequest.of(0, 20));

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("不正なyearMonth形式で例外を投げる")
        void getMonthlySummaries_invalidYearMonth_throwsException() {
            assertThatThrownBy(() -> service.getMonthlySummaries("2026-13", null, PageRequest.of(0, 20)))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> service.getMonthlySummaries("invalid", null, PageRequest.of(0, 20)))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("generateSummaries - 月次集計生成")
    class GenerateSummaries {

        @Test
        @DisplayName("勤怠レコードなしの場合、勤務時間は全て0になる")
        void generate_noAttendanceRecords_allMinutesAreZero() {
            when(employeeRepository.findAllWithDepartment()).thenReturn(List.of(employee1));
            when(leaveRequestRepository.findAllApprovedPaidLeavesInMonth(any(), any()))
                    .thenReturn(Collections.emptyList());
            when(summaryRepository.findByEmployeeIdAndYearMonth(1L, "2026-07"))
                    .thenReturn(Optional.empty());

            service.generateSummaries("2026-07");

            ArgumentCaptor<MonthlyAttendanceSummary> captor = ArgumentCaptor.forClass(MonthlyAttendanceSummary.class);
            verify(summaryRepository).save(captor.capture());

            MonthlyAttendanceSummary saved = captor.getValue();
            assertThat(saved.getTotalWorkMinutes()).isZero();
            assertThat(saved.getOvertimeMinutes()).isZero();
            assertThat(saved.getNightMinutes()).isZero();
            assertThat(saved.getHolidayWorkMinutes()).isZero();
            assertThat(saved.getWorkingDays()).isZero();
            assertThat(saved.getPaidLeaveDays()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("承認済み有給休暇がある場合、有給取得日数が計算される")
        void generate_withApprovedPaidLeave_calculatesPaidLeaveDays() {
            when(employeeRepository.findAllWithDepartment()).thenReturn(List.of(employee1));

            var leave = LeaveRequest.builder()
                    .id(1L)
                    .employee(employee1)
                    .leaveType(LeaveType.PAID)
                    .startDate(LocalDate.of(2026, 7, 6))
                    .endDate(LocalDate.of(2026, 7, 8))
                    .days(new BigDecimal("3.0"))
                    .status(ApprovalStatus.APPROVED)
                    .build();

            when(leaveRequestRepository.findAllApprovedPaidLeavesInMonth(any(), any()))
                    .thenReturn(List.of(leave));
            when(summaryRepository.findByEmployeeIdAndYearMonth(1L, "2026-07"))
                    .thenReturn(Optional.empty());

            service.generateSummaries("2026-07");

            ArgumentCaptor<MonthlyAttendanceSummary> captor = ArgumentCaptor.forClass(MonthlyAttendanceSummary.class);
            verify(summaryRepository).save(captor.capture());

            assertThat(captor.getValue().getPaidLeaveDays()).isEqualByComparingTo(new BigDecimal("3"));
        }

        @Test
        @DisplayName("半休の場合、0.5日として計算される")
        void generate_withHalfLeave_calculatesHalfDay() {
            when(employeeRepository.findAllWithDepartment()).thenReturn(List.of(employee1));

            var leave = LeaveRequest.builder()
                    .id(1L)
                    .employee(employee1)
                    .leaveType(LeaveType.HALF_AM)
                    .startDate(LocalDate.of(2026, 7, 7))
                    .endDate(LocalDate.of(2026, 7, 7))
                    .days(new BigDecimal("0.5"))
                    .status(ApprovalStatus.APPROVED)
                    .build();

            when(leaveRequestRepository.findAllApprovedPaidLeavesInMonth(any(), any()))
                    .thenReturn(List.of(leave));
            when(summaryRepository.findByEmployeeIdAndYearMonth(1L, "2026-07"))
                    .thenReturn(Optional.empty());

            service.generateSummaries("2026-07");

            ArgumentCaptor<MonthlyAttendanceSummary> captor = ArgumentCaptor.forClass(MonthlyAttendanceSummary.class);
            verify(summaryRepository).save(captor.capture());

            assertThat(captor.getValue().getPaidLeaveDays()).isEqualByComparingTo(new BigDecimal("0.5"));
        }

        @Test
        @DisplayName("SPECIAL休暇はカウントされない（PaidLeavesInMonthクエリで除外済み）")
        void generate_specialLeaveNotCounted() {
            when(employeeRepository.findAllWithDepartment()).thenReturn(List.of(employee1));
            when(leaveRequestRepository.findAllApprovedPaidLeavesInMonth(any(), any()))
                    .thenReturn(Collections.emptyList());
            when(summaryRepository.findByEmployeeIdAndYearMonth(1L, "2026-07"))
                    .thenReturn(Optional.empty());

            service.generateSummaries("2026-07");

            ArgumentCaptor<MonthlyAttendanceSummary> captor = ArgumentCaptor.forClass(MonthlyAttendanceSummary.class);
            verify(summaryRepository).save(captor.capture());

            assertThat(captor.getValue().getPaidLeaveDays()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("月跨ぎの休暇は対象月内の平日のみカウントされる")
        void generate_leaveSpanningMonths_onlyCountsDaysInTargetMonth() {
            when(employeeRepository.findAllWithDepartment()).thenReturn(List.of(employee1));

            // 2026-06-29(日) ~ 2026-07-03(金): 7月内の平日は 7/1(水),7/2(木),7/3(金) = 3日
            var leave = LeaveRequest.builder()
                    .id(1L)
                    .employee(employee1)
                    .leaveType(LeaveType.PAID)
                    .startDate(LocalDate.of(2026, 6, 29))
                    .endDate(LocalDate.of(2026, 7, 3))
                    .days(new BigDecimal("5.0"))
                    .status(ApprovalStatus.APPROVED)
                    .build();

            when(leaveRequestRepository.findAllApprovedPaidLeavesInMonth(any(), any()))
                    .thenReturn(List.of(leave));
            when(summaryRepository.findByEmployeeIdAndYearMonth(1L, "2026-07"))
                    .thenReturn(Optional.empty());

            service.generateSummaries("2026-07");

            ArgumentCaptor<MonthlyAttendanceSummary> captor = ArgumentCaptor.forClass(MonthlyAttendanceSummary.class);
            verify(summaryRepository).save(captor.capture());

            assertThat(captor.getValue().getPaidLeaveDays()).isEqualByComparingTo(new BigDecimal("3"));
        }

        @Test
        @DisplayName("既存サマリーがある場合は更新される（upsert）")
        void generate_existingSummary_updatesInsteadOfInsert() {
            when(employeeRepository.findAllWithDepartment()).thenReturn(List.of(employee1));
            when(leaveRequestRepository.findAllApprovedPaidLeavesInMonth(any(), any()))
                    .thenReturn(Collections.emptyList());

            var existing = MonthlyAttendanceSummary.builder()
                    .id(99L)
                    .employee(employee1)
                    .yearMonth("2026-07")
                    .totalWorkMinutes(100)
                    .overtimeMinutes(50)
                    .paidLeaveDays(new BigDecimal("2.0"))
                    .workingDays(5)
                    .build();

            when(summaryRepository.findByEmployeeIdAndYearMonth(1L, "2026-07"))
                    .thenReturn(Optional.of(existing));

            service.generateSummaries("2026-07");

            verify(summaryRepository, never()).save(any());
            assertThat(existing.getTotalWorkMinutes()).isZero();
            assertThat(existing.getPaidLeaveDays()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("複数社員に対して各1レコードが生成される")
        void generate_multipleEmployees_createsOnePerEmployee() {
            when(employeeRepository.findAllWithDepartment()).thenReturn(List.of(employee1, employee2));
            when(leaveRequestRepository.findAllApprovedPaidLeavesInMonth(any(), any()))
                    .thenReturn(Collections.emptyList());
            when(summaryRepository.findByEmployeeIdAndYearMonth(any(), eq("2026-07")))
                    .thenReturn(Optional.empty());

            service.generateSummaries("2026-07");

            verify(summaryRepository, org.mockito.Mockito.times(2)).save(any(MonthlyAttendanceSummary.class));
        }

        @Test
        @DisplayName("不正なyearMonth形式で例外を投げる")
        void generate_invalidYearMonth_throwsException() {
            assertThatThrownBy(() -> service.generateSummaries("invalid"))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> service.generateSummaries(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
