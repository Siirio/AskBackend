# Product and Service Creation-to-Search Pipeline

Status: APPROVED

Date: 2026-07-16

## Scope

This iteration makes the item and service creation-to-search pipeline correct, rebuildable, bounded, resilient, measurable, and relevant. It covers canonical item, service, and offer writes; a transactional outbox; versioned PostgreSQL search projections; reliable Meilisearch indexing; optional AI enrichment; deterministic and AI-assisted query interpretation; bounded retrieval, hydration, and PostgreSQL fallback; reindexing; reconciliation; executable search evaluation; load testing; and the frontend contract changes required to exercise search.

Onboarding, broad frontend redesign, supplier fallback, business broadcasts, automatic business notifications from search, general analytics, and unrelated chat or business-workspace changes are excluded.

V1, V2, and V3 migrations are immutable. V4 may be rewritten because it has not reached production. Later migrations may be added only when repository behavior requires them.

## Authoritative Invariants

1. `Product`, `ServiceOffering`, `ProductOffer`, and service branch offers contain canonical business truth.
2. `SearchDocument` and Meilisearch contain derived, rebuildable projections.
3. Canonical writes and their outbox events commit in one PostgreSQL transaction.
4. Canonical write transactions never call AI or Meilisearch.
5. Lexical projection and indexing do not wait for AI enrichment.
6. AI metadata is stored separately with evidence, confidence, schema version, model version, extraction time, and verification state.
7. AI metadata never overwrites raw business fields.
8. AI-derived facts normally expand retrieval and influence ranking rather than becoming hard filters.
9. Explicit price, location, status, document type, business selection, and unambiguous exclusions may be hard constraints.
10. Production search and fallback never load or score the full catalog in Java memory.
11. Candidate retrieval and hydration are bounded and paginated.
12. Old events cannot overwrite newer projections or resurrect disabled or unpublished listings.
13. Each search request is self-contained and depends only on the complete current query and explicit request fields.
14. Search never creates chats, requests, recipient lists, or business notifications.
15. Explicit constraints are never silently relaxed; exact results and alternatives are separate.
16. Relevance and performance claims require recorded evaluation and load-test results.

## Canonical Write and Projection Flow

```text
create/update/import item or service
-> validate ownership and input
-> save canonical entity and branch offer
-> increment monotonic aggregate version
-> insert versioned outbox event
-> commit PostgreSQL transaction
-> claim bounded outbox jobs with SKIP LOCKED
-> build lexical SearchDocument from canonical PostgreSQL data
-> index the versioned document in Meilisearch
-> schedule optional AI enrichment
-> validate and persist separate AI metadata
-> emit a newer projection event
-> update SearchDocument and Meilisearch when the version is current
```

The outbox state model is `PENDING`, `PROCESSING`, `RETRY`, `COMPLETED`, and `DEAD`. Workers claim jobs in short transactions, release database locks before network calls, and finish or reschedule jobs in separate transactions. Attempts, availability time, claim time, completion time, and the last bounded error are recorded. Duplicate delivery is safe. Processing order is not trusted.

The indexed document carries a monotonic `documentVersion`. Index and delete operations compare the event version with the current canonical/projection version. A delayed event becomes a no-op when a newer version exists. Disable and unpublish events use the same rule.

## Projection Model

`SearchDocument` stores one searchable branch-level item or service result. It includes the canonical target identifiers, business and branch identifiers, document type, publication status, title, normalized title, business and branch names, category path, brand, SKU, raw summary, tokens, verified attributes, AI attributes, aliases, AI search summary, price bounds, currency, location, availability semantics, freshness, and document version.

Canonical fields are copied into the projection; they are not moved out of their owning entities. Projection rows can be deleted and rebuilt from canonical PostgreSQL data in keyset-paginated batches.

AI facts use a separate normalized entity or JSON-backed record owned by the search/enrichment domain. Every fact records its source text or field, evidence, confidence, schema version, extractor/model version, extraction timestamp, and verification state. The initial states are `AI_DERIVED`, `BUSINESS_CONFIRMED`, `BUSINESS_CORRECTED`, and `REJECTED`.

