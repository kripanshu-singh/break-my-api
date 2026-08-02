# Architecture Context

## Stack

| Layer      | Technology                               | Role                                        |
|------------|------------------------------------------|---------------------------------------------|
| Frontend   | Next.js + TypeScript                     | UI, routing, server components              |
| UI         | Tailwind CSS + shadcn/ui                 | Styling and component system                |
| Backend    | Spring Boot 3.4 + Java 21 + Maven        | REST API, orchestration, report retrieval   |
| Queue      | RabbitMQ                                 | Job queue for async test execution          |
| Worker     | Spring `@Async` (same JVM)               | Executes adversarial requests concurrently  |
| Database   | PostgreSQL 16 (Docker local, Neon prod)  | Persistent storage for runs, cases, results |
| AI         | OpenAI SDK (model-swappable via baseURL) | Payload generation and semantic judgement   |
| Realtime   | WebSocket (STOMP over Spring WebSocket)  | Live streaming of run progress and results  |
| Rate Limit | Redis 7 (Docker local)                   | Per-IP rate limiting                        |

## System Boundaries

- `frontend/app/` — Next.js pages: landing/input form, live dashboard, shareable report page
- `frontend/components/` — Reusable UI components: Dashboard (live results), ReportCard (shareable)
- `backend/src/main/java/me/kripanshu/backend/controller/` — Spring REST controllers: Project (CRUD), Endpoint (CRUD), TestSuite (CRUD), Baseline (upsert/probe), plus future POST /api/test, GET /api/report/:id, GET /api/test/:runId/status
- `backend/src/main/java/me/kripanshu/backend/service/` — Business logic: ProjectService, EndPointService, TestSuiteService, BaselineService, AI calls, orchestration, report aggregation
- `backend/src/main/java/me/kripanshu/backend/entity/` — JPA entities: Project, Endpoint, Baseline, TestSuite, TestRun, TestResult
- `backend/src/main/java/me/kripanshu/backend/repository/` — Spring Data JPA repositories
- `backend/src/main/java/me/kripanshu/backend/dto/` — Request/response DTOs (records): ApiResponse, CreateProjectRequest, ProjectResponse, UpdateProjectRequest, CreateEndPointRequest, EndPointResponse, UpdateEndPointRequest, CreateBaselineRequest, BaselineResponse, ProbeResult
- `backend/src/main/java/me/kripanshu/backend/config/` — Spring config: WebSocket, RabbitMQ, RestClient, rate limiter
- `backend/src/main/java/me/kripanshu/backend/worker/` — `@Async` job processor consuming RabbitMQ messages

## Storage Model

### Data Flow Overview

```text
Project (Payment Gateway)
 ├── Endpoint (POST /v1/charges)
 │    └── Baseline (Golden response: 200 OK, JSON schema, <500ms)
 │
 └── Test Suite (Boundary Fuzzing & Regressions)
      └── Test Run #101 (Executed at 10:00 PM)
           ├── Test Result 1: Standard Request (PASSED)
           ├── Test Result 2: Null Amount Mutation (FAILED - 500 Error)
           └── Test Result 3: String Overflow Mutation (PASSED - 400 Bad Request)
```

### Services

- **PostgreSQL (local Docker, Neon in prod)**: Persistent state — run metadata, test cases with payloads, test results, findings/verdicts. User-provided auth headers are never persisted, only held in memory during execution.
- **Redis (local Docker)**: Rate limiter counters per IP; ephemeral — no long-term persistence.
- **RabbitMQ (local Docker)**: Job queue for async test execution; transient messages with dead-letter queue on failure.

### Database Schema (6 Tables)

#### 1. `projects` (Top-Level Container)

Defines the target application or microservice to test. Stores base host URL and global identity.

| Column | Type | Notes |
|--------|------|-------|
| `id` | UUID | PK, auto-generated |
| `name` | VARCHAR | Not null |
| `description` | VARCHAR | Not null |
| `base_url` | VARCHAR | Not null |
| `version` | VARCHAR | Nullable |
| `created_at` | TIMESTAMP | Not null, auto-set |
| `updated_at` | TIMESTAMP | Auto-set |

#### 2. `endpoints` (Target Routes)

Belongs to a **Project** (1:N). Each row represents a single API route, its HTTP method, and expected request payload format.

| Column | Type | Notes |
|--------|------|-------|
| `id` | UUID | PK |
| `project_id` | UUID | FK → projects, not null |
| `path` | VARCHAR | Not null (e.g. `/v1/charges`) |
| `method` | VARCHAR | Not null (e.g. `POST`) |
| `headers` | TEXT | JSON string, nullable |
| `query_params` | TEXT | JSON string, nullable |
| `body_schema` | TEXT | JSON schema, nullable |
| `created_at` | TIMESTAMP | Not null, auto-set |
| `updated_at` | TIMESTAMP | Auto-set |

#### 3. `expected_responses` (Baseline — Golden Standard Snapshot)

