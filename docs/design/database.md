# DB 設計

Flyway マイグレーションで管理する。DDL は PostgreSQL 準拠。

## テーブル一覧

| テーブル | 説明 |
|---------|------|
| departments | 部署マスタ（自己参照で階層） |
| employees | 社員マスタ |
| attendance_records | 日次出退勤レコード |
| attendance_correction_requests | 打刻修正申請 |
| leave_requests | 休暇申請 |
| leave_balances | 有給残日数 |
| monthly_attendance_summaries | 月次集計 |

## テーブル定義

### departments

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | BIGSERIAL | PK | |
| name | VARCHAR(100) | NOT NULL | 部署名 |
| parent_id | BIGINT | FK(departments.id), NULL可 | 親部署（NULLで最上位） |
| level | INTEGER | NOT NULL, CHECK(1-3) | 階層レベル |
| version | BIGINT | NOT NULL DEFAULT 0 | 楽観ロック |
| created_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |
| updated_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |

### employees

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | BIGSERIAL | PK | |
| employee_number | VARCHAR(20) | UNIQUE, NOT NULL | 社員番号（ログインID） |
| last_name | VARCHAR(50) | NOT NULL | 姓 |
| first_name | VARCHAR(50) | NOT NULL | 名 |
| email | VARCHAR(255) | UNIQUE, NOT NULL | メールアドレス |
| password | VARCHAR(255) | NOT NULL | BCryptハッシュ |
| department_id | BIGINT | FK(departments.id), NOT NULL | 所属部署 |
| position | VARCHAR(100) | | 役職 |
| role | VARCHAR(20) | NOT NULL, CHECK(EMPLOYEE/ADMIN) | ロール |
| hire_date | DATE | NOT NULL | 入社日 |
| version | BIGINT | NOT NULL DEFAULT 0 | 楽観ロック |
| created_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |
| updated_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |

### attendance_records

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | BIGSERIAL | PK | |
| employee_id | BIGINT | FK(employees.id), NOT NULL | 社員 |
| date | DATE | NOT NULL | 勤務日 |
| clock_in | TIMESTAMP | | 出勤時刻 |
| clock_out | TIMESTAMP | | 退勤時刻 |
| work_minutes | INTEGER | | 実労働時間（分） |
| overtime_minutes | INTEGER | | 残業時間（分） |
| night_minutes | INTEGER | | 深夜時間（分） |
| version | BIGINT | NOT NULL DEFAULT 0 | 楽観ロック |
| created_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |
| updated_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |

**UNIQUE制約:** (employee_id, date)

### attendance_correction_requests

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | BIGSERIAL | PK | |
| employee_id | BIGINT | FK(employees.id), NOT NULL | 申請者 |
| attendance_record_id | BIGINT | FK(attendance_records.id), NOT NULL | 修正対象 |
| requested_clock_in | TIMESTAMP | | 修正後の出勤時刻 |
| requested_clock_out | TIMESTAMP | | 修正後の退勤時刻 |
| reason | VARCHAR(500) | NOT NULL | 申請理由 |
| status | VARCHAR(20) | NOT NULL DEFAULT 'PENDING' | PENDING/APPROVED/REJECTED |
| approver_id | BIGINT | FK(employees.id) | 承認者 |
| approved_at | TIMESTAMP | | 承認/却下日時 |
| version | BIGINT | NOT NULL DEFAULT 0 | 楽観ロック |
| created_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |
| updated_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |

### leave_requests

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | BIGSERIAL | PK | |
| employee_id | BIGINT | FK(employees.id), NOT NULL | 申請者 |
| leave_type | VARCHAR(20) | NOT NULL | PAID/HALF_AM/HALF_PM/SPECIAL/COMPENSATORY |
| start_date | DATE | NOT NULL | 開始日 |
| end_date | DATE | NOT NULL | 終了日 |
| days | DECIMAL(3,1) | NOT NULL | 消化日数（0.5 or 1.0〜） |
| reason | VARCHAR(500) | NOT NULL | 申請理由 |
| status | VARCHAR(20) | NOT NULL DEFAULT 'PENDING' | PENDING/APPROVED/REJECTED |
| approver_id | BIGINT | FK(employees.id) | 承認者 |
| approved_at | TIMESTAMP | | 承認/却下日時 |
| version | BIGINT | NOT NULL DEFAULT 0 | 楽観ロック |
| created_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |
| updated_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |

### leave_balances

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | BIGSERIAL | PK | |
| employee_id | BIGINT | FK(employees.id), NOT NULL | 社員 |
| fiscal_year | INTEGER | NOT NULL | 年度 |
| total_days | DECIMAL(4,1) | NOT NULL DEFAULT 20.0 | 付与日数 |
| used_days | DECIMAL(4,1) | NOT NULL DEFAULT 0.0 | 使用日数 |
| version | BIGINT | NOT NULL DEFAULT 0 | 楽観ロック |
| created_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |
| updated_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |

