# Progress Tracker

Update this file after every meaningful implementation change.

## Current Phase

Phase 0 — Foundation and Guardrails (Not started)

## Current Goal

Set up monorepo skeleton (frontend/, backend/, worker/), Docker Compose infra (PostgreSQL + Redis), health endpoints, environment management, and cost/abuse guardrails.

## Completed

- None yet.

## In Progress

- None yet.

## Next Up

1. Create project skeleton with frontend/, backend/, shared/ directories
2. Set up Docker Compose with PostgreSQL and Redis
3. Add health check endpoints to backend
4. Configure logger and request ID middleware
5. Add Redis rate limiter (5 runs/day/IP)
6. Set up environment variable template and validation

## Open Questions

- Should the worker be a separate service or a process spawned by the backend? (Doc suggests isolated process — confirm deployment model)
- What is the target cloud provider for MVP deployment? Railway + Supabase + Vercel is recommended in docs
- Should we use Prisma or raw SQL for database access? (Recommendation: Prisma for speed, raw SQL for complex queries)
- What is the exact Gemini API model name and free tier limits to configure?

## Architecture Decisions

- **Monorepo with three packages**: frontend (Next.js), backend (Express), shared (types/schemas). Separating concerns cleanly while sharing type definitions.
- **BullMQ over in-process execution**: Ensures backend stays responsive under load; job queue provides retry, dead-letter, and observability out of the box.
- **Two-call AI architecture**: First call generates adversarial payloads with expected behavior; second call (on suspicious subset only) provides semantic judgement. This limits AI cost while delivering the core differentiator.
- **Gemini Flash as default AI provider**: Free tier sufficient for MVP; provider abstraction allows swapping to Claude later without code changes.
- **No user auth for MVP**: Anonymous usage with IP-based rate limiting reduces friction and accelerates time to first run.

## Session Notes

- Context files generated on 2026-07-25 from project documentation in Doc/ folder
- Project README references a 4-week MVP build with phased execution plan
- Solo engineer model — scope discipline and weekly ship cadence are critical