Mapped to the **Baseline** entity. Has a **1:1 relationship** with an **Endpoint**. Records what a successful, healthy API response looks like. The fuzzing engine compares future runs against this baseline to spot bugs or performance regressions.

| Column | Type | Notes |
|--------|------|-------|
| `id` | UUID | PK |
| `endpoint_id` | UUID | FK → endpoints, unique, not null |
| `expected_status` | INTEGER | Not null (e.g. 200) |
| `expected_schema` | TEXT | JSON schema of expected response |
| `sample_response` | TEXT | Baseline snapshot body |
| `max_latency_ms` | BIGINT | SLA threshold (e.g. 500ms) |
| `created_at` | TIMESTAMP | Not null, auto-set |
| `updated_at` | TIMESTAMP | Auto-set |

#### 4. `test_suites` (Test Blueprint)

Belongs to a **Project** (1:N). Defines how BreakMyAPI should test endpoints — concurrency limits, active mutators, schedule configs.

| Column | Type | Notes |
|--------|------|-------|
| `id` | UUID | PK |
| `project_id` | UUID | FK → projects, not null |
| `name` | VARCHAR | Not null (e.g. `Boundary Fuzzing & Regressions`) |
| `configuration` | TEXT | JSON config (maxConcurrency, perRequestTimeoutMs, maxResponseSizeBytes, runDurationLimitSeconds, judgeSuspiciousOnly, mutators) |
| `created_at` | TIMESTAMP | Not null, auto-set |
| `updated_at` | TIMESTAMP | Auto-set |

Every project gets a `Default Fuzzing Profile` test suite auto-created on project creation, so a user can run tests without touching settings. The suite `configuration` is a typed `TestSuiteConfig` record that is validated at the API boundary and merged against defaults before persistence.

#### 5. `test_runs` (Execution Event)

Belongs to a **TestSuite** (1:N). Generated when a suite is triggered. Tracks high-level execution stats.

| Column | Type | Notes |
|--------|------|-------|
| `id` | UUID | PK |
| `suite_id` | UUID | FK → test_suites, not null |
| `status` | ENUM | PENDING, RUNNING, COMPLETED, FAILED |
| `started_at` | TIMESTAMP | Nullable |
| `completed_at` | TIMESTAMP | Nullable |
| `total_tests` | INTEGER | Number of mutated requests sent |
| `failed_tests` | INTEGER | Number of failures exposed |
| `created_at` | TIMESTAMP | Not null, auto-set |
| `updated_at` | TIMESTAMP | Auto-set |

#### 6. `test_results` (Granular Findings)

Belongs to a **TestRun** (1:N). Every mutated payload generates one row — captures exact payload, response, latency, and diff report.

| Column | Type | Notes |
|--------|------|-------|
| `id` | UUID | PK |
| `test_run_id` | UUID | FK → test_runs, not null, cascade delete |
| `mutation_type` | VARCHAR(50) | Not null (e.g. BASELINE, BOUNDARY_NULL) |
| `request_payload` | JSONB | Payload sent to target |
| `response_status` | INTEGER | Not null (actual HTTP status) |
| `response_body` | JSONB | Response body excerpt |
| `latency_ms` | INTEGER | Not null |
| `passed` | BOOLEAN | Not null |
| `diff_report` | JSONB | Expected vs actual diff |
| `severity` | ENUM(VARCHAR 20) | LOW, MEDIUM, HIGH, CRITICAL |

### Relationship & Cascade Summary

| Parent | Child | Type | On Delete Action |
|--------|-------|------|------------------|
| `projects` | `endpoints` | **1:N** | `CASCADE` — deleting a project removes its endpoints |
| `endpoints` | `expected_responses` | **1:1** | `CASCADE` — deleting an endpoint removes its baseline |
| `projects` | `test_suites` | **1:N** | `CASCADE` — deleting a project removes its test suites |
| `test_suites` | `test_runs` | **1:N** | `RESTRICT` — keep run logs even if suite changes |
| `test_runs` | `test_results` | **1:N** | `CASCADE` — deleting a run cleans up its result logs |

## Auth and Access Model

- No user authentication for MVP — the tool is fully anonymous, quota-gated by IP
- Auth headers for target endpoints are provided by the user at submission time, held in memory only, and attached to outgoing test requests
- Reports are shareable via UUID-based URL (hard to guess, not authenticated)
- Rate limiting applied per IP via Redis (5 runs/day/IP default)

## Invariants

1. REST controllers delegate long-lived work to RabbitMQ — controllers never block on test execution
2. User-provided auth headers are never written to the database or logs
3. The AI provider is swappable via OpenAI SDK `baseURL` + `model` config (no separate adapter layer needed)
4. Targets pointing to localhost or private IP ranges are rejected before any processing
5. The system enforces hard caps on concurrency, run duration, payload size, and response size
6. AI responses are validated against a strict JSON schema; parse failure triggers one retry then graceful degradation
