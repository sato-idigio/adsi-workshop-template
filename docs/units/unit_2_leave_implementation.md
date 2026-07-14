# Unit 2: 休暇管理 — 実装記録

## 実装日

2026-07-14

## ブランチ

`feature/unit2-leave`

## 実装内容

### Backend

#### Flyway マイグレーション

| ファイル | 内容 |
|---------|------|
| `V5__create_leave_requests.sql` | leave_requests テーブル + インデックス |
| `V6__create_leave_balances.sql` | leave_balances テーブル（UNIQUE制約付き） |
| `V7__seed_leave_balances.sql` | 全社員に2026年度20日付与 |

#### Enum

| ファイル | 値 |
|---------|-----|
| `entity/enums/LeaveType.java` | PAID, HALF_AM, HALF_PM, SPECIAL, COMPENSATORY |
| `entity/enums/ApprovalStatus.java` | PENDING, APPROVED, REJECTED |

#### Entity

| ファイル | 説明 |
|---------|------|
| `entity/LeaveRequest.java` | 休暇申請。approve()/reject() メソッド持ち |
| `entity/LeaveBalance.java` | 有給残日数。addUsedDays()/getRemainingDays() メソッド持ち |

#### DTO (record)

| ファイル | 用途 |
|---------|------|
| `dto/LeaveRequestCreate.java` | 申請リクエスト（Bean Validation付き） |
| `dto/LeaveRequestResponse.java` | 申請レスポンス（from() ファクトリ） |
| `dto/LeaveBalanceResponse.java` | 残日数レスポンス（from() ファクトリ） |

#### Repository

| ファイル | 主要メソッド |
|---------|-------------|
| `LeaveRequestRepository.java` | findByEmployeeId, findByStatus, findByIdWithEmployee |
| `LeaveBalanceRepository.java` | findByEmployeeIdAndFiscalYear |

#### Service

| ファイル | 役割 |
|---------|------|
| `service/LeaveService.java` | interface |
| `service/impl/LeaveServiceImpl.java` | ビジネスロジック |

**ビジネスルール実装:**

- 日数計算: HALF_AM/HALF_PM → 0.5日、PAID/SPECIAL/COMPENSATORY → 営業日数（土日除外）
- 残日数チェック: PAID/HALF_* のみ。SPECIAL/COMPENSATORY は消費なし
- 残日数減算: 承認時に usedDays を加算（申請時ではない）
- 自己承認防止: approver.id == employee.id でエラー
- 承認済み/却下済みの再操作防止

#### Controller

| エンドポイント | メソッド | 認証 |
|---------------|---------|------|
| `POST /api/leaves` | 休暇申請 | 認証済 |
| `GET /api/leaves` | 一覧取得（ADMIN:全件、一般:自分のみ） | 認証済 |
| `PUT /api/leaves/{id}/approve` | 承認 | ADMIN |
| `PUT /api/leaves/{id}/reject` | 却下 | ADMIN |
| `GET /api/leaves/balance` | 残日数取得 | 認証済 |

#### その他

- `exception/InsufficientLeaveBalanceException.java` — 残日数不足例外
- `config/SecurityConfig.java` — approve/reject を ADMIN 制限に追加
- `exception/GlobalExceptionHandler.java` — InsufficientLeaveBalanceException ハンドラ追加

### Frontend

#### 型定義（`lib/types.ts` に追加）

- `LeaveType`, `ApprovalStatus`
- `LeaveRequestCreate`, `LeaveRequestResponse`, `LeaveBalanceResponse`, `PagedLeaveRequests`

#### Hook

| ファイル | エクスポート |
|---------|-------------|
| `hooks/useLeave.ts` | useLeaveRequests, useLeaveBalance, createLeaveRequest, approveLeaveRequest, rejectLeaveRequest |

#### コンポーネント

| ファイル | 役割 |
|---------|------|
| `components/leave/LeaveBalance.tsx` | 残日数カード（付与/使用/残） |
| `components/leave/LeaveRequestForm.tsx` | 申請フォーム（種別選択、半休時は終了日非表示） |
| `components/leave/LeaveRequestList.tsx` | 申請一覧テーブル（ステータスバッジ付き） |
| `components/leave/LeaveApprovalList.tsx` | 承認待ち一覧（承認/却下ボタン付き） |

#### ページ

| パス | 対象 |
|-----|------|
| `/leaves` | 社員用 — 残日数表示 + 申請フォーム + 申請履歴 |
| `/admin/leaves` | 管理者用 — 承認待ち一覧 |

#### ダッシュボード更新

- `app/page.tsx` — 「休暇管理」リンク（全員）、「休暇承認」リンク（ADMIN）を追加

## テスト

### LeaveServiceImplTest（9件）

1. 有給申請で営業日数を正しく計算（月〜金 = 5日）
2. 半休で 0.5日を設定
3. 残日数不足で InsufficientLeaveBalanceException
4. 特別休暇は残日数チェックしない
5. 承認時にステータス APPROVED + 残日数減算
6. 自己承認防止（IllegalArgumentException）
7. 承認済み申請の再承認エラー
8. 却下でステータス REJECTED
9. 残日数取得で正しい値を返す

### LeaveControllerTest（6件）

1. POST /api/leaves — 201
2. POST /api/leaves — バリデーションエラーで 400
3. GET /api/leaves — 一覧取得 200
4. GET /api/leaves/balance — 残日数取得 200
5. PUT /api/leaves/{id}/approve — 未認証で 401
6. PUT /api/leaves/{id}/approve — EMPLOYEE 権限で 403

## 検証結果

- Backend テスト: 全30件 PASS
- Frontend TypeScript 型チェック: PASS
- 未回答の [Answer]: なし
