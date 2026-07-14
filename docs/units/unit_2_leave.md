# Unit 2: 休暇管理

## 目的

有給・半休・特別休暇・代休の申請・承認と残日数管理。
打刻ドメインに依存せず、Unit 1 と**完全に並行実装可能**。

## 依存

- **Unit 0** — Employee Entity, 認証基盤, テストインフラ

## ユーザーストーリー

- US-40: 社員として、有給休暇を申請したい
- US-41: 社員として、半休（午前半休・午後半休）を申請したい
- US-42: 社員として、特別休暇を申請したい
- US-43: 社員として、代休を申請したい
- US-44: 管理者として、休暇申請を承認/却下したい
- US-45: 社員として、有給休暇の残日数を確認したい

## テーブル（Flyway マイグレーション）

| 順序 | ファイル | テーブル |
|------|---------|---------|
| V5 | V5__create_leave_requests.sql | leave_requests |
| V6 | V6__create_leave_balances.sql | leave_balances |
| V7 | V7__seed_leave_balances.sql | 初期データ（全社員に20日付与） |

## API エンドポイント

| メソッド | パス | 説明 | 認証 |
|---------|------|------|------|
| POST | /api/leaves | 休暇申請 | 認証済 |
| GET | /api/leaves | 休暇申請一覧 | 認証済 |
| PUT | /api/leaves/{id}/approve | 休暇承認 | 管理者 |
| PUT | /api/leaves/{id}/reject | 休暇却下 | 管理者 |
| GET | /api/leaves/balance | 有給残日数取得 | 認証済 |

## Backend 実装内容

### 追加ファイル

```
├── controller/
│   └── LeaveController.java
├── service/
│   ├── LeaveService.java               # interface
│   └── impl/
│       └── LeaveServiceImpl.java
├── repository/
│   ├── LeaveRequestRepository.java
│   └── LeaveBalanceRepository.java
├── entity/
│   ├── LeaveRequest.java
│   ├── LeaveBalance.java
│   └── enums/
│       ├── LeaveType.java              # enum
│       └── ApprovalStatus.java         # enum（打刻修正と共用）
├── dto/
│   ├── LeaveRequestCreate.java         # record
│   ├── LeaveRequestResponse.java       # record
│   └── LeaveBalanceResponse.java       # record
```

### ビジネスルール

| ルール | 詳細 |
|--------|------|
| 日数計算 | HALF_AM / HALF_PM → 0.5日、PAID → 日数（startDate〜endDate の営業日数） |
| 残日数チェック | PAID / HALF_* 申請時に残日数 >= 消費日数を確認 |
| 残日数減算 | 承認時に usedDays を加算（申請時ではない） |
| SPECIAL / COMPENSATORY | 残日数を消費しない |
| 自分の申請は自分で承認できない | approver != employee |

### テスト重点

- 残日数不足時の申請拒否
- 承認時の残日数更新（トランザクション整合性）
- 半休の日数計算（0.5日）
- 自己承認防止

## Frontend 実装内容

### 追加ページ

```
├── app/
│   ├── leaves/page.tsx             # 休暇申請画面
│   └── admin/
│       └── leaves/page.tsx         # 休暇承認画面（管理者）
├── components/
│   └── leave/
│       ├── LeaveRequestForm.tsx    # 申請フォーム
│       ├── LeaveRequestList.tsx    # 申請一覧テーブル
│       ├── LeaveBalance.tsx        # 残日数表示
│       └── LeaveApprovalList.tsx   # 承認待ち一覧（管理者）
└── hooks/
    └── useLeave.ts                 # SWR hooks
```

## 完了条件

- [ ] 休暇申請 API が動作する（全種別）
- [ ] 承認 / 却下 API が動作する
- [ ] 承認時に有給残日数が正しく減算される
- [ ] 残日数不足時に申請がエラーになる
- [ ] Service のユニットテスト（カバレッジ 80%+）
- [ ] Frontend で休暇申請・一覧表示・残日数確認ができる
- [ ] Frontend で管理者が承認/却下操作できる

## 見積もり

- Backend: 1〜1.5日
- Frontend: 1日
- 計: 2〜2.5日
