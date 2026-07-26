# Architecture Context

## Stack

| Layer       | Technology                           | Role                                         |
| ----------- | ------------------------------------ | -------------------------------------------- |
| Framework   | Next.js + TypeScript                 | Frontend app, routing, server components     |
| UI          | Tailwind CSS + shadcn/ui             | Styling and component system                 |
| Backend     | Node.js / Express + TypeScript       | API intake, orchestration, report retrieval  |
| Queue       | BullMQ + Redis                       | Job queue for test execution, rate limiting  |
| Worker      | Isolated Node.js process             | Executes adversarial requests concurrently   |
| Database    | PostgreSQL via Supabase              | Persistent storage for runs, cases, results  |
| AI          | Google Gemini Flash (default)        | Payload generation and semantic judgement    |
| Realtime    | WebSocket (socket.io)                | Live streaming of run progress and results   |

## System Boundaries

- `frontend/app/` — Next.js pages: landing/input form, live dashboard, shareable report page
- `frontend/components/` — Reusable UI components: Dashboard (live results), ReportCard (shareable)
- `backend/routes/` — Express route handlers: POST /api/test, GET /api/report/:id, GET /api/test/:runId/status
- `backend/workers/` — BullMQ worker process executing payloads against target endpoints
- `backend/ai/` — AI provider adapter (Gemini/Claude) for payload generation and semantic judgement
- `backend/queue/` — BullMQ queue configuration and job definitions
- `backend/socket/` — WebSocket server setup and room management per runId

## Storage Model

- **PostgreSQL (Supabase)**: Persistent state — run metadata, test cases with payloads, test results, findings/verdicts. User-provided auth headers are never persisted, only held in memory during execution.
- **Redis (Railway)**: BullMQ queue state and job data; rate limiter counters per IP; ephemeral — no long-term persistence.

## Auth and Access Model

- No user authentication for MVP — the tool is fully anonymous, quota-gated by IP
- Auth headers for target endpoints are provided by the user at submission time, held in memory only, and attached to outgoing test requests
- Reports are shareable via UUID-based URL (hard to guess, not authenticated)
- Rate limiting applied per IP via Redis (5 runs/day/IP default)

## Invariants

1. Request handlers do not run long-lived background work — all test execution is delegated to BullMQ workers
2. User-provided auth headers are never written to the database or logs
3. The AI provider adapter is swappable via environment variable without changing business logic
4. Targets pointing to localhost or private IP ranges are rejected before any processing
5. The system enforces hard caps on concurrency, run duration, payload size, and response size
6. AI responses are validated against a strict JSON schema; parse failure triggers one retry then graceful degradation