## Query Interpretation

Interpretation starts with bounded deterministic parsing for price and currency expressions, numeric ranges, explicit exclusions, document type, known location input, and request fields. It then optionally calls AI under strict connection and response timeouts. AI failure returns the deterministic plan rather than failing search.

The interpretation cache key includes normalized `rawQuery`, language, explicit filters, overrides, deterministic parser version, prompt version, model version, and attribute schema version. It excludes users, sessions, journeys, and prior queries.

The server validates the interpreted plan against a strict owned schema. Unknown fields, invalid values, oversized output, and unsupported hard constraints are rejected. AI output may add aliases, semantic terms, category suggestions, purposes, and soft attributes. It may not change explicit request fields or restore removed constraints.

## Retrieval, Ranking, and Alternatives

Meilisearch retrieves a bounded candidate window using prioritized searchable attributes: normalized title, title, brand, category path, verified attributes, aliases, AI summary, AI attributes, important excerpts, raw description, and business metadata. Filterable and sortable fields are explicitly allowlisted.

PostgreSQL hydrates only the requested page plus a small configured margin using bounded queries that fetch the result-card graph without N+1 access. Hydration preserves Meilisearch order. Disabled, unpublished, stale, or unauthorized rows are removed; the margin prevents a short page after filtering.

Ranking combines versioned components: intent/category relevance, explicit constraints, verified attribute agreement, lexical/semantic agreement, geographic relevance, price fitness, freshness, and small legitimate tie-breakers. Constants live in typed configuration and are measured by the evaluation runner.

When explicit constraints produce no exact result, the system runs bounded, predefined relaxation passes. It never changes the exact result set. Alternatives identify every relaxed constraint and why each result appears.

## PostgreSQL Fallback

Fallback uses indexed normalized text, PostgreSQL full-text search, and `pg_trgm` where justified by measured plans. It applies publication, type, price, business, and location constraints in SQL, returns bounded IDs, and uses the same hydration and result-shaping path as Meilisearch.

Critical queries receive `EXPLAIN (ANALYZE, BUFFERS)` review against generated local data. Sequential scans are acceptable only when the planner proves they are cheaper for a deliberately small relation; no application-memory full-catalog fallback remains.

## Reindexing and Reconciliation

Administrative operations provide resumable, keyset-paginated projection rebuild, bounded Meilisearch batch indexing, drift comparison, missing/orphan/stale-version reporting, repair, indexing-lag reporting, and dead-event retry. Rebuild does not delete the live index first. A replacement versioned index is populated and validated before the configured index is switched.

## Error Handling and Observability

External failures are classified into retryable and terminal outcomes. Retry uses bounded exponential backoff with jitter. Dead jobs remain visible and retryable by an administrator. Logs include event and aggregate identifiers but exclude sensitive query or business content. Metrics cover queue depth, attempts, dead events, indexing lag, indexing errors/recovery, AI latency/failure/fallback, search latency, fallback use, candidate counts, hydration query counts, and evaluation version.

## Verification

No automated test files or test dependencies are added. Verification uses executable builds, isolated local migration runs, deterministic evaluation commands, bounded reindex/reconciliation dry runs, direct database invariant queries, Meilisearch task/version inspection, `EXPLAIN (ANALYZE, BUFFERS)`, architecture dependency scans, and load tests. Each verification command records its exact output and exit code.

A versioned Russian/Kazakh/mixed-language evaluation dataset and standalone runner record precision@3, recall@10, MRR, zero-result correctness, explicit-constraint violation rate, category mismatch rate, and fallback usage. A reproducible load test records healthy Meilisearch, AI timeout, concurrent indexing, PostgreSQL fallback, reindex, and bulk import behavior.

Frontend changes are restricted to the canonical request and response contract, interpreted constraints, exact-versus-alternative presentation, supported sorting, availability uncertainty, and removal of search-triggered supplier actions.
