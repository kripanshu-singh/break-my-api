# Solo Engineer Best Practices

## 1) Scope discipline

- Build only one differentiator deeply: semantic correctness analysis.
- Delay nice-to-have features until MVP is stable.
- Use explicit non-goals to avoid roadmap drift.

## 2) Architecture discipline

- Keep components boring and replaceable.
- Separate intake API and execution worker.
- Use queues for all heavy test runs.
- Keep AI provider behind one adapter interface.

## 3) Reliability discipline

- Apply per-endpoint timeout defaults.
- Use bounded concurrency in workers.
- Implement retries with backoff only for transient failures.
- Keep idempotency keys for run submission.

## 4) Security discipline

- Never persist user-provided auth headers.
- Store secrets in env vars only.
- Validate all inputs at API edge.
- Block localhost and private network targets to reduce SSRF risk.
- Add explicit warning that users must test owned staging endpoints.

## 5) Cost discipline

- Hard-cap infra spend from day 1.
- Limit free tier usage with quota per IP and per account.
- Batch AI calls and avoid per-test AI evaluations.
- Track cost per run in logs and DB.

## 6) Data model discipline

- Separate run metadata, test cases, and findings.
- Use append-only result events for auditability.
- Keep nullable columns explicit and documented.

## 7) Observability discipline

- Add structured logs with run ID and endpoint.
- Track latency percentiles, timeout count, and 5xx ratio.
- Add basic alerting for worker failure spikes.

## 8) Prompt engineering discipline

- Lock response schema and reject malformed AI output.
- Enforce payload uniqueness and required coverage categories.
- Version prompts so regressions are trackable.

## 9) Delivery discipline

- Ship one vertical slice each week.
- End every week with a demoable artifact.
- Maintain changelog and release notes continuously.

## 10) Quality discipline

- Minimum test baseline:
  - Unit tests for parsers and validators.
  - Integration test for queue + worker + DB write.
  - Smoke test for end-to-end single endpoint run.
- Track escaped defects in a small postmortem log.

## 11) Documentation discipline

- Write docs as code in same repo.
- Update docs in same PR as feature changes.
- Keep runbook current before launch and after incidents.

## 12) Personal sustainability

- Keep one no-meeting deep work block daily.
- Limit work-in-progress to 1 major feature at a time.
- Reserve weekly buffer for bug fixes and refactor.