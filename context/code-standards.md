# Code Standards

## General

- Keep modules small and single-purpose — one file = one responsibility
- Fix root causes, do not layer workarounds on top of bugs
- Do not mix unrelated concerns in one component, route, or function
- Prefer boring, replaceable patterns over clever abstractions
- Write docs as code in the same repo; update docs in the same PR as feature changes

## TypeScript

- Strict mode is required throughout the project
- Avoid `any` — use explicit interfaces, discriminated unions, or narrowly scoped types
- Validate unknown external input at system boundaries (API edge, AI responses) before trusting it
- Define shared types in a common package or types file consumed by both frontend and backend
- Use branded types for IDs (e.g. `RunId` instead of raw `string`)

## Next.js

- Default to server components — add `'use client'` only when browser interactivity requires it
- Keep page components focused on composition; move logic to hooks or server actions
- Route handlers (`app/api/`) are for lightweight proxying — heavy orchestration lives in the backend
- Report pages (`/report/[id]`) fetch data server-side and render static content for shareability

## Express / Backend

- Validate and parse request input with Zod before any logic runs
- Wrap all route handlers in a consistent async error handler
- Return consistent response shapes: `{ data, error, meta }`
- Use request IDs for correlating logs across the request lifecycle
- Rate limiting is applied at the route level, not in business logic

## Styling

- Use CSS custom property tokens defined in `ui-context.md` — no hardcoded hex values
- Follow the border radius scale defined in `ui-context.md`
- Responsive design with mobile-first breakpoints
- Dark mode only — no light mode variants

## shadcn/ui

- Install new components via `npx shadcn@latest add` — never write Radix primitives from scratch
- Components in `components/ui/` are auto-generated; do not edit them manually
- Add `'use client'` only at the shadcn component boundary — keep page logic in server components

## API Routes (Backend)

- `POST /api/test` — validates input, calls AI via adapter, creates DB records, enqueues BullMQ job, returns runId
- `GET /api/report/:runId` — fetches run, cases, results, findings — returns full report model
- `GET /api/test/:runId/status` — returns lightweight run status summary
- All routes reject targets on localhost/private IP ranges before any processing
- Auth headers accepted in request body are used in-memory only — never logged or persisted

## Data and Storage

- Runs, test cases, test results, and findings are separate tables with foreign key relationships
- Use append-only result events for auditability (test_results are never mutated, only inserted)
- Keep nullable columns explicit and documented in the schema
- Large response bodies are stored as excerpts (first N chars), not full content
- Cost per run is tracked in the runs table and exposed in run metadata

## AI Provider Layer

- All AI interactions go through a single adapter interface in `backend/ai/`
- The provider is selected by `AI_PROVIDER` env var — default is Gemini
- AI responses are validated against a Zod schema; on parse failure, retry once with a corrective prompt, then fail gracefully
- Prompts are versioned so regressions are trackable
- Enforce payload uniqueness and required coverage categories in the generation prompt

## File Organization

- `frontend/app/` — Next.js pages and layouts
- `frontend/components/` — Reusable React components
- `frontend/components/ui/` — shadcn/ui primitives (auto-generated, do not edit manually)
- `frontend/lib/` — Frontend utilities, API client, types
- `backend/routes/` — Express route handlers
- `backend/middleware/` — Express middleware (error handler, rate limiter, validator)
- `backend/workers/` — BullMQ worker processes
- `backend/ai/` — AI provider adapter and prompts
- `backend/queue/` — BullMQ queue configuration
- `backend/socket/` — WebSocket server and room management
- `backend/db/` — Database schema, migrations, queries
- `backend/lib/` — Shared utilities, types, constants
- `shared/` — Types and schemas shared between frontend and backend
