# Search Operations

## Configuration

`MEILISEARCH_HOST_URL`, `MEILISEARCH_API_KEY`, and `MEILISEARCH_INDEX_NAME` configure primary retrieval. Worker batch sizes, leases, attempts, and backoff remain configurable under `ASK_SEARCH_*`.

## Version migration

V4 introduced `search_projection_version_seq`. Because the applied Flyway history of shared dev/stage databases is not confirmed, V4 is preserved. Forward migration V5 adds durable `projection_action`, retires unsupported Business/UniqueOffer search events while preserving their outbox history, removes their obsolete PostgreSQL projections, makes tombstone-only fields nullable, and advances the sequence above the maximum existing `search_document.projection_version` and `search_outbox_event.aggregate_version`. Run a full Meilisearch rebuild after the upgrade so any legacy external-only documents are removed.

Before deployment, inspect `flyway_schema_history` on each environment. Never edit or remove an already applied migration. Validate both a clean migration and an upgrade from the actual environment history.

## Delivery recovery

Each SearchDocument is the durable desired state:

- `INDEX`: Meilisearch should contain this exact `projectionVersion`.
- `DELETE`: Meilisearch should not contain the aggregate ID.

If state changes during a network call, confirmation requeues the current desired action/version. DEAD events remain auditable. Repair requeues the current durable desired state; absence of a SearchDocument is an invariant failure and is never silently marked complete.

## Rebuild and reconciliation

Rebuild indexes only `INDEX` projection rows with keyset pagination, validates the replacement index, and swaps it. Reconciliation scans both `INDEX` and `DELETE` desired states, compares `projectionVersion` with `indexedVersion`, and requeues dirty desired state.

Current reconciliation does not yet prove canonical Item/Service field equality or inspect Meilisearch documents directly. Do not claim missing-canonical/orphan-index detection until those bounded comparisons are implemented and verified.

## Required release verification

Requires separate authorization:

- Maven clean verification;
- clean PostgreSQL startup and Flyway migration;
- upgrade from actual dev/stage Flyway history;
- Item/Service create, update, deactivate, delete, import, moderation, and enrichment scenarios;
- Meilisearch outage/recovery;
- concurrent stale UPSERT/DELETE scenarios;
- reconciliation repair;
- relevance, latency, and load evaluation.
