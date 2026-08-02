# Progress Tracker

Update this file after every meaningful implementation change.

## Current Phase

Phase 2 — Test Suite Domain (Complete)

## Current Goal

Build the Test Suite (testing blueprint) sub-resource with CRUD, typed validated configuration, default fuzzing profile auto-creation, and RESTRICT-aware deletion.

## Completed

- Test Suite domain implemented: entity (existing), DTOs, service, controller, tests
- Docker Compose file created (PostgreSQL 16, Redis 7, RabbitMQ 4)
- application.properties configured for Docker services
- Context files updated for Spring Boot architecture
- Skills installed: shadcn/ui, Next.js App Router, TypeScript advanced types, Tailwind
- pom.xml fixed (Spring Boot 3.4.4, Java 21, all starters added)
- Project compiles successfully (`./mvnw compile`)
- Entities created: Project, Endpoint, Baseline, TestSuite, TestRun, TestResult
- Repositories created: ProjectRepository, EndpointRepository, BaselineRepository, TestSuiteRepository, TestRunRepository, TestResultRepository
- Baseline entity, service, controller (upsert, probe, get-by-endpoint, delete)
- JsonSchemaGenerator and RestClientConfig for probe auto-capture

## In Progress

- None — waiting on next tasks

## 2026-08-02 — Test Suite domain created (DTOs, Service, Controller, default profile)

- Created `MutationType` enum (`entity` package): BOUNDARY, INJECTION, NULL_VALUES, INTEGER_OVERFLOW, LONG_STRINGS, WRONG_TYPES, MALFORMED_AUTH
- Created DTOs:
  - `TestSuiteConfig` — typed record with nullable fields (maxConcurrency, perRequestTimeoutMs, maxResponseSizeBytes, runDurationLimitSeconds, judgeSuspiciousOnly, mutators), `defaults()` factory, and `merge()` that overlays provided fields onto defaults
  - `CreateTestSuiteRequest` — `@NotBlank` name + optional config
  - `UpdateTestSuiteRequest` — all-optional name/config
  - `TestSuiteResponse` — full suite with resolved config + timestamps
- Created `TestSuiteService` with CRUD + business logic:
  - `createTestSuite(projectId, request)` — project existence check, config merge + bounds validation (concurrency 1–50, timeout 1–60s, response size ≤1MB, duration ≤1h, ≥1 mutator), save
  - `getTestSuite(projectId, id)` / `getTestSuitesByProjectId(projectId)` — read-only; verifies suite belongs to the project in path (404 otherwise)
  - `updateTestSuite(projectId, id, request)` — partial update; rejects blank name; revalidates merged config
  - `deleteTestSuite(projectId, id)` — returns 409 `ResourceInUseException` when test runs exist (RESTRICT invariant — run history retained), else deletes
  - `createDefaultSuite(project)` — auto-creates a `Default Fuzzing Profile` suite with default config
- Created `TestSuiteController` with fully nested sub-resource routes under `/api/projects/{projectId}/test-suites`: POST (201), GET list (200), GET by id (200), PUT (200), DELETE (204)
- Created `ResourceInUseException` (409) + `GlobalExceptionHandler` handler
- Added `existsBySuiteId` to `TestRunRepository`
- Refactored `ProjectService` to constructor injection and auto-creates the Default Fuzzing Profile suite on project creation (zero-friction default for users)
- Added 7 unit tests for `TestSuiteService` (defaults merge, partial merge, invalid concurrency, missing project, cross-project 404, delete-with-runs 409, delete success)
- Project compiles and all 8 tests pass (`./mvnw test`)

## 2026-07-30 — Enhanced ExpectedResponse → Baseline (upsert, probe, sub-resource routes)

- Renamed entity `ExpectedResponse` → `Baseline` (table `expected_responses` preserved)
- Renamed repository `ExpectedResponseRepository` → `BaselineRepository`
- Replaced CRUD DTOs: `CreateBaselineRequest`, `BaselineResponse` (removed `UpdateExpectedResponseRequest`)
- Added `ProbeResult` DTO for probe response
- Created `BaselineService` with 4 operations:
  - `createOrUpdateBaseline(endpointId, request)` — upsert: fetch or create baseline, then save
  - `getBaselineByEndpoint(endpointId)` — query by endpoint FK, 404 if none
  - `deleteBaseline(endpointId)` — remove by endpoint FK, keep parent endpoint intact
  - `probeAndSetBaseline(endpointId)` — dispatch real HTTP request via `RestClient`, capture status/latency/body, auto-derive JSON Schema, save/update baseline
