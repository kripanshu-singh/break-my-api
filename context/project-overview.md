# BreakMyAPI

## Overview

BreakMyAPI is an AI-powered adversarial API tester that finds crashes, logic failures, and semantically wrong responses in minutes. Users paste an endpoint URL or upload a Swagger/OpenAPI spec, and the system generates 20 intelligent adversarial payloads per endpoint, fires them concurrently, evaluates responses semantically, and produces a shareable severity-ordered report. It targets solo backend engineers, startup teams shipping quickly, interns preparing PRs, and engineering managers doing pre-release risk checks.

## Goals

1. Allow a user to test one endpoint in under 2 minutes and receive 20 high-signal adversarial payloads with semantic judgement
2. Differentiate from dumb fuzzers by performing two-call AI analysis — generate targeted payloads with expected behavior, then evaluate actual responses for correctness, suspicious patterns, and exploitability
3. Produce a shareable report card that clearly explains every finding in plain language, ordered by severity, that engineers can screenshot and share

## Core User Flow

1. User lands on the Next.js frontend and chooses input mode (single endpoint or Swagger upload)
2. User provides endpoint URL, HTTP method, sample body, and optional auth headers
3. Spring Boot backend validates input, calls AI via OpenAI SDK to generate 20 adversarial payloads (with expected status codes and behavior descriptions), creates a `run` record in PostgreSQL, and enqueues a RabbitMQ job
4. Spring `@Async` worker picks up the job, fires all payloads concurrently with bounded parallelism, streams incremental results via WebSocket (STOMP) to the live dashboard
5. For suspicious responses (especially 200s), a second AI call judges semantic correctness — flagging status mismatches, null bodies, data leaks, and latency anomalies
6. Aggregated findings are persisted and exposed via a shareable read-only report page

## Features

### Input Modes

- Single endpoint: URL + method + sample body + optional headers
- Swagger/OpenAPI upload: parse spec, extract all endpoints, batch test independently

### AI Engine

- Two-call architecture: first generates payloads with expected behavior, second judges actual responses
- Categories: boundary values, injection attempts, null/undefined, integer overflow, long strings, wrong types, malformed auth
- Strict JSON response schema enforced; retry once on parse failure, then fail gracefully

### Live Streaming

- WebSocket-based incremental result streaming per run
- Run progress events, per-case results, and findings pushed in real time

### Reporting

- Endpoint-level findings list with verdict (correct/suspicious/wrong), severity (low/medium/high/critical), and explanation
- Severity-ordered report card designed for sharing
- Permanent shareable link via GET /api/report/:runId

## Scope

### In Scope

- Single endpoint adversarial testing with AI-generated payloads
- Swagger/OpenAPI upload with endpoint extraction and batch testing
- Semantic response judgement via second AI call (on suspicious subset)
- Live WebSocket dashboard for real-time streaming results
- Shareable read-only report pages
- Rate limiting and abuse protection (5 runs/day/IP)
- SSRF protection — reject localhost/private network targets

### Out of Scope

- Full workflow integration testing across endpoint chains
- Autonomous auth-flow crawling and auto-login (Phase 4+)
- Production traffic replay
- Enterprise SSO and multi-tenancy
- Historical run comparisons (Phase 6+)
- CI integration hooks (Phase 6+)

## Success Criteria

1. A user can submit a single endpoint and receive a complete result set with 20 payloads in under 2 minutes
2. Semantic anomalies are detected and explained — status code mismatches, suspicious 200s with null bodies, data leaks, latency outliers
3. A shareable report URL renders a severity-ordered findings page without exposing user secrets
4. Rate limiter blocks over-quota requests, and localhost/private targets are rejected
5. Cost per run is tracked and visible; monthly spend is hard-capped via provider quotas