**UNIQUE制約:** (employee_id, fiscal_year)

### monthly_attendance_summaries

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | BIGSERIAL | PK | |
| employee_id | BIGINT | FK(employees.id), NOT NULL | 社員 |
| year_month | VARCHAR(7) | NOT NULL | 対象年月（YYYY-MM） |
| total_work_minutes | INTEGER | NOT NULL DEFAULT 0 | 総労働時間（分） |
| overtime_minutes | INTEGER | NOT NULL DEFAULT 0 | 残業時間（分） |
| night_minutes | INTEGER | NOT NULL DEFAULT 0 | 深夜時間（分） |
| holiday_work_minutes | INTEGER | NOT NULL DEFAULT 0 | 休日出勤時間（分） |
| paid_leave_days | DECIMAL(4,1) | NOT NULL DEFAULT 0.0 | 有給取得日数 |
| working_days | INTEGER | NOT NULL DEFAULT 0 | 出勤日数 |
| version | BIGINT | NOT NULL DEFAULT 0 | 楽観ロック |
| created_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |
| updated_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |

**UNIQUE制約:** (employee_id, year_month)

## ER 図

```
departments                    employees
┌──────────────────┐          ┌─────────────────────────┐
│ id (PK)          │◀─────────│ department_id (FK)      │
│ name             │          │ id (PK)                 │
│ parent_id (FK)───│──┐       │ employee_number (UQ)    │
│ level            │  │       │ last_name               │
│ version          │  │       │ first_name              │
└──────────────────┘  │       │ email (UQ)              │
        ▲             │       │ password                │
        └─────────────┘       │ position                │
       (self-ref)             │ role                    │
                              │ hire_date               │
                              │ version                 │
                              └───────────┬─────────────┘
                                          │
               ┌──────────────────────────┼──────────────────────────┐
               │                          │                          │
               ▼                          ▼                          ▼
┌──────────────────────┐   ┌──────────────────────────┐   ┌─────────────────┐
│ attendance_records   │   │ attendance_correction_   │   │ leave_requests  │
│ id (PK)              │◀──│ requests                 │   │ id (PK)         │
│ employee_id (FK)     │   │ id (PK)                  │   │ employee_id(FK) │
│ date                 │   │ employee_id (FK)          │   │ leave_type      │
│ clock_in             │   │ attendance_record_id (FK) │   │ start_date      │
│ clock_out            │   │ requested_clock_in        │   │ end_date        │
│ work_minutes         │   │ requested_clock_out       │   │ days            │
│ overtime_minutes     │   │ reason                    │   │ reason          │
│ night_minutes        │   │ status                    │   │ status          │
│ version              │   │ approver_id (FK)          │   │ approver_id(FK) │
│ UQ(employee_id,date) │   │ approved_at               │   │ approved_at     │
└──────────────────────┘   │ version                   │   │ version         │
               │           └──────────────────────────┘   └─────────────────┘
               ▼                                                    │
┌──────────────────────────┐                              ┌─────────────────┐
│ monthly_attendance_      │                              │ leave_balances  │
│ summaries                │                              │ id (PK)         │
│ id (PK)                  │                              │ employee_id(FK) │
│ employee_id (FK)         │                              │ fiscal_year     │
│ year_month               │                              │ total_days      │
│ total_work_minutes       │                              │ used_days       │
│ overtime_minutes         │                              │ version         │
│ night_minutes            │                              │ UQ(emp,year)    │
│ holiday_work_minutes     │                              └─────────────────┘
│ paid_leave_days          │
│ working_days             │
│ version                  │
│ UQ(employee_id,          │
│    year_month)           │
└──────────────────────────┘
```

## インデックス戦略

| テーブル | インデックス | 用途 |
|---------|------------|------|
| employees | employee_number (UNIQUE) | ログイン検索 |
| employees | department_id | 部署別社員一覧 |
| attendance_records | (employee_id, date) UNIQUE | 日次レコード一意性 |
| attendance_records | (employee_id, date) — range scan | 月次一覧取得 |
| attendance_correction_requests | (status) | 承認待ち一覧 |
| leave_requests | (employee_id, status) | 自分の申請一覧 |
| leave_requests | (status) | 承認待ち一覧 |
| leave_balances | (employee_id, fiscal_year) UNIQUE | 年度別残日数 |
| monthly_attendance_summaries | (employee_id, year_month) UNIQUE | 月次集計一意性 |

## Flyway マイグレーション方針

- ファイル名: `V{番号}__{説明}.sql`（例: `V1__create_departments.sql`）
- 順序: departments → employees → attendance_records → その他
- DDL と初期データ（マスタ）は別ファイルにする
- `ddl-auto=none`（Hibernate による DDL 自動生成は禁止）
