# ドメインモデル設計

## Entity 一覧

| Entity | 説明 | 対応テーブル |
|--------|------|-------------|
| Employee | 社員 | employees |
| Department | 部署（最大3階層） | departments |
| AttendanceRecord | 日次の出退勤記録 | attendance_records |
| AttendanceCorrectionRequest | 打刻修正申請 | attendance_correction_requests |
| LeaveRequest | 休暇申請 | leave_requests |
| LeaveBalance | 有給残日数 | leave_balances |
| MonthlyAttendanceSummary | 月次勤怠集計 | monthly_attendance_summaries |

## Value Object 一覧

| Value Object | 説明 | 使用箇所 |
|-------------|------|----------|
| WorkDuration | 勤務時間（時間・分） | AttendanceRecord, MonthlyAttendanceSummary |
| EmployeeNumber | 社員番号（一意識別子） | Employee |
| ApprovalStatus | 申請ステータス（PENDING / APPROVED / REJECTED） | AttendanceCorrectionRequest, LeaveRequest |
| LeaveType | 休暇種別（PAID / HALF_AM / HALF_PM / SPECIAL / COMPENSATORY） | LeaveRequest |
| Role | ロール（EMPLOYEE / ADMIN） | Employee |

## Entity 詳細

### Employee（社員）

```
Employee
├── id: Long (PK, auto)
├── employeeNumber: EmployeeNumber (unique, ログインID)
├── lastName: String (姓)
├── firstName: String (名)
├── email: String
├── password: String (BCrypt hash)
├── department: Department (FK)
├── position: String (役職)
├── role: Role (EMPLOYEE / ADMIN)
├── hireDate: LocalDate (入社日)
├── version: Long (楽観ロック)
├── createdAt: LocalDateTime
└── updatedAt: LocalDateTime
```

### Department（部署）

```
Department
├── id: Long (PK, auto)
├── name: String (部署名)
├── parent: Department (FK, nullable — 最上位は null)
├── level: Integer (1=本部, 2=部, 3=課)
├── version: Long (楽観ロック)
├── createdAt: LocalDateTime
└── updatedAt: LocalDateTime
```

### AttendanceRecord（出退勤記録）

```
AttendanceRecord
├── id: Long (PK, auto)
├── employee: Employee (FK)
├── date: LocalDate (勤務日)
├── clockIn: LocalDateTime (出勤時刻, nullable)
├── clockOut: LocalDateTime (退勤時刻, nullable)
├── workDuration: WorkDuration (実労働時間, calculated)
├── overtimeDuration: WorkDuration (残業時間, calculated)
├── nightDuration: WorkDuration (深夜時間, calculated)
├── version: Long (楽観ロック)
├── createdAt: LocalDateTime
└── updatedAt: LocalDateTime
```

**ビジネスルール:**
- 残業 = 実労働時間 - 8時間（8時間超の場合のみ）
- 深夜時間 = 22:00〜5:00 に該当する労働時間
- 同一社員・同一日付の組み合わせは一意

### AttendanceCorrectionRequest（打刻修正申請）

```
AttendanceCorrectionRequest
├── id: Long (PK, auto)
├── employee: Employee (FK, 申請者)
├── attendanceRecord: AttendanceRecord (FK, 修正対象)
├── requestedClockIn: LocalDateTime (修正後の出勤時刻)
├── requestedClockOut: LocalDateTime (修正後の退勤時刻)
├── reason: String (申請理由)
├── status: ApprovalStatus (PENDING / APPROVED / REJECTED)
├── approver: Employee (FK, nullable, 承認者)
├── approvedAt: LocalDateTime (nullable)
├── version: Long (楽観ロック)
├── createdAt: LocalDateTime
└── updatedAt: LocalDateTime
```

### LeaveRequest（休暇申請）

