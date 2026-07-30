# Architecture Context

## Stack

| Layer       | Technology                                | Role                                         |
| ----------- | ----------------------------------------- | -------------------------------------------- |
| Frontend    | Next.js + TypeScript                      | UI, routing, server components               |
| UI          | Tailwind CSS + shadcn/ui                  | Styling and component system                 |
| Backend     | Spring Boot 3.4 + Java 21 + Maven         | REST API, orchestration, report retrieval    |
| Queue       | RabbitMQ                                  | Job queue for async test execution           |
| Worker      | Spring `@Async` (same JVM)                | Executes adversarial requests concurrently   |
| Database    | PostgreSQL 16 (Docker local, Neon prod)   | Persistent storage for runs, cases, results  |
| AI          | OpenAI SDK (model-swappable via baseURL)   | Payload generation and semantic judgement    |
| Realtime    | WebSocket (STOMP over Spring WebSocket)   | Live streaming of run progress and results   |
| Rate Limit  | Redis 7 (Docker local)                    | Per-IP rate limiting                          |

## System Boundaries

- `frontend/app/` — Next.js pages: landing/input form, live dashboard, shareable report page
- `frontend/components/` — Reusable UI components: Dashboard (live results), ReportCard (shareable)
- `backend/src/main/java/me/kripanshu/backend/controller/` — Spring REST controllers: Project (CRUD), Endpoint (CRUD), plus future POST /api/test, GET /api/report/:id, GET /api/test/:runId/status
- `backend/src/main/java/me/kripanshu/backend/service/` — Business logic: ProjectService, EndPointService, AI calls, orchestration, report aggregation
- `backend/src/main/java/me/kripanshu/backend/entity/` — JPA entities: Project, Endpoint, Baseline, TestSuite, TestRun, TestResult
- `backend/src/main/java/me/kripanshu/backend/repository/` — Spring Data JPA repositories
- `backend/src/main/java/me/kripanshu/backend/dto/` — Request/response DTOs (records): ApiResponse, CreateProjectRequest, ProjectResponse, UpdateProjectRequest, CreateEndPointRequest, EndPointResponse, UpdateEndPointRequest
- `backend/src/main/java/me/kripanshu/backend/config/` — Spring config: WebSocket, RabbitMQ, RestClient, rate limiter
- `backend/src/main/java/me/kripanshu/backend/worker/` — `@Async` job processor consuming RabbitMQ messages

## Storage Model

- **PostgreSQL (local Docker, Neon in prod)**: Persistent state — run metadata, test cases with payloads, test results, findings/verdicts. User-provided auth headers are never persisted, only held in memory during execution.
- **Redis (local Docker)**: Rate limiter counters per IP; ephemeral — no long-term persistence.
- **RabbitMQ (local Docker)**: Job queue for async test execution; transient messages with dead-letter queue on failure.

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
