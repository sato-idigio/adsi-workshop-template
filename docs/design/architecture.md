# アーキテクチャ設計

## 技術スタック

| レイヤー | 技術 | バージョン |
|---------|------|-----------|
| Frontend | Next.js (TypeScript) | 15.x |
| UI | Tailwind CSS | 4.x |
| Backend | Spring Boot (Java) | 3.x (Java 21) |
| DB | PostgreSQL | 16 |
| マイグレーション | Flyway | 10.x |
| 認証 | Spring Security + JWT | — |
| ビルド | Gradle (backend) / npm (frontend) | — |
| テスト (BE) | JUnit 5 + Mockito + AssertJ | — |
| テスト (FE) | Vitest + Testing Library | — |

## システム構成図

```
┌─────────────────────────────────────────────────────┐
│ Browser (PC)                                         │
└──────────────────────────┬──────────────────────────┘
                           │ HTTP
                           ▼
┌─────────────────────────────────────────────────────┐
│ Next.js (port 3000)                                  │
│ ┌─────────────────┐  ┌───────────────────────────┐  │
│ │ Server Component │  │ API Routes (proxy)        │  │
│ │ (SSR/SSG)        │  │ /api/** → localhost:8080  │  │
│ └─────────────────┘  └───────────────────────────┘  │
└──────────────────────────┬──────────────────────────┘
                           │ HTTP (localhost)
                           ▼
┌─────────────────────────────────────────────────────┐
│ Spring Boot (port 8080)                              │
│ ┌───────────────────────────────────────────────┐   │
│ │ Controller Layer                               │   │
│ │ @RestController — REST API endpoints           │   │
│ ├───────────────────────────────────────────────┤   │
│ │ Service Layer (interface + impl)               │   │
│ │ ビジネスロジック・ドメインルール               │   │
│ ├───────────────────────────────────────────────┤   │
│ │ Repository Layer (Spring Data JPA)             │   │
│ │ DB アクセス                                    │   │
│ └───────────────────────────────────────────────┘   │
└──────────────────────────┬──────────────────────────┘
                           │ JDBC
                           ▼
┌─────────────────────────────────────────────────────┐
│ PostgreSQL (port 5432)                               │
│ Database: attendance_db                              │
└─────────────────────────────────────────────────────┘
```

## Backend レイヤード構成

```
com.example.attendance
├── config/              # Spring 設定（Security, CORS 等）
├── controller/          # REST Controller
├── service/             # Service interface
│   └── impl/            # Service 実装
├── repository/          # Spring Data JPA Repository
├── entity/              # JPA Entity
├── dto/                 # Request/Response DTO (record)
├── exception/           # カスタム例外 + @RestControllerAdvice
└── util/                # ユーティリティ（時間計算等）
```

## 依存方向

```
Controller → Service (interface) → Repository (interface) → Entity
     ↑                                                         ↑
     │                                                         │
    DTO                                                    Entity
（リクエスト/レスポンス）                              （DB マッピング）
```

- Controller は Service interface のみ依存（実装は DI）
- Controller と外部の間は DTO（record）でやりとり
- Entity を API レスポンスに直接使わない
- Service 層に `@Transactional` を付与

## 認証方式

| 項目 | 設計 |
|------|------|
| 方式 | JWT (stateless) |
| トークン格納 | Authorization: Bearer ヘッダー |
| トークン有効期限 | 24時間 |
| パスワード保存 | BCrypt |
| 未認証時 | 401 Unauthorized |
| 権限不足時 | 403 Forbidden |

### Security 設定方針

```
公開パス:
  - POST /api/auth/login
  - GET /api/health

管理者限定:
  - /api/admin/**
  - /api/employees (POST/PUT)
  - /api/departments (POST/PUT)
  - /api/reports/**

認証必須（全ロール）:
  - それ以外の /api/**
```

## エラーハンドリング

`@RestControllerAdvice` でグローバルに処理する。

| 例外 | HTTP Status | レスポンス |
|------|-------------|-----------|
| バリデーションエラー | 400 | フィールド名 + メッセージの配列 |
| 認証失敗 | 401 | 汎用メッセージ |
| 権限不足 | 403 | 汎用メッセージ |
| リソース未存在 | 404 | 汎用メッセージ |
| 一意制約違反 | 409 | 重複内容を示すメッセージ |
| 楽観ロック競合 | 409 | 再取得を促すメッセージ |
| サーバー内部エラー | 500 | 汎用メッセージ（詳細はログ） |

## 横断的関心事

| 関心事 | 実装 |
|--------|------|
| ログ | SLF4J + Logback |
| バリデーション | Bean Validation |
| 楽観ロック | @Version |
| CORS | SecurityFilterChain で設定 |
| ページネーション | Spring Data Pageable |
| 日時 | LocalDate / LocalDateTime (JVM タイムゾーン: Asia/Tokyo) |

## Frontend 設計方針

| 方針 | 詳細 |
|------|------|
| レンダリング | Server Component デフォルト、インタラクション必要部分のみ Client |
| 状態管理 | SWR でサーバーステート管理 |
| 認証 | JWT をメモリ（Context）に保持 + Cookie httpOnly も検討 |
| API 通信 | `/api/**` → Next.js rewrites → backend |
| フォーム | React Hook Form + Zod |
| スタイル | Tailwind CSS |

## SageMaker プレビュー対応

- `SAGEMAKER=1` 時は basePath `/codeeditor/default/absports/3000` を付与
- `withBasePath()` ユーティリティを全 fetch に適用
- proxy 構成: `:3000`(proxy) → `:3001`(next) + `:8080`(backend)
