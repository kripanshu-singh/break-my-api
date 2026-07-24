# Operations Runbook

## Environments

- Local: Docker Compose + .env.local.
- Staging: mirrors production with lower limits.
- Production: public access with strict quotas.

## Pre-deploy checklist

- Migrations reviewed and applied in staging.
- Worker and API version compatibility checked.
- Cost limits confirmed in provider dashboards.
- Rate limits and abuse protections enabled.
- Smoke test run completed.

## Deploy procedure

1. Deploy API service.
2. Deploy worker service.
3. Verify Redis and DB connectivity.
4. Run smoke test (`single endpoint` mode).
5. Validate WebSocket stream and report generation.

## Rollback procedure

1. Stop new run intake via feature flag.
2. Roll back API image.
3. Roll back worker image.
4. Resume intake after smoke pass.

## Incident severities

- P0: security breach, secret leak, abusive misuse.
- P1: major run failures, queue stuck, report loss.
- P2: partial degradation, slow processing.
- P3: cosmetic issues, low-impact bugs.

## Incident response template

- Detection time:
- Scope affected:
- Suspected root cause:
- Mitigation applied:
- Permanent fix owner:
- Follow-up due date:

## Common incidents and actions

### Queue backlog surge

- Check worker health and concurrency settings.
- Temporarily lower intake rate.
- Drain long-running stuck jobs.

### AI provider errors

- Switch provider fallback if configured.
- Return graceful message and retry option.
- Capture response schema failures for tuning.

### Cost spike

- Reduce daily run quota immediately.
- Disable semantic second-pass temporarily.
- Investigate abusive IP patterns and block.

### Report generation failure

- Regenerate from persisted run events.
- If missing data, mark run partial and notify user.

## Monitoring metrics

- Runs per hour.
- Worker success rate.
- Median and p95 run duration.
- Queue depth.
- AI call failures.
- Cost per run.

## Weekly ops routine

- Review error budget and top failures.
- Review cost trend and quotas.
- Tune prompts based on false positives/negatives.
- Update runbook from incident learnings.
