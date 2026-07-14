# Unit 0: 共通基盤

## 目的

全 Unit の土台となるプロジェクト骨格・認証・マスタ管理を構築する。
**テストが実行できる状態**まで持っていくことがゴール。

## ユーザーストーリー

- US-01: 社員として、社員番号とパスワードでログインしたい
- US-02: 社員として、ログアウトしたい
- US-60: 管理者として、社員を登録したい
- US-61: 管理者として、社員情報を編集したい
- US-62: 管理者として、部署を管理したい

## テーブル（Flyway マイグレーション）

| 順序 | ファイル | テーブル |
|------|---------|---------|
| V1 | V1__create_departments.sql | departments |
| V2 | V2__create_employees.sql | employees |
| V3 | V3__seed_master_data.sql | 初期データ（部署・管理者アカウント） |

## API エンドポイント

| メソッド | パス | 説明 | 認証 |
|---------|------|------|------|
| POST | /api/auth/login | ログイン | 不要 |
| POST | /api/auth/logout | ログアウト | 必要 |
| GET | /api/auth/me | 現在のユーザー | 必要 |
| GET | /api/employees | 社員一覧 | 管理者 |
| POST | /api/employees | 社員登録 | 管理者 |
| GET | /api/employees/{id} | 社員詳細 | 管理者 |
| PUT | /api/employees/{id} | 社員更新 | 管理者 |
| GET | /api/departments | 部署一覧（ツリー） | 認証済 |
| POST | /api/departments | 部署作成 | 管理者 |
| PUT | /api/departments/{id} | 部署更新 | 管理者 |
| GET | /api/health | ヘルスチェック | 不要 |

## Backend 実装内容

### プロジェクト骨格

- Gradle プロジェクト（Spring Boot 3.x / Java 21）
- 依存: Spring Web, Spring Security, Spring Data JPA, Flyway, PostgreSQL, Lombok, jjwt
- application.yml（dev / test プロファイル）
- Dockerfile（SageMaker 対応: `--network=sagemaker`）

### パッケージ構成

```
com.example.attendance
├── config/
│   ├── SecurityConfig.java          # SecurityFilterChain
│   ├── JwtAuthenticationFilter.java # JWT フィルター
│   └── CorsConfig.java             # CORS 設定
├── controller/
│   ├── AuthController.java
│   ├── EmployeeController.java
│   └── DepartmentController.java
├── service/
│   ├── AuthService.java             # interface
│   ├── EmployeeService.java         # interface
│   ├── DepartmentService.java       # interface
│   └── impl/
│       ├── AuthServiceImpl.java
│       ├── EmployeeServiceImpl.java
│       └── DepartmentServiceImpl.java
├── repository/
│   ├── EmployeeRepository.java
│   └── DepartmentRepository.java
├── entity/
│   ├── Employee.java
│   └── Department.java
├── dto/
│   ├── LoginRequest.java            # record
│   ├── LoginResponse.java           # record
│   ├── EmployeeResponse.java        # record
│   ├── EmployeeCreateRequest.java   # record
│   ├── EmployeeUpdateRequest.java   # record
│   ├── DepartmentResponse.java      # record
│   ├── DepartmentTreeResponse.java  # record
│   ├── DepartmentCreateRequest.java # record
│   ├── DepartmentUpdateRequest.java # record
│   └── ErrorResponse.java           # record
├── exception/
│   ├── GlobalExceptionHandler.java  # @RestControllerAdvice
│   ├── ResourceNotFoundException.java
│   └── DuplicateResourceException.java
└── util/
    └── JwtUtil.java                 # トークン生成・検証
```

### テストインフラ

- `src/test/resources/application-test.yml` — H2 インメモリ DB
- テスト用ベースクラス（`@ActiveProfiles("test")`）
- Controller テスト: `@WebMvcTest` + `@MockitoBean`
- Repository テスト: `@DataJpaTest`
- 統合テスト: `@SpringBootTest` + `@AutoConfigureMockMvc`

## Frontend 実装内容

### プロジェクト骨格

- Next.js 15 (App Router / TypeScript)
- 依存: Tailwind CSS, SWR, React Hook Form, Zod
- next.config.ts（rewrites: `/api/**` → `localhost:8080`）
- SageMaker プレビュー対応（basePath / proxy）

### ページ・コンポーネント

```
src/
├── app/
│   ├── layout.tsx              # 共通レイアウト（ヘッダー・ナビ・認証ガード）
│   ├── page.tsx                # ダッシュボード（枠だけ。打刻は Unit 1）
│   ├── login/page.tsx          # ログイン画面
│   └── admin/
│       ├── employees/page.tsx  # 社員一覧
│       └── departments/page.tsx # 部署管理
├── components/
│   └── ui/                     # Button, Input, Table, Modal 等
├── contexts/
│   └── AuthContext.tsx         # JWT 保持・ログイン状態管理
├── hooks/
│   └── useAuth.ts
└── lib/
    ├── api-client.ts           # fetch ラッパー（withBasePath 対応）
    └── types.ts                # 共通型定義
```

### テストインフラ

- vitest.config.ts
- @testing-library/react セットアップ
- MSW（Mock Service Worker）セットアップ

## 完了条件

- [ ] `./gradlew test` が通る（最低1つの Controller テスト + Repository テスト）
- [ ] `npm test` が通る（最低1つのコンポーネントテスト）
- [ ] ログイン → JWT 取得 → 認証付きリクエスト が動作する
- [ ] 社員 CRUD の API が動作する
- [ ] 部署 CRUD の API が動作する
- [ ] Frontend でログイン → ダッシュボード遷移ができる

## 見積もり

- Backend: 1〜1.5日
- Frontend: 1〜1.5日
- 計: 2〜3日（2人で共同作業）
