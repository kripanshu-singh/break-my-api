# Backlog and Milestones

## Milestone timeline

- M1 (end week 1): single endpoint MVP complete.
- M2 (end week 2): live stream + semantic checks complete.
- M3 (end week 3): Swagger mode + share report complete.
- M4 (end week 4): launch-safe hardening complete.

## Prioritized backlog

### P0 (must-have before launch)

- Endpoint submission UI.
- AI payload generation with strict JSON schema.
- Queue-driven execution worker.
- Result persistence and report endpoint.
- WebSocket live progress updates.
- Rate limiting and spend guardrails.
- SSRF and unsafe target protections.

### P1 (important after launch)

- Swagger upload with endpoint selector.
- Semantic second-pass on suspicious responses.
- Better report visuals and exports.
- Basic auth token helper (manual paste).

### P2 (growth and polish)

- Auto-login token fetch flow.
- Historical run comparisons.
- Team workspaces and saved configs.
- CI integration hooks.

## Weekly execution board template

For each task, maintain:

- Owner: self
- Status: todo|doing|blocked|done
- Effort: S|M|L
- Risk: low|medium|high
- Demo artifact: link to clip/screenshot

## Definition of done template

A task is done only when:

- Functionality works on local and staging.
- Relevant test coverage exists.
- Logs and errors are actionable.
- Docs are updated in this folder.

## Risk register

- Risk: AI cost overrun.
  - Mitigation: daily quotas, selective semantic pass.
- Risk: high false positives.
  - Mitigation: tune prompts, add confidence thresholds.
- Risk: malicious usage.
  - Mitigation: quotas, target validation, abuse monitoring.
- Risk: solo burnout and deadline slip.
  - Mitigation: strict WIP limits and weekly scope cuts.

## Scope cut policy (solo protection)

If behind schedule, cut in this order:

1. Auto-login auth flow.
2. Advanced exports.
3. Historical comparisons.
4. Non-critical UI polish.

Do not cut:

- safety guardrails,
- queue architecture,
- report correctness,
- semantic anomaly explanation.
