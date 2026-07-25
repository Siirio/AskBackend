# Search REST API Contract

## Public search

`POST /api/v1/search` is anonymous and does not create requests, chats, recipients, notifications, or supplier outreach.

The JSON contract uses snake_case. `raw_query` is required and is returned unchanged.

### Request

- `raw_query`: complete visible customer query.
- `scope`: `item` or `service`, selected by the frontend and never changed by AI.
- `selected_category`, `city`, `sort`, `language`, and `user_location`: optional search inputs.
- `sort`: `intent_match`, `distance`, or `price_asc`.
- `page`: zero-based page, from 0 through 20.
- `page_size`: from 1 through 50.
- `filters`: optional `scope`, `category`, `city`, `min_price`, and `max_price` constraints.
- `overrides`: optional values for the same constraint keys. Explicit overrides take precedence over interpreted values.

### Response

- `raw_query`, `scope`, and `understood_query` preserve the request context.
- `interpreted_constraints` reports each effective constraint with its source.
- `sections` keeps `EXACT` matches separate from `ALTERNATIVE` results.
- Alternative sections include `relaxed_constraints` and a human-readable `reason`.
- `page`, `page_size`, `total`, and `has_next` describe bounded pagination.
- `diagnostics` reports engine, fallback reason, candidate count, and server latency for operations; clients must not render diagnostics.

Cards include brand presentation, price when known, availability state, an honest `availability_warning`, human-readable `match_reasons`, branch/distance context, badges, and opaque contact actions. Availability is never invented.

## Write path (synchronous projection)

SearchDocument is created/updated/deleted synchronously in the same `@Transactional` as the canonical Item/Service/moderate mutation. A `SearchOutboxEvent` with matching `projectionVersion` is appended in the same transaction. The aggregate, projection, and outbox event commit atomically.

An async `SKIP LOCKED` worker reads existing SearchDocuments, checks `projectionVersion` for staleness, and delivers to Meilisearch. The worker never creates SearchDocuments — it only reads and delivers.

## Retrieval behavior

Meilisearch is the primary bounded candidate engine. PostgreSQL hydrates canonical data via `SearchDocument` and is the indexed fallback through full-text and trigram candidate SQL. A Meilisearch failure is visible in diagnostics and logs but does not fail search when PostgreSQL is available.

A read-your-writes overlay merges dirty projections (`indexedAt IS NULL OR projectionVersion > indexedAt`) into results so newly created items appear immediately even before Meilisearch delivery.

DeepSeek interpretation is optional. Deterministic interpretation always runs inside the frontend-selected scope, explicit request values win, and a missing key, timeout, malformed response, or provider error falls back to deterministic interpretation.

AI enrichment runs only after `POST /api/v1/platform/ai-enrichment` with `targetType` (`PRODUCT`, `SERVICE`, or `UNIQUE_OFFER`) and `aggregateIds`. It requires `USE_AI_ITEMS_SERVICES_TOOLS` and an unexpired managed-import grant assigned to that platform user for the target business and scope. Records are never enriched automatically on creation.
