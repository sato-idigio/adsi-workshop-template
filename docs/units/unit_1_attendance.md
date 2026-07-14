# Unit 1: 打刻・勤怠一覧・打刻修正

## 目的

出退勤の打刻、月次勤怠一覧、および打刻修正申請。勤怠ドメインの中核機能。

## 依存

- **Unit 0** — Employee Entity, 認証基盤, テストインフラ

## ユーザーストーリー

- US-10: 社員として、Web画面のボタンで出勤打刻したい
- US-11: 社員として、Web画面のボタンで退勤打刻したい
- US-12: 社員として、当日の打刻状況を確認したい
- US-30: 社員として、自分の月次勤怠一覧を確認したい
- US-40: 社員として、打刻を間違えた場合に修正申請を出したい
- US-41: 管理者として、打刻修正申請を承認/却下したい

## テーブル（Flyway マイグレーション）

| 順序 | ファイル | テーブル |
|------|---------|---------|
| V4 | V4__create_attendance_records.sql | attendance_records |
| V5 | V5__create_attendance_correction_requests.sql | attendance_correction_requests |

## API エンドポイント

### 打刻・勤怠一覧

| メソッド | パス | 説明 | 認証 |
|---------|------|------|------|
| POST | /api/attendance/clock-in | 出勤打刻 | 認証済 |
| POST | /api/attendance/clock-out | 退勤打刻 | 認証済 |
| GET | /api/attendance/today | 当日の打刻状況 | 認証済 |
| GET | /api/attendance/monthly?yearMonth=YYYY-MM | 月次勤怠一覧 | 認証済 |

### 打刻修正

| メソッド | パス | 説明 | 認証 |
|---------|------|------|------|
| POST | /api/attendance-corrections | 修正申請作成 | 認証済 |
| GET | /api/attendance-corrections?status=&page=&size= | 修正申請一覧（ページング） | 認証済 |
| PUT | /api/attendance-corrections/{id}/approve | 修正申請承認 | ADMIN |
| PUT | /api/attendance-corrections/{id}/reject | 修正申請却下 | ADMIN |

## Backend 実装内容

### 追加ファイル

```
├── controller/
│   ├── AttendanceController.java
│   └── AttendanceCorrectionController.java
├── service/
│   ├── AttendanceService.java                  # interface
│   ├── AttendanceCorrectionService.java        # interface
│   └── impl/
│       ├── AttendanceServiceImpl.java
│       └── AttendanceCorrectionServiceImpl.java
├── repository/
│   ├── AttendanceRecordRepository.java
│   └── AttendanceCorrectionRequestRepository.java
├── entity/
│   ├── AttendanceRecord.java
│   └── AttendanceCorrectionRequest.java
├── dto/
│   ├── AttendanceRecordResponse.java           # record
│   ├── CorrectionRequestCreate.java            # record + Bean Validation
│   └── CorrectionRequestResponse.java          # record
└── util/
    └── WorkTimeCalculator.java                 # 残業・深夜計算ユーティリティ
```

### テスト

```
├── controller/
│   ├── AttendanceControllerTest.java
│   └── AttendanceCorrectionControllerTest.java
├── service/
│   ├── AttendanceServiceImplTest.java
│   └── AttendanceCorrectionServiceImplTest.java
└── util/
    └── WorkTimeCalculatorTest.java
```

### Entity: AttendanceRecord

| フィールド | 型 | 備考 |
|-----------|-----|------|
| id | Long | IDENTITY |
| employee | Employee | ManyToOne LAZY |
| date | LocalDate | NOT NULL, UNIQUE(employee_id, date) |
| clockIn | LocalDateTime | |
| clockOut | LocalDateTime | |
| workMinutes | Integer | |
| overtimeMinutes | Integer | |
| nightMinutes | Integer | |
| version | Long | @Version 楽観ロック |

### Entity: AttendanceCorrectionRequest

| フィールド | 型 | 備考 |
|-----------|-----|------|
| id | Long | IDENTITY |
| employee | Employee | ManyToOne LAZY |
| attendanceRecord | AttendanceRecord | ManyToOne LAZY |
| requestedClockIn | LocalDateTime | |
| requestedClockOut | LocalDateTime | |
| reason | String | max 500, NOT NULL |
| status | String | PENDING / APPROVED / REJECTED |
| approver | Employee | nullable |
| approvedAt | LocalDateTime | |
| version | Long | @Version 楽観ロック |

### ビジネスルール（WorkTimeCalculator）

| ルール | 計算 |
|--------|------|
| 実労働時間 | clockOut - clockIn（分単位） |
| 残業時間 | max(0, 実労働時間 - 480分) |
| 深夜時間 | 22:00〜5:00 に重なる時間（分単位、日跨ぎ対応） |

### テスト重点

- WorkTimeCalculator のユニットテスト（境界値: 8h丁度, 22:00跨ぎ, 5:00跨ぎ）
- 出勤打刻の重複防止テスト
- 退勤打刻の前提条件テスト（出勤打刻がないと不可）
- 修正申請の作成・承認・却下フロー
- ADMIN ロールによる一覧フィルタリング

## Frontend 実装内容

### 追加ページ・コンポーネント

```
├── app/
│   ├── page.tsx                    # ダッシュボード（打刻ボタン実装）
│   └── attendance/page.tsx         # 月次勤怠一覧
├── components/
│   └── attendance/
│       ├── ClockButton.tsx         # 出勤/退勤ボタン
│       ├── TodayStatus.tsx         # 当日の打刻状況表示
│       ├── MonthlyTable.tsx        # 月次テーブル
│       ├── CorrectionForm.tsx      # 修正申請フォーム
│       └── CorrectionList.tsx      # 修正申請一覧
└── hooks/
    ├── useAttendance.ts            # 打刻・勤怠一覧 SWR hooks
    └── useCorrection.ts            # 修正申請 SWR hooks + 操作関数
```

### hooks

| Hook/関数 | 用途 |
|-----------|------|
| `useTodayAttendance()` | 当日打刻状況の取得 |
| `useMonthlyAttendance(yearMonth)` | 月次勤怠一覧の取得 |
| `clockIn()` / `clockOut()` | 打刻実行 |
| `useCorrectionList(status?, page, size)` | 修正申請一覧（ページング） |
| `createCorrection(request)` | 修正申請作成 |
| `approveCorrection(id)` / `rejectCorrection(id)` | 承認/却下 |

## 完了条件

- [x] 出勤/退勤打刻 API が動作する
- [x] 勤務時間・残業・深夜時間が正しく計算される
- [x] 月次一覧 API が期間指定で取得できる
- [x] WorkTimeCalculator のユニットテスト
- [x] Frontend でダッシュボードから打刻操作ができる
- [x] Frontend で月次勤怠一覧が表示される
- [x] 打刻修正申請の作成 API が動作する
- [x] 管理者による修正申請の承認/却下 API が動作する
- [x] Frontend で修正申請フォーム・一覧が動作する
- [x] Controller / Service / Util のテストが存在する

## 見積もり

- Backend（打刻・一覧）: 1日
- Backend（修正申請）: 0.5日
- Frontend: 1日
- 計: 2.5日
