# Progress Tracker

Update this file after every meaningful implementation change.

## Current Phase

Phase 1 — Entity and Repository Layer (Complete)

## Current Goal

Establish database schema, JPA entities, and repository layer. Then build REST controllers and service layer.

## Completed

- Docker Compose file created (PostgreSQL 16, Redis 7, RabbitMQ 4)
- application.properties configured for Docker services
- Context files updated for Spring Boot architecture
- Skills installed: shadcn/ui, Next.js App Router, TypeScript advanced types, Tailwind
- pom.xml fixed (Spring Boot 3.4.4, Java 21, all starters added)
- Project compiles successfully (`./mvnw compile`)
- Entities created: Project, Endpoint, Baseline, TestSuite, TestRun, TestResult
- Repositories created: ProjectRepository, EndpointRepository, BaselineRepository, TestSuiteRepository, TestRunRepository, TestResultRepository

## In Progress

- None — waiting on next tasks

## Next Up

1. Add health check endpoint (`GET /api/health`)
2. Configure RabbitMQ queue and `@Async` worker skeleton
3. Add Redis rate limiter filter
4. Create REST controller stubs

## Open Questions

- What is the target cloud provider for production? (Neon for Postgres, Railway for Spring Boot backend, Vercel for frontend — confirm)
- Gemini or OpenAI for AI calls? (Using OpenAI SDK with swappable baseURL — confirm default provider)
- Should we add Springdoc/OpenAPI for API documentation?

## Architecture Decisions

- **Spring Boot + Maven over Express**: The backend is now Spring Boot 3.4 with Java 21 + Maven. Better portfolio value, cleaner REST APIs, built-in JPA/WebSocket/AMQP support.
- **RabbitMQ over BullMQ**: Native Spring AMQP support eliminates separate worker process. `@Async` on the consumer handles concurrency. No separate deployable needed.
- **OpenAI SDK over custom adapter**: The OpenAI Java SDK supports custom `baseUrl` — swap between Gemini (free via OpenAI-compatible endpoint), OpenAI, or Claude by changing config. No adapter layer needed.
- **PostgreSQL in Docker for local dev, Neon for prod**: PostgreSQL chosen over SQLite for Spring Boot compatibility (JPA/Hibernate is PostgreSQL-native). Neon free tier for production.
- **No user auth for MVP**: Anonymous usage with IP-based rate limiting reduces friction and accelerates time to first run.
- **Vercel for frontend, Railway for backend**: Frontend on Vercel Free (Next.js), backend on Railway free tier (Java/Spring Boot container).

## Session Notes

- Context files generated on 2026-07-25 from project documentation in Doc/ folder
- Switched from Express/Node to Spring Boot/Java on user request for resume value
- Docker Compose chosen over native installs for Postgres/Redis/RabbitMQ — zero machine pollution
- OpenAI SDK chosen over custom provider adapter — model-swappable via baseURL out of the box

## 2026-07-26 — DTO layer created

- Created `ApiResponse<T>` generic response wrapper record (data, error, meta) with static `success()` and `error()` factory methods
- Created `CreateProjectRequest` record with Jakarta `@NotBlank` validation on name, description, baseUrl; optional version
- Created `UpdateProjectRequest` record with all optional fields for partial updates
- Created `ProjectResponse` record exposing id, name, description, baseUrl, version, createdAt, updatedAt
- Project compiles successfully
