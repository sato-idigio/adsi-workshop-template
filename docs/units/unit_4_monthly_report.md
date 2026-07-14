# Unit 4: 月次レポート

## 目的

管理者向けの月次勤怠集計レポート。
AttendanceRecord を月単位で集計し、部署別・社員別に閲覧できる。

## 依存

- **Unit 0** — Employee / Department Entity, 認証基盤
- **Unit 1** — AttendanceRecord Entity・Repository（集計対象データ）

## ユーザーストーリー

- US-50: 管理者として、月次勤怠集計レポートを閲覧したい
- US-31: 管理者として、全社員の月次勤怠一覧を確認したい

## テーブル（Flyway マイグレーション）

| 順序 | ファイル | テーブル |
|------|---------|---------|
| V9 | V9__create_monthly_attendance_summaries.sql | monthly_attendance_summaries |

## API エンドポイント

| メソッド | パス | 説明 | 認証 |
|---------|------|------|------|
| GET | /api/reports/monthly | 月次集計レポート | 管理者 |
| POST | /api/reports/monthly/generate | 月次集計を生成/再生成 | 管理者 |

## Backend 実装内容

### 追加ファイル

```
├── controller/
│   └── MonthlyReportController.java
├── service/
│   ├── MonthlyReportService.java           # interface
│   └── impl/
│       └── MonthlyReportServiceImpl.java
├── repository/
│   └── MonthlyAttendanceSummaryRepository.java
├── entity/
│   └── MonthlyAttendanceSummary.java
├── dto/
│   └── MonthlySummaryResponse.java         # record
```

### ビジネスルール

| ルール | 詳細 |
|--------|------|
| 集計対象 | 指定月の全 AttendanceRecord |
| 総労働時間 | 月内の workMinutes 合計 |
| 残業時間 | 月内の overtimeMinutes 合計 |
| 深夜時間 | 月内の nightMinutes 合計 |
| 休日出勤時間 | 土日祝の勤務時間合計（祝日は簡易: 土日のみ） |
| 有給取得日数 | 月内の承認済み有給休暇日数（Unit 2 の leave_requests 参照） |
| 出勤日数 | clockIn がある日のカウント |

### テスト重点

- 月次集計の正確性（手計算との突き合わせ）
- 休日判定（土日）
- 有給取得日数の計算（半休 = 0.5日）
- 集計データがない月の処理

## Frontend 実装内容

### 追加ページ

```
├── app/
│   └── admin/
│       └── reports/page.tsx          # 月次レポート画面（管理者）
├── components/
│   └── admin/
│       ├── MonthlyReportTable.tsx    # 集計テーブル
│       └── ReportFilter.tsx          # 年月・部署フィルター
└── hooks/
    └── useMonthlyReport.ts           # SWR hooks
```

## 完了条件

- [ ] 月次集計生成 API が動作する
- [ ] 総労働・残業・深夜・休日出勤時間が正しく集計される
- [ ] 部署フィルターが機能する
- [ ] ページネーションが動作する
- [ ] Service のユニットテスト（カバレッジ 80%+）
- [ ] Frontend で管理者がレポートを閲覧できる

## 見積もり

- Backend: 1日
- Frontend: 0.5日
- 計: 1.5日

## 備考

- 有給取得日数は Unit 2 (leave_requests) のデータを参照するが、
  Unit 2 が未完成でも集計ロジック自体は 0 として処理可能。
  統合テスト時に Unit 2 データと合わせて検証する。
