# Unit 3: 打刻修正（申請・承認）

## 目的

打刻忘れ・誤りの修正申請と管理者による承認。
承認時に既存の AttendanceRecord を更新する。

## 依存

- **Unit 0** — Employee Entity, 認証基盤
- **Unit 1** — AttendanceRecord Entity・Repository

## ユーザーストーリー

- US-20: 社員として、打刻を修正する申請を出したい
- US-21: 管理者として、部下の打刻修正申請を承認/却下したい

## テーブル（Flyway マイグレーション）

| 順序 | ファイル | テーブル |
|------|---------|---------|
| V8 | V8__create_attendance_correction_requests.sql | attendance_correction_requests |

## API エンドポイント

| メソッド | パス | 説明 | 認証 |
|---------|------|------|------|
| POST | /api/attendance-corrections | 修正申請 | 認証済 |
| GET | /api/attendance-corrections | 申請一覧 | 認証済 |
| PUT | /api/attendance-corrections/{id}/approve | 承認 | 管理者 |
| PUT | /api/attendance-corrections/{id}/reject | 却下 | 管理者 |

## Backend 実装内容

### 追加ファイル

```
├── controller/
│   └── AttendanceCorrectionController.java
├── service/
│   ├── AttendanceCorrectionService.java       # interface
│   └── impl/
│       └── AttendanceCorrectionServiceImpl.java
├── repository/
│   └── AttendanceCorrectionRequestRepository.java
├── entity/
│   └── AttendanceCorrectionRequest.java
├── dto/
│   ├── CorrectionRequestCreate.java           # record
│   └── CorrectionRequestResponse.java         # record
```

### ビジネスルール

| ルール | 詳細 |
|--------|------|
| 対象レコード存在確認 | attendanceRecordId が存在しない場合 404 |
| 承認時の反映 | AttendanceRecord の clockIn/clockOut を更新し、勤務時間を再計算 |
| 二重申請防止 | 同一レコードに PENDING 状態の申請が既にある場合は拒否 |
| 自己承認防止 | approver != employee |

### テスト重点

- 承認時の AttendanceRecord 更新（clockIn/clockOut + 再計算）
- 二重申請の防止
- 自己承認防止
- 存在しない打刻レコードへの申請

## Frontend 実装内容

### 追加ページ

```
├── app/
│   ├── attendance/
│   │   └── corrections/page.tsx       # 打刻修正申請画面
│   └── admin/
│       └── corrections/page.tsx       # 打刻修正承認画面（管理者）
├── components/
│   └── attendance/
│       ├── CorrectionForm.tsx         # 修正申請フォーム
│       ├── CorrectionList.tsx         # 自分の申請一覧
│       └── CorrectionApprovalList.tsx # 承認待ち一覧（管理者）
└── hooks/
    └── useCorrection.ts               # SWR hooks
```

## 完了条件

- [ ] 打刻修正申請 API が動作する
- [ ] 承認時に AttendanceRecord が正しく更新される
- [ ] 勤務時間が再計算される
- [ ] 二重申請防止が機能する
- [ ] Service のユニットテスト（カバレッジ 80%+）
- [ ] Frontend で修正申請の作成・一覧表示ができる
- [ ] Frontend で管理者が承認/却下操作できる

## 見積もり

- Backend: 0.5〜1日
- Frontend: 0.5〜1日
- 計: 1〜2日
