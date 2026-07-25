# AI Workflow Rules

## Approach

Build this project incrementally using a spec-driven workflow. The six context files define what to build, how to build it, and the current state of progress. Always implement against these specs — do not infer or invent behavior from scratch. If a requirement is missing from the context files, treat it as an open question to resolve before proceeding. Ship one vertical slice each week, and end every week with a demoable artifact.

## Scoping Rules

- Work on one feature unit at a time — do not parallelize across system boundaries
- Prefer small, verifiable increments over large speculative changes
- Do not combine unrelated system boundaries (e.g. UI changes + worker changes) in a single implementation step
- If a change cannot be verified end to end quickly, the scope is too broad — split it

## When to Split Work

Split an implementation step if it combines:

- Frontend UI changes and backend API/worker changes
- Multiple unrelated API routes (e.g. test submission and report retrieval)
- Behavior not clearly defined in the context files
- AI provider integration and database schema changes

## Handling Missing Requirements

- Do not invent product behavior not defined in the context files
- If a requirement is ambiguous, resolve it in the relevant context file before implementing
- If a requirement is missing entirely, add it as an open question in `progress-tracker.md` before continuing
- When you disagree with a spec decision, raise it as a question — do not silently deviate

## Protected Files

Do not modify the following unless explicitly instructed:

- `context/templates/*` — template source files for context file generation
- Third-party library internals (node_modules, vendored code)
- CI/CD configuration files that are not part of the feature scope
- `.gitignore` — version control ignore rules

## Keeping Docs in Sync

Update the relevant context file whenever implementation changes:

- System architecture or boundaries → `architecture.md`
- Storage model decisions → `architecture.md`
- UI tokens, component conventions, or layout patterns → `ui-context.md`
- Code conventions or standards → `code-standards.md`
- Feature scope or success criteria → `project-overview.md`
- Current phase, completed work, open questions → `progress-tracker.md`

If the change contradicts something in any context file, update the file to reflect the new reality before continuing.

## Before Moving to the Next Unit

1. The current unit works end to end within its defined scope
2. No invariant defined in `architecture.md` was violated
3. `progress-tracker.md` reflects the completed work
4. Relevant tests pass (lint, typecheck, unit, integration)
5. No secrets, debug logs, or hardcoded credentials are committed

## Phase Gate Checklist

Before declaring a phase complete:

- Feature complete for phase scope
- Smoke tests passing (single endpoint run completes cleanly)
- Cost impact reviewed and quotas confirmed
- Security review completed (no secret persistence, SSRF protection active)
- Documentation updated in context files
