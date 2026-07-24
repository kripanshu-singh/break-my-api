# Product Blueprint

## Product name

BreakMyAPI

## One-line positioning

AI-powered adversarial API tester that finds crashes, logic failures, and semantically wrong responses in minutes.

## Problem statement

Most API testing tools either:

- validate schemas and happy paths, or
- fuzz randomly and only detect hard failures.

They often miss semantic failures such as:

- incorrect status codes (for example, `200` instead of `404`),
- success responses with null/invalid business state,
- weak validation accepted as success,
- sensitive data exposure in non-error responses.

## Core differentiator

BreakMyAPI is not just payload generation. It performs semantic judgement on real responses.

Two-call AI model:

1. Generate targeted adversarial payloads with expected behavior.
2. Evaluate actual responses and classify correctness, suspicious behavior, and exploitability.

## Primary users

- Solo backend engineers.
- Startup teams shipping quickly.
- Interns and early-career developers preparing PRs.
- Engineering managers doing pre-release risk checks.

## MVP success criteria

- User can test one endpoint in less than 2 minutes.
- System generates 20 high-signal payloads per endpoint.
- System reports:
  - expected vs actual status mismatch,
  - suspicious 200 responses,
  - timeout and latency anomalies,
  - potential data leaks.
- User can share a report via URL.

## Non-goals for MVP

- Full workflow integration testing across endpoint chains.
- Autonomous auth-flow crawling.
- Production traffic replay.
- Enterprise SSO and tenancy.

## Input modes

1. Single endpoint mode.

- URL, method, sample body, optional headers.

2. Swagger/OpenAPI mode.

- Parse endpoints and schemas from uploaded spec.
- Run endpoint-level adversarial tests independently.

## Output artifacts

- Live test stream dashboard.
- Endpoint-level findings list.
- Severity-ordered report card.
- Shareable link to full run.

## Quality principles

- Useful over perfect.
- Explain every finding in plain language.
- Default-safe behavior (no secret retention).
- Cost visibility and hard limits by design.
