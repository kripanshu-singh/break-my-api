# Phase-Wise Execution Plan (Solo Engineer)

## Planning assumptions

- Developer: 1 engineer.
- Duration: 8 weeks total.
- MVP launch target: end of week 4.
- Hardening and growth: weeks 5 to 8.

## Phase 0: Foundation and guardrails (2-3 days)

### Goal

Establish working skeleton and hard safety limits before feature work.

### Deliverables

- Monorepo structure (`frontend/`, `backend/`, `worker/`).
- Local infra via Docker Compose (PostgreSQL + Redis).
- Environment management and secrets template.
- Cost and abuse guardrails baseline.

### Tasks

- Create project skeleton and shared types package.
- Add health check endpoints.
- Configure logger and request IDs.
- Add Redis rate limiter by IP (for example 5 runs/day/IP).
- Add monthly and daily spend limits in hosting and AI provider.

### Definition of Done

- `docker compose up` brings required services up cleanly.
- Backend starts and serves `/health`.
- Rate limiter blocks over-quota requests.
- No secrets are committed.

### Risks

- Infrastructure drift across local and cloud.
- Missing cost controls early.

---

## Phase 1: Single endpoint testing MVP (week 1)

### Goal

Allow user to run adversarial tests against one endpoint and see a basic report.

### Deliverables

- UI form for endpoint, method, sample payload, optional headers.
- AI payload generation call (20 payloads with expected statuses).
- Job queue submission and worker execution.
- Basic results page with status and latency.

### Tasks

- Build `POST /api/test` request intake.
- Build AI provider adapter (`generatePayloads`).
- Store run, test cases, and results in PostgreSQL.
- Fire payloads concurrently with controlled parallelism.
- Return final report data via `GET /api/report/:id`.

### Definition of Done

- One user can trigger a test run and receive a complete result set.
- At least 20 payloads generated and executed.
- Failures and timeouts clearly visible.

### Risks

- Unbounded concurrency causing false failures.
- Poor payload quality due to weak prompt design.

---

## Phase 2: Live dashboard and semantic checks (week 2)

### Goal

Differentiate product with real-time UX and semantic correctness detection.

### Deliverables

- WebSocket-based live result streaming.
- Semantic judge pass for suspicious responses.
- Severity tagging (`low`, `medium`, `high`, `critical`).

### Tasks

- Add socket room per run ID.
- Emit incremental worker results.
- Batch suspicious responses for second AI call.
- Implement verdict schema:
  - `verdict`: `correct|suspicious|wrong`
  - `severity`
  - `explanation`

### Definition of Done

- User sees progressive run updates without refresh.
- Semantic anomalies are explained in plain language.
- Mismatch-only signal can be filtered.

### Risks

- AI cost growth due to per-case judgment.
- Noisy verdicts if prompt constraints are weak.

---

## Phase 3: Swagger mode and report sharing (week 3)

### Goal

Expand utility from single endpoint to API-level scanning and sharing.

### Deliverables

- Swagger/OpenAPI upload and parser.
- Endpoint extraction and batched queue dispatch.
- Shareable read-only report page.

### Tasks

- Parse spec and normalize operations.
- Allow endpoint include/exclude selection.
- Queue one job per endpoint with a parent run ID.
- Aggregate endpoint findings into one report.

### Definition of Done

- User uploads valid spec and receives multi-endpoint report.
- Shared URL works without exposing secrets.
- Report load time remains acceptable.

### Risks

- Bad specs causing parser failures.
- Large specs generating long runs and poor UX.

---

## Phase 4: Launch hardening and reliability (week 4)

### Goal

Make MVP launch-safe for public traffic.

### Deliverables

- Authentication header handling policy.
- Input validation and safe defaults.
- Retry policies and failure isolation.
- Release checklist and runbook.

### Tasks

- Ensure headers are in-memory only.
- Add warning banners (staging only, no production targets).
- Add worker retry with max attempts and dead-letter path.
- Add structured error codes for user-facing failures.

### Definition of Done

- No P0 security gaps in checklist.
- Controlled degradation under partial outages.
- Reproducible deploy and rollback process.

### Risks

- Abuse of tool against non-owned endpoints.
- Broken user trust due to unclear failures.

---

## Phase 5: Optimization and quality (weeks 5-6)

### Goal

Reduce cost, improve signal quality, and improve maintainability.

### Deliverables

- Cost-aware AI invocation strategy.
- Prompt and payload quality tuning.
- Baseline automated tests.

### Tasks

- Trigger semantic pass only on suspicious subsets.
- Add deduplication for generated payloads.
- Add unit tests for parser and validators.
- Add integration tests for queue-worker flow.

### Definition of Done

- Cost per run is measured and visible.
- Test suite covers critical paths.
- Reduced false-positive finding rate.

---

## Phase 6: Growth features (weeks 7-8)

### Goal

Improve retention and virality.

### Deliverables

- Better report cards and export.
- Comparison runs (before/after fix).
- Basic usage analytics.

### Tasks

- Add report summary card designed for sharing.
- Add run diff view by endpoint.
- Add anonymized metrics dashboard.

### Definition of Done

- Users can share polished findings externally.
- Repeat users can verify fix impact quickly.

## Weekly cadence for solo execution

- Monday: plan and issue slicing.
- Tuesday to Thursday: implementation.
- Friday: stabilization, docs, demo video, and post.
- Saturday: tech debt burn-down.
- Sunday: rest or optional backlog grooming.

## Phase gate checklist

Each phase must pass before moving forward:

- Feature complete for phase scope.
- Smoke tests passing.
- Cost impact reviewed.
- Security review completed.
- Documentation updated.
