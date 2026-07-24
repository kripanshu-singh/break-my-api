# Architecture and Data Contracts

## High-level architecture

- Frontend: Next.js dashboard and report pages.
- Backend API: Express/Nest-compatible service for intake and retrieval.
- Queue: BullMQ with Redis.
- Worker: isolated process executing adversarial requests.
- Database: PostgreSQL for runs, cases, results, findings.
- AI layer: provider adapter (Gemini/Claude selectable).

## Execution flow

1. User submits endpoint/spec + optional headers.
2. Backend validates payload and creates `run` record.
3. Backend asks AI for adversarial cases.
4. Backend enqueues execution jobs.
5. Worker executes cases with bounded concurrency.
6. Worker writes result events and emits WebSocket updates.
7. Semantic judge runs on suspicious subset.
8. Aggregated report is persisted and exposed via report API.

## Core APIs (suggested)

### `POST /api/test`

Create a test run.

Request body:

- `mode`: `single|swagger`
- `endpoint` (for single)
- `method`
- `sampleBody`
- `headers` (optional, in-memory only)
- `swagger` (for upload mode)

Response:

- `runId`
- `status`

### `GET /api/test/:runId/status`

Return run status summary.

### `GET /api/report/:runId`

Return full report model suitable for share page.

### `WS /runs/:runId`

Push incremental result and progress events.

## Suggested database tables

### `runs`

- `id` (uuid, pk)
- `mode`
- `created_at`
- `completed_at`
- `status` (`queued|running|completed|failed`)
- `input_hash`
- `target_base_url`

### `test_cases`

- `id` (uuid, pk)
- `run_id` (fk)
- `endpoint`
- `method`
- `payload_json`
- `case_type`
- `expected_status`
- `expected_behavior`

### `test_results`

- `id` (uuid, pk)
- `run_id` (fk)
- `test_case_id` (fk)
- `actual_status`
- `response_time_ms`
- `response_body_excerpt`
- `network_error`
- `created_at`

### `findings`

- `id` (uuid, pk)
- `run_id` (fk)
- `test_case_id` (fk)
- `verdict` (`correct|suspicious|wrong`)
- `severity`
- `explanation`
- `category`

## Event contracts

### `run.progress`

- `runId`
- `completedCases`
- `totalCases`
- `percent`

### `run.case_result`

- `runId`
- `testCaseId`
- `actualStatus`
- `responseTimeMs`
- `error`

### `run.finding`

- `runId`
- `findingId`
- `severity`
- `summary`

## Safety constraints

- Reject targets pointing to localhost/private IP ranges.
- Enforce max payload and max response size.
- Enforce max run duration.
- Enforce strict header allowlist and redact logs.

## AI response schema (strict)

```json
{
  "cases": [
    {
      "payload": {},
      "type": "boundary",
      "expectedStatus": 400,
      "expectedBehavior": "Validation error",
      "severity": "medium"
    }
  ]
}
```

If schema parse fails:

- retry once with corrective prompt,
- then fail gracefully with user-facing guidance.
