# Current System Audit

Updated: 2026-07-16

## Search pipeline

Product offers and service branch offers are canonical, versioned sources. Every supported create, update, disable, import, and autodump publication writes a monotonic search version and a transactional outbox event. Canonical transactions do not call Meilisearch or AI.

The outbox worker uses bounded `SKIP LOCKED` claims, retained retry/dead state, exponential backoff, stale-event supersession, and per-aggregate delivery ordering. PostgreSQL projection upserts are compare-and-set by aggregate version. Meilisearch operations wait for task completion and propagate failures.

`search_document` contains the lexical projection, verified business attributes, separate AI attributes, availability provenance, search vector, trigram title, projection version, and enrichment claim state. `search_ai_metadata` stores model/schema versions, confidence, evidence, source, verification state, and extraction time without mutating canonical business truth.

Meilisearch v1.10.3 is the primary bounded candidate engine. PostgreSQL full-text/trigram SQL is the bounded fallback. Hydration is batched, preserves engine order, and revalidates active/version state. No full-catalog `findAll`, `findActiveCandidates`, or outreach dependency remains in production search.

Query interpretation is deterministic and self-contained. Optional DeepSeek interpretation has strict timeouts and validation; explicit filters and overrides take precedence. AI enrichment runs only for documents explicitly queued by an authorized platform user and safely no-ops without a key.

The public response preserves the raw query, reports interpreted constraints, separates exact and relaxed alternatives, paginates, provides honest availability warnings and match reasons, and includes nonvisual operational diagnostics. Search never creates requests, chats, recipients, broadcasts, or notifications.

## Frontend

The frontend uses the current filters/overrides/pagination DTO, renders exact and alternative sections separately, shows match reasons and availability warnings, and offers only relevance, distance, and lowest-price sorting. Internal diagnostics and supplier-check/broadcast actions are not rendered.

## Operations and evidence

Local and VPS compose files pin Meilisearch v1.10.3. Reindex uses a replacement index and bounded keyset batches; reconciliation detects and optionally repairs missing, orphaned, and stale projections. Operational recovery is documented in `AI_Knowledge/features/search/operations.md`.

Build, migration, healthy-engine, outage-fallback, stale-delivery, reindex, and reconciliation drills have been run against an isolated PostgreSQL database and local Meilisearch. Evaluation and load tooling lives under `tools`; reports are release evidence, not automated test coverage.

## Remaining operational risks

- Production relevance quality depends on representative multilingual evaluation data and curated aliases.
- k6 percentiles must be recorded on production-like infrastructure before setting an external SLO.
- AI enrichment quality requires a configured provider key and reviewed evidence samples.
- Meilisearch and PostgreSQL capacity, backups, alerts, and retention must be sized for the deployment volume.
