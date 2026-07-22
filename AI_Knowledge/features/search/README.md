# Unified Search

Ask search is a versioned item/service projection pipeline with PostgreSQL as canonical storage, Meilisearch as the primary candidate engine, and PostgreSQL full-text/trigram retrieval as the fallback.

## Write path

Canonical item, service, import, and autodump transactions increment the aggregate search version and append a `search_outbox_event` in the same transaction. They never call Meilisearch or AI.

A bounded `SKIP LOCKED` worker builds the PostgreSQL lexical projection first, rejects stale versions, then updates Meilisearch and waits for task completion. Retryable failures remain visible with backoff; terminal failures are retained as dead events.

## Read path

The frontend selects PRODUCT or SERVICE and sends the complete raw query unchanged. DeepSeek can add validated interpretation inside that selected scope when configured, but it is never required. Meilisearch returns bounded candidate IDs, PostgreSQL performs bounded hydration and version revalidation, and centralized ranking produces exact and explicitly relaxed alternative sections.

If Meilisearch is unavailable, indexed PostgreSQL retrieval is used and the response records the fallback reason. Search has no dependency on request, chat, broadcast, recipient, or notification services and creates none of them.

## Rebuild and repair

Reindex uses bounded keyset batches, writes a replacement versioned index, validates it, and swaps it into service. Reconciliation detects missing, orphaned, and stale-version documents and can enqueue repairs. See `operations.md` for commands and rollback guidance.

## AI enrichment

AI enrichment workers run only for documents explicitly requested by a platform user with `USE_AI_CATALOG_TOOLS`. They claim bounded batches, validate a strict schema, store evidence-bearing metadata, and never change canonical products or services.
