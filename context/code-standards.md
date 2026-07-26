# Code Standards

## General

- Keep modules small and single-purpose — one file = one responsibility
- Fix root causes, do not layer workarounds on top of bugs
- Do not mix unrelated concerns in one component, route, or function
- Prefer boring, replaceable patterns over clever abstractions
- Write docs as code in the same repo; update docs in the same PR as feature changes

## TypeScript (Frontend)

- Strict mode is required throughout the frontend project
- Avoid `any` — use explicit interfaces, discriminated unions, or narrowly scoped types
- Validate unknown external input at system boundaries (API edge, AI responses) before trusting it
- Define shared types in a common package or types file consumed by both frontend and types generator
- Use branded types for IDs (e.g. `RunId` instead of raw `string`)

## Next.js

- Default to server components — add `'use client'` only when browser interactivity requires it
- Keep page components focused on composition; move logic to hooks or server actions
- Route handlers (`app/api/`) are for lightweight proxying — heavy orchestration lives in the backend
- Report pages (`/report/[id]`) fetch data server-side and render static content for shareability

## Spring Boot / Java

- Use Java 21 LTS features (records for DTOs, pattern matching, text blocks)
- Validate request bodies with Jakarta Validation (`@Valid`, `@NotBlank`, `@Positive`, etc.) at the controller boundary — never trust raw input
- Use records or Java POJOs for request/response DTOs; keep entities separate from DTOs (never expose JPA entities directly as API responses)
- Return consistent response shapes: use a standard `ApiResponse<T>` wrapper with `data`, `error`, `meta` fields
- Use SLF4J + MDC for structured logging with runId and correlation IDs
- Rate limiting is applied at the controller level via interceptor or filter, not in service logic
- Use constructor injection (no field injection with `@Autowired`)
- Keep controllers thin — orchestration belongs in service layer
- Use `@Async` for fire-and-forget background work (worker), not for request-scoped operations

## Styling

- Use CSS custom property tokens defined in `ui-context.md` — no hardcoded hex values
- Follow the border radius scale defined in `ui-context.md`
- Responsive design with mobile-first breakpoints
- Dark mode only — no light mode variants

## shadcn/ui

- Install new components via `npx shadcn@latest add` — never write Radix primitives from scratch
- Components in `components/ui/` are auto-generated; do not edit them manually
- Add `'use client'` only at the shadcn component boundary — keep page logic in server components

## API Routes (Spring Boot Controllers)

- `POST /api/test` — validates input via Jakarta Validation, calls AI via service, creates DB records via JPA, publishes message to RabbitMQ, returns runId
- `GET /api/report/:runId` — fetches run, cases, results, findings — returns full report model
- `GET /api/test/:runId/status` — returns lightweight run status summary
- All routes reject targets on localhost/private IP ranges before any processing
- Auth headers accepted in request body are used in-memory only — never logged or persisted
- Use `@RestController`, `@RequestMapping`, and consistent HTTP status codes in responses

## Data and Storage

- Runs, test cases, test results, and findings are separate tables with foreign key relationships
- Use append-only result events for auditability (test_results are never mutated, only inserted)
- Keep nullable columns explicit and documented in the schema
- Large response bodies are stored as excerpts (first N chars), not full content
- Cost per run is tracked in the runs table and exposed in run metadata

## AI Provider Layer

- Use OpenAI SDK (Java) — provider/model is swappable via `baseUrl` and `model` config in `application.properties`
- Default: Gemini via OpenAI-compatible endpoint (free tier); switch to OpenAI/Claude by changing config
- AI responses are validated against a JSON schema on parse; retry once with corrective prompt on failure, then fail gracefully
- Prompts are versioned (stored as resource files or constants) so regressions are trackable
- Enforce payload uniqueness and required coverage categories in the generation prompt

## File Organization

- `frontend/` — Next.js application
  - `app/` — Pages and layouts
  - `components/` — Reusable React components
  - `components/ui/` — shadcn/ui primitives (auto-generated, do not edit manually)
  - `lib/` — Frontend utilities, API client, types
- `backend/` — Spring Boot application (Maven)
  - `src/main/java/com/breakmyapi/`
    - `controller/` — REST controllers
    - `service/` — Business logic and AI orchestration
    - `model/` — JPA entities
    - `repository/` — Spring Data JPA repositories
    - `config/` — Spring configuration (WebSocket, RabbitMQ, RestClient, rate limiter)
    - `worker/` — `@Async` job processor consuming RabbitMQ messages
    - `dto/` — Request/response DTOs (records)
  - `src/main/resources/` — `application.properties`, prompt templates, static resources
  - `pom.xml` — Maven build configuration
- `docker-compose.yml` — Local infrastructure (PostgreSQL, Redis, RabbitMQ)
