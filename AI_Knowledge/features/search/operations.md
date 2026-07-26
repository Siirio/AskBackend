# Search Operations

## Configuration

`MEILISEARCH_HOST_URL`, `MEILISEARCH_API_KEY`, and `MEILISEARCH_INDEX_NAME` configure primary retrieval. Worker batch sizes, leases, attempts, and backoff remain configurable under `ASK_SEARCH_*`.

## Migration baseline

The project intentionally has exactly two fresh-deploy migrations:

- `V1__init.sql` contains the complete final DDL: extensions, sequence, tables, indexes, and constraints.
- `V2__reference_data.sql` contains reference-data inserts for cities and typed categories.

There is no supported in-place upgrade from a database whose Flyway history contains the former `V4` or `V5`. Recreate those databases before deploying this baseline, then run a full Meilisearch rebuild.

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