- Created `BaselineController` with sub-resource routes under `/api/v1/endpoints/{endpointId}/baseline`:
  - `POST` — create or update baseline (201)
  - `POST /probe` — probe endpoint and auto-capture baseline (200)
  - `GET` — get baseline by endpoint (200)
  - `DELETE` — delete baseline (204)
- Created `RestClientConfig` — `RestClient` bean for probe HTTP dispatch
- Created `JsonSchemaGenerator` — lightweight utility that infers JSON Schema (draft-07) from a JSON response body using Jackson
- Deleted all old `ExpectedResponse*` files
- Project compiles successfully

## 2026-07-30 — Renamed Baseline → ExpectedResponse across the entire codebase

- Renamed entity, repository, all 3 DTOs, service, and controller
- Updated all Java references (imports, type names, variable names, method names, log messages)
- Updated table name from `baselines` to `expected_responses`
- Updated API route from `/api/baselines` to `/api/expected-responses`
- Updated all context file references in `architecture.md` and `progress-tracker.md`
- Project compiles cleanly with zero remaining "Baseline"/"baseline" references in Java files

## 2026-07-30 — ExpectedResponse CRUD created (DTOs, Service, Controller)

- Created DTOs:
  - `CreateExpectedResponseRequest` — `@NotNull` + `@Min(100)` `@Max(599)` validation on `expectedStatus`
  - `ExpectedResponseResponse` — full response with all expected response fields + timestamps
  - `UpdateExpectedResponseRequest` — all optional fields for partial updates
- Created `ExpectedResponseService` with full CRUD + business validation:
  - `createExpectedResponse` — validates endpoint exists, enforces 1:1 (no duplicate expected response per endpoint)
  - `getExpectedResponse` — find by id or 404
  - `getExpectedResponseByEndpointId` — find by endpoint FK with existence check
  - `updateExpectedResponse` — partial update with field-level validation for status range and latency bounds
  - `deleteExpectedResponse` — exists check then delete
- Created `ExpectedResponseController` with 5 REST endpoints:
  - `POST /api/expected-responses/{endpointId}` — 201 Created
  - `GET /api/expected-responses/{id}` — 200 OK
  - `GET /api/expected-responses/by-endpoint/{endpointId}` — 200 OK (1:1 lookup)
  - `PUT /api/expected-responses/{id}` — 200 OK
  - `DELETE /api/expected-responses/{id}` — 204 No Content
- Project compiles successfully

## 2026-07-30 — Data model documentation added to architecture.md

- Expanded the Storage Model section in `architecture.md` with:
  - Data flow diagram showing how all 6 tables work together (e-commerce payment service scenario)
  - Detailed per-table schema documentation (columns, types, notes)
  - Real-world example rows for each table
  - Relationship and cascade summary table

## Next Up

1. Add health check endpoint (`GET /api/health`)
2. Build TestRun domain (CRUD + run status) — suites need a run-triggering endpoint (`POST /api/test-suites/{id}/run`) once the worker exists
3. Configure RabbitMQ queue and `@Async` worker skeleton
4. Add Redis rate limiter filter
5. Add DTO validation error tests

## 2026-07-30 — Endpoint CRUD (Entity, DTOs, Service, Controller)

- Added `createdAt`/`updatedAt` timestamp fields to `Endpoint` entity with `@PrePersist`/`@PreUpdate` lifecycle callbacks
- Created DTOs:
  - `CreateEndPointRequest` — Jakarta `@NotBlank` + `@Pattern` validation for path (must start with `/`) and method (whitelist)
  - `EndPointResponse` — full response with all endpoint fields + timestamps
  - `UpdateEndPointRequest` — all optional fields for partial updates
- Cleaned up `EndPointRepository` — removed auto-generated NetBeans comments
- Created `EndPointService` with full CRUD + business validation:
  - `createEndPoint` — validates project exists, validates method/path, saves
  - `getEndpoint` — find by id or 404
  - `getEndpointsByProjectId` — list by project with existence check
  - `updateEndpoint` — partial update with field-level validation
  - `deleteEndpoint` — exists check then delete
  - Service-level `validateMethod()` and `validatePath()` guarding against invalid input
