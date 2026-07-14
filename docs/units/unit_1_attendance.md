# Unit 1: 打刻・勤怠一覧

## 目的

出退勤の打刻と月次勤怠一覧。勤怠ドメインの中核機能。

## 依存

- **Unit 0** — Employee Entity, 認証基盤, テストインフラ

## ユーザーストーリー

- US-10: 社員として、Web画面のボタンで出勤打刻したい
- US-11: 社員として、Web画面のボタンで退勤打刻したい
- US-12: 社員として、当日の打刻状況を確認したい
- US-30: 社員として、自分の月次勤怠一覧を確認したい

## テーブル（Flyway マイグレーション）

| 順序 | ファイル | テーブル |
|------|---------|---------|
| V4 | V4__create_attendance_records.sql | attendance_records |

## API エンドポイント

| メソッド | パス | 説明 | 認証 |
|---------|------|------|------|
| POST | /api/attendance/clock-in | 出勤打刻 | 認証済 |
| POST | /api/attendance/clock-out | 退勤打刻 | 認証済 |
| GET | /api/attendance/today | 当日の打刻状況 | 認証済 |
| GET | /api/attendance/monthly?yearMonth=YYYY-MM | 月次勤怠一覧 | 認証済 |

## Backend 実装内容

### 追加ファイル

```
├── controller/
│   └── AttendanceController.java
├── service/
│   ├── AttendanceService.java           # interface
│   └── impl/
│       └── AttendanceServiceImpl.java
├── repository/
│   └── AttendanceRecordRepository.java
├── entity/
│   └── AttendanceRecord.java
├── dto/
│   └── AttendanceRecordResponse.java    # record
└── util/
    └── WorkTimeCalculator.java          # 残業・深夜計算ユーティリティ
```

### ビジネスルール（WorkTimeCalculator）

| ルール | 計算 |
|--------|------|
| 実労働時間 | clockOut - clockIn（分単位） |
| 残業時間 | max(0, 実労働時間 - 480分) |
| 深夜時間 | 22:00〜5:00 に重なる時間（分単位） |

### テスト重点

- WorkTimeCalculator のユニットテスト（境界値: 8h丁度, 22:00跨ぎ, 5:00跨ぎ）
- 出勤打刻の重複防止テスト
- 退勤打刻の前提条件テスト（出勤打刻がないと不可）

## Frontend 実装内容

### 追加ページ

```
├── app/
│   ├── page.tsx                # ダッシュボード（打刻ボタン実装）
│   └── attendance/page.tsx     # 月次勤怠一覧
├── components/
│   └── attendance/
│       ├── ClockButton.tsx     # 出勤/退勤ボタン
│       ├── TodayStatus.tsx     # 当日の打刻状況表示
│       └── MonthlyTable.tsx    # 月次テーブル
└── hooks/
    └── useAttendance.ts        # SWR hooks
```

## 完了条件

- [ ] 出勤/退勤打刻 API が動作する
- [ ] 勤務時間・残業・深夜時間が正しく計算される
- [ ] 月次一覧 API が期間指定で取得できる
- [ ] WorkTimeCalculator のユニットテスト（カバレッジ 80%+）
- [ ] Frontend でダッシュボードから打刻操作ができる
- [ ] Frontend で月次勤怠一覧が表示される

## 見積もり

- Backend: 1日
- Frontend: 1日
- 計: 2日
