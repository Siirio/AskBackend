# Unified Search

ASK search is a synchronous PostgreSQL projection pipeline with Meilisearch as the primary retrieval engine and PostgreSQL lexical search as the fallback.

## Write path

Canonical Item, Service, and moderation transactions create/update/delete the PostgreSQL `SearchDocument` projection synchronously in the same `@Transactional` boundary. A `SearchOutboxEvent` is appended in the same transaction with the matching `projectionVersion`. The aggregate mutation, projection, and outbox event commit atomically — there is no window where an item exists but has no search projection.

A bounded `SKIP LOCKED` worker reads existing SearchDocuments (created synchronously), checks `projectionVersion` for staleness, and delivers to Meilisearch in a separate transaction. The worker never creates SearchDocuments — it only reads and delivers. Retryable failures remain visible with exponential backoff; terminal failures are retained as DEAD events.

### Synchronous projection call sites

| Call site | Trigger | Projection action |
|-----------|---------|-------------------|
| `BusinessProductProcessor.createProduct()` | New item | UPSERT (if active) or skip |
| `BusinessProductProcessor.updateProduct()` | Item edit | UPSERT (if active+approved) or DELETE |
| `BusinessProductProcessor.deleteProduct()` | Item delete | DELETE |
| `BusinessServiceProcessor.createService()` | New service | UPSERT (if active) or DELETE |
| `BusinessServiceProcessor.updateService()` | Service edit | UPSERT (if active) or DELETE |
| `ModerationProcessor.approveProduct()` | Moderation approve | UPSERT (if active) |
| `ModerationProcessor.rejectProduct()` | Moderation reject | DELETE |
| `ModerationProcessor.moderateProduct()` | Moderation hide/show | UPSERT or DELETE |

### Visibility policy

- **Items**: `isActive=true AND moderationStatus=APPROVED` → searchable. All other states → not searchable (DELETE from projection).
- **Services**: `isActive=true` → searchable. Inactive → not searchable.
- Moderation rejection sends DELETE. Moderation approval sends UPSERT.

## Read path

The frontend selects ITEM or SERVICE scope. Meilisearch returns bounded candidate aggregate IDs, PostgreSQL hydrates `SearchDocument` entities, and centralized in-memory ranking produces exact and alternative sections.

A read-your-writes overlay runs after Meilisearch retrieval: dirty projections (`indexedAt IS NULL OR projectionVersion > indexedAt epoch millis`) are merged into results so newly created items appear immediately even before the async worker delivers them to Meilisearch.

If Meilisearch is unavailable, PostgreSQL full-text/trigram retrieval is used and the response records the fallback reason.

## Version and staleness

- `projectionVersion` (Long epoch millis) is set when the SearchDocument is created/updated synchronously. The same value is written to `search_outbox_event.aggregate_version`.
- The outbox worker re-reads the SearchDocument in its own transaction. If `projectionVersion > event.aggregateVersion`, the event is stale (a newer write already happened) and is skipped.
- `indexedAt` is set when Meilisearch delivery completes. Dirty projections have `indexedAt IS NULL OR projectionVersion > indexedAt epoch millis`.
- DELETE events carry the `aggregateId` directly. Meilisearch document ID is the aggregate UUID, so DELETEs work without the SearchDocument.

## Rebuild and repair

Reindex reads canonical `SearchDocument` rows with keyset pagination, writes a replacement Meilisearch index, validates it, and swaps it into service. Reconciliation detects missing, orphaned, and stale-version documents.

DEAD UPSERT events from the pre-fix era (empty-shell constraint violations) require repair: rebuild the SearchDocument from canonical entity data, then requeue the event. See `operations.md` for the repair procedure.

## AI enrichment

AI enrichment runs only for records explicitly selected by the assigned platform member during that business's active seven-day managed-import grant. It validates the provider response and fills only missing descriptions plus additive text-supported tags and attributes on Items, Services, and Unique Offers; ordinary Item, Service, and UniqueOffer creation never requests enrichment, and enrichment never replaces manual data or invents operational facts.
