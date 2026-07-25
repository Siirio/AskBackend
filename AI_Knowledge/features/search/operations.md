# Search Operations

## Required configuration

- `MEILISEARCH_HOST_URL`, `MEILISEARCH_API_KEY`, and `MEILISEARCH_INDEX_NAME` configure primary retrieval.
- `ASK_SEARCH_AI_ENRICHMENT_ENABLED` defaults to `true`; enrichment requires `DEEPSEEK_API_KEY` to perform external calls.
- Worker batch sizes, intervals, timeouts, attempts, and backoff are configurable under `ASK_SEARCH_*` environment variables in `application.yml`.

Production and stage use separate pinned Meilisearch services and persistent volumes in `deploy/vps/compose.yml`.

## Reindex

Set `ASK_SEARCH_REINDEX_ON_STARTUP=true` for one controlled instance. The job reads PostgreSQL with keyset pagination, writes a replacement index, validates the replacement, and swaps it into service. Return the flag to `false` after completion. Do not run concurrent startup rebuilds.

Rollback keeps PostgreSQL canonical. Point `MEILISEARCH_INDEX_NAME` at the last validated index or run a fresh rebuild. Search continues through PostgreSQL while Meilisearch is unavailable.

## Reconciliation

Set `ASK_SEARCH_RECONCILIATION_ENABLED=true` to schedule bounded comparison. `ASK_SEARCH_RECONCILIATION_REPAIR=true` enqueues repairs for missing or stale documents and removes confirmed orphans. Review counts and sampled IDs before enabling repair in a new environment.

Moderator-hidden Items are non-live in both projection and reconciliation, so repair cannot reintroduce them.

## Dead outbox events

Dead events are retained in `search_outbox_event`; never delete them as a retry mechanism. Confirm the canonical aggregate version and failure cause, restore the dependency or data invariant, then reset only the selected event to the retryable state with its availability time set to the current time. The worker still applies stale-version protection.

### Pre-fix DEAD events (empty-shell constraint violations)

Before 2026-07-25, `SearchProjectionServiceImpl.apply()` called `findOrCreate()` which created empty `SearchDocument` shells with only `documentType` and `aggregateId`. Six NOT NULL fields (`title`, `normalizedTitle`, `currency`, `verifiedAttributes`, `aiAttributes`, `aliases`) were never populated, causing INSERT constraint violations. After 8 retries, events went DEAD.

**Repair procedure for DEAD UPSERT events:**

1. Identify DEAD UPSERT events: `SELECT * FROM search_outbox_event WHERE status = 'DEAD' AND event_type = 'UPSERT'`
2. For each event, check if the canonical aggregate still exists:
   - `PRODUCT_OFFER` → query `Item` by `aggregate_id`
   - `SERVICE_BRANCH_OFFER` → query `ServiceOffering` by `aggregate_id`
3. If aggregate exists AND no valid SearchDocument exists for it → rebuild SearchDocument via `SearchDocumentService.upsertItemProjection()` or `upsertServiceProjection()` with canonical data
4. If aggregate no longer exists → mark event as COMPLETED (aggregate was deleted)
5. If valid SearchDocument already exists (e.g., from a subsequent edit) → mark event as COMPLETED (superseded)
6. Requeue repaired events via `SearchOutboxService.republish()` with the current `projectionVersion`

This repair is idempotent: running it multiple times is safe because `republish()` uses `on conflict ... do update` and the worker applies stale-version protection.

## Completed outbox retention

`SearchOutboxRetentionScheduler` deletes `COMPLETED` events whose `processed_at` is older than `ASK_SEARCH_OUTBOX_RETENTION` (default `P3D`), running every `ASK_SEARCH_OUTBOX_RETENTION_INTERVAL` (default `PT1H`). Only `COMPLETED` rows are purged — `DEAD` rows stay for diagnosis, and idempotent dedup is unaffected because `completeSuperseded` plus stale-version protection guard against replays, not the presence of old completed rows.

## AI enrichment failures

AI metadata is derived and can be rebuilt. Inspect attempt count, error, worker claim, and dead state on the search document plus evidence rows in `search_ai_metadata`. Missing API keys require no repair. After provider or schema recovery, reset only selected dead enrichment claims; canonical item/service data is unaffected.

## Verification

Run the backend package, frontend build, Flyway V1 baseline on an isolated database, migration hash, `git diff --check`, architecture scans, the evaluation runner, PostgreSQL query plans, and the parameterized k6 profile. An outage drill must prove the fallback response and the visible separator log before release.