```
LeaveRequest
├── id: Long (PK, auto)
├── employee: Employee (FK, 申請者)
├── leaveType: LeaveType (PAID / HALF_AM / HALF_PM / SPECIAL / COMPENSATORY)
├── startDate: LocalDate (開始日)
├── endDate: LocalDate (終了日)
├── days: BigDecimal (消化日数: 1.0 or 0.5)
├── reason: String (申請理由)
├── status: ApprovalStatus (PENDING / APPROVED / REJECTED)
├── approver: Employee (FK, nullable)
├── approvedAt: LocalDateTime (nullable)
├── version: Long (楽観ロック)
├── createdAt: LocalDateTime
└── updatedAt: LocalDateTime
```

### LeaveBalance（有給残日数）

```
LeaveBalance
├── id: Long (PK, auto)
├── employee: Employee (FK)
├── fiscalYear: Integer (年度)
├── totalDays: BigDecimal (付与日数: 20.0)
├── usedDays: BigDecimal (使用日数)
├── remainingDays: BigDecimal (残日数 = total - used)
├── version: Long (楽観ロック)
├── createdAt: LocalDateTime
└── updatedAt: LocalDateTime
```

### MonthlyAttendanceSummary（月次勤怠集計）

```
MonthlyAttendanceSummary
├── id: Long (PK, auto)
├── employee: Employee (FK)
├── yearMonth: YearMonth (対象年月)
├── totalWorkDuration: WorkDuration (総労働時間)
├── overtimeDuration: WorkDuration (残業時間合計)
├── nightDuration: WorkDuration (深夜時間合計)
├── holidayWorkDuration: WorkDuration (休日出勤時間)
├── paidLeaveDays: BigDecimal (有給取得日数)
├── workingDays: Integer (出勤日数)
├── version: Long (楽観ロック)
├── createdAt: LocalDateTime
└── updatedAt: LocalDateTime
```

## ドメイン関連図

```
┌─────────────┐       ┌──────────────┐
│ Department  │◀──┐   │   Employee   │
│             │   └───│ department   │
│ parent ─────│──┐    │              │
└─────────────┘  │    └──────┬───────┘
      ▲          │           │
      └──────────┘           │
     (self-ref)              │
                             │
        ┌────────────────────┼─────────────────────┐
        │                    │                     │
        ▼                    ▼                     ▼
┌───────────────┐  ┌─────────────────────┐  ┌──────────────┐
│ Attendance    │  │ AttendanceCorrection │  │ LeaveRequest │
│ Record        │  │ Request              │  │              │
│               │◀─│ attendanceRecord     │  │              │
│ employee ─────│  │ employee ────────────│  │ employee ────│
└───────────────┘  │ approver ────────────│  │ approver ────│
        │          └─────────────────────┘  └──────────────┘
        │                                          │
        ▼                                          ▼
┌───────────────────────┐                 ┌──────────────┐
│ MonthlyAttendance     │                 │ LeaveBalance │
│ Summary               │                 │              │
│ employee ─────────────│                 │ employee ────│
└───────────────────────┘                 └──────────────┘
```

## Repository 一覧

| Repository | 主なメソッド |
|-----------|-------------|
| EmployeeRepository | findByEmployeeNumber, findByDepartment |
| DepartmentRepository | findByParent, findByLevel |
| AttendanceRecordRepository | findByEmployeeAndDate, findByEmployeeAndDateBetween |
| AttendanceCorrectionRequestRepository | findByEmployeeAndStatus, findByStatus |
| LeaveRequestRepository | findByEmployeeAndStatus, findByStatus |
| LeaveBalanceRepository | findByEmployeeAndFiscalYear |
| MonthlyAttendanceSummaryRepository | findByEmployeeAndYearMonth |

## Service 一覧

| Service | 責務 |
|---------|------|
| AuthService | 認証・ログイン/ログアウト |
| AttendanceService | 出勤/退勤打刻・勤怠記録管理 |
| AttendanceCorrectionService | 打刻修正申請・承認/却下 |
| LeaveService | 休暇申請・承認/却下・残日数管理 |
| MonthlyReportService | 月次集計の生成・閲覧 |
| EmployeeService | 社員 CRUD |
| DepartmentService | 部署 CRUD |
