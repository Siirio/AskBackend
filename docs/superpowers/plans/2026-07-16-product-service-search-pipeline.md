# Product and Service Search Pipeline Implementation Plan

**Goal:** Build a fast, relevant, versioned, rebuildable product/service creation-to-search pipeline without adding automated tests.

**Verification policy:** Do not create test files or add test dependencies. Verify with compilation/builds, immutable migration hashes, isolated local PostgreSQL and Meilisearch runtime checks, direct invariant queries, dry runs, evaluation runners, query plans, dependency scans, and load tests. Do not commit or push.

## Global Constraints

- V1, V2, and V3 remain byte-for-byte unchanged; verify their recorded SHA-256 hashes after every migration edit.
- V4 may be rewritten because it is not deployed.
- Canonical write transactions never call AI or Meilisearch.
- AI metadata is separate from business truth and soft by default.
- Every retrieval, hydration, worker, rebuild, and reconciliation operation is bounded.
- Search never creates chats, requests, recipients, broadcasts, or notifications.
- Onboarding, broad frontend redesign, and unrelated workspace work are excluded.
- No automated tests, test source trees, test dependencies, Vitest, JUnit, Testcontainers, or Playwright suites are added.

## Phase 1: Baseline and V4 Schema

- [ ] Record backend/frontend build results and V1–V3 hashes.
- [ ] Rewrite V4 search schema with canonical search versions, `search_outbox_event`, separate `search_ai_metadata`, versioned `search_document` fields, `pg_trgm`, full-text vector, and bounded claim/filter indexes.
- [ ] Map the V4 fields to focused entities and enums.
- [ ] Create an isolated local database, run Flyway through V4, inspect columns/indexes through `information_schema` and `pg_indexes`, and rerun V1–V3 hashes.
- [ ] Run `mvn -DskipTests package` and record the exact result.

## Phase 2: Transactional Outbox and Canonical Writes

- [ ] Implement idempotent enqueue, `SKIP LOCKED` bounded claim, completion, retry with bounded exponential backoff/jitter, and retained dead events.
- [ ] Add monotonic `searchVersion` propagation to product offers and service branch offers.
- [ ] Replace synchronous `SearchDocumentService` calls in product, service, catalog import, and autodump publication with outbox insertion in the canonical transaction.
- [ ] Remove the obsolete `search_index_queue` implementation.
- [ ] Verify with isolated local create/update/disable/import transactions and direct SQL showing canonical row/outbox atomicity.
- [ ] Scan catalog/service/import packages to prove no canonical path references Meilisearch or DeepSeek.

## Phase 3: Versioned Lexical Projection Worker

- [ ] Add bounded canonical source readers for one product offer or service branch offer.
- [ ] Implement compare-and-set projection upsert/archive using canonical current version and publication state.
- [ ] Implement short outbox claim transactions and external indexing outside database locks.
- [ ] Verify duplicate delivery, delayed stale events, disable/unpublish, and lexical-before-AI behavior with controlled local event rows and direct SQL/Meilisearch inspection.
- [ ] Run the backend build and dependency scans.

## Phase 4: Reliable Meilisearch, Reindex, and Reconciliation

- [ ] Pin a Meilisearch version in local/deployment configuration and record the observed server version.
- [ ] Replace catch-and-log writes with typed results, awaited task completion, timeouts, and retryable failure propagation.
- [ ] Configure prioritized searchable attributes and allowlisted filters/sorts without claiming unmeasured morphology.
- [ ] Implement keyset-paginated rebuild into a replacement versioned index and validated switch.
- [ ] Implement bounded missing/orphan/stale-version reconciliation and optional repair enqueue.
- [ ] Verify with local index outage/recovery, sampled versions, counts, resume cursors, and reconciliation reports.

## Phase 5: Separate Optional AI Enrichment

- [ ] Replace arbitrary attribute maps with a strict extraction DTO and server-owned schema validation.
- [ ] Configure connection/response timeouts, bounded batches/concurrency, retry/dead visibility, and prompt-injection-resistant input framing.
- [ ] Store confidence, evidence, source, model/schema versions, extraction time, and verification state only in `search_ai_metadata`.
- [ ] Emit a new projection event after valid metadata commits; never modify raw product/service fields.
- [ ] Verify valid, malformed, empty, timeout, and missing-key responses through a deterministic local stub server and direct database inspection.

## Phase 6: Self-Contained Query Interpretation

- [ ] Add deterministic price/currency/range/exclusion/location/document-type parsing.
- [ ] Add strict optional AI plan validation and deterministic precedence: removal/override, explicit filter, deterministic query fact, AI signal, absent.
- [ ] Build a bounded versioned cache key containing all semantic inputs and no journey/history data.
- [ ] Validate query length, pagination, page size, radius, bounds, IDs, numeric ranges, override keys, and sort enum.
- [ ] Verify a command-line fixture matrix covering complete-query independence, removals, overrides, AI timeout/malformed output, and cache separation.

## Phase 7: Bounded Retrieval, Hydration, Ranking, and Alternatives

- [ ] Replace full-catalog Java fallback with indexed PostgreSQL full-text/trigram candidate SQL and strict limits.
- [ ] Use one bounded hydration path with fixed query count, rank-order preservation, active/version revalidation, and candidate margin.
- [ ] Centralize versioned ranking and human-readable match reasons.
- [ ] Keep exact results separate from predefined, explicitly reported relaxation passes.
- [ ] Run `EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON)` on generated 10,000-row and locally feasible larger datasets.
- [ ] Scan production search code to prove no `findAll`, `findActiveCandidates`, full-catalog scoring, or outreach dependency remains.

## Phase 8: Required Frontend Contract

- [ ] Align request/response DTOs with explicit filters, overrides, pagination, interpreted constraints, and exact/alternative sections.
- [ ] Restrict search sorts to relevance, distance, and lowest price.
- [ ] Remove supplier-check, active-event, and broadcast actions from the search path.
- [ ] Preserve the complete visible query on every request and render honest availability warnings.
- [ ] Run TypeScript/Vite build and capture mobile screenshots around the search/results flow.

## Phase 9: Evaluation and Performance Evidence

- [ ] Create a standalone versioned evaluation dataset and CLI runner outside test source trees.
- [ ] Measure precision@3, recall@10, MRR, zero-result correctness, explicit-constraint violations, category mismatch, fallback usage, and language cohorts.
- [ ] Add a parameterized k6 script for healthy search, AI timeout, concurrent indexing, Meilisearch outage/fallback, rebuild/reconciliation, and import publication.
- [ ] Record exact metrics and latency percentiles; do not claim improvement without them.

## Phase 10: Operations and Final Verification

- [ ] Update OpenAPI, current-system audit, search/catalog/service/import contracts, architecture, and operations runbook.
- [ ] Document reindex, reconciliation, dead-event retry, rollback, and V4 migration assumptions.
- [ ] Run backend package, frontend build, migration hashes, `git diff --check`, architecture scans, evaluation runner, query plans, and k6 fresh.
- [ ] Report exact outputs, performance/relevance metrics, unresolved risks, deferred work, and rollback considerations without claiming automated test coverage.