- Created `EndPointController` with 5 REST endpoints:
  - `POST /api/endpoints/{projectId}` — 201 Created
  - `GET /api/endpoints/{id}` — 200 OK
  - `GET /api/endpoints/project/{projectId}` — 200 OK (list by project)
  - `PUT /api/endpoints/{id}` — 200 OK
  - `DELETE /api/endpoints/{id}` — 204 No Content
- Fixed `@Controller` → `@RestController`, broken route templates, and return status codes
- Updated `architecture.md` to reflect actual package paths (`me.kripanshu.backend.*`)

## 2026-07-27 — ProjectController created

- Created `ProjectController` in `controller/` package with constructor injection
- Endpoints: `POST /api/projects`, `GET /api/projects/{id}`, `GET /api/projects`, `PUT /api/projects/{id}`, `DELETE /api/projects/{id}`
- Uses `@Valid` for request validation, `ApiResponse<T>` wrapper, proper HTTP status codes (201 for create, 204 for delete)
- Delegates all logic to `ProjectService` — controller stays thin

## 2026-07-27 — Global and app exception handler created

- Created custom exception hierarchy:
  - `ResourceNotFoundException` (404) — structured not-found errors with resource name and ID
  - `DuplicateResourceException` (409) — conflict errors with resource name, field, and value
  - `BadRequestException` (400) — general bad request errors
- Created `GlobalExceptionHandler` (`@RestControllerAdvice`) handling:
  - Custom exceptions → appropriate HTTP status codes
  - `MethodArgumentNotValidException` (400) with per-field error details
  - `ConstraintViolationException` (400) with constraint violation details
  - `HttpMessageNotReadableException` (400), `MissingServletRequestParameterException` (400), `MethodArgumentTypeMismatchException` (400)
  - `NoSuchElementException` (404), `IllegalArgumentException` (400)
  - Fallback `Exception` (500) with error logging
  - All responses use consistent `ApiResponse<T>` shape with timestamp in meta
- Updated `ProjectService` to throw `ResourceNotFoundException` and `DuplicateResourceException` instead of raw Java exceptions

## Open Questions

- What is the target cloud provider for production? (Neon for Postgres, Railway for Spring Boot backend, Vercel for frontend — confirm)
- Gemini or OpenAI for AI calls? (Using OpenAI SDK with swappable baseURL — confirm default provider)
- Should we add Springdoc/OpenAPI for API documentation?

## Architecture Decisions

- **Spring Boot + Maven over Express**: The backend is now Spring Boot 3.4 with Java 21 + Maven. Better portfolio value, cleaner REST APIs, built-in JPA/WebSocket/AMQP support.
- **RabbitMQ over BullMQ**: Native Spring AMQP support eliminates separate worker process. `@Async` on the consumer handles concurrency. No separate deployable needed.
- **OpenAI SDK over custom adapter**: The OpenAI Java SDK supports custom `baseUrl` — swap between Gemini (free via OpenAI-compatible endpoint), OpenAI, or Claude by changing config. No adapter layer needed.
- **PostgreSQL in Docker for local dev, Neon for prod**: PostgreSQL chosen over SQLite for Spring Boot compatibility (JPA/Hibernate is PostgreSQL-native). Neon free tier for production.
- **No user auth for MVP**: Anonymous usage with IP-based rate limiting reduces friction and accelerates time to first run.
- **Vercel for frontend, Railway for backend**: Frontend on Vercel Free (Next.js), backend on Railway free tier (Java/Spring Boot container).

## Session Notes

- Context files generated on 2026-07-25 from project documentation in Doc/ folder
- Switched from Express/Node to Spring Boot/Java on user request for resume value
- Docker Compose chosen over native installs for Postgres/Redis/RabbitMQ — zero machine pollution
- OpenAI SDK chosen over custom provider adapter — model-swappable via baseURL out of the box

## 2026-07-26 — DTO layer created

- Created `ApiResponse<T>` generic response wrapper record (data, error, meta) with static `success()` and `error()` factory methods
- Created `CreateProjectRequest` record with Jakarta `@NotBlank` validation on name, description, baseUrl; optional version
- Created `UpdateProjectRequest` record with all optional fields for partial updates
- Created `ProjectResponse` record exposing id, name, description, baseUrl, version, createdAt, updatedAt
- Project compiles successfully
