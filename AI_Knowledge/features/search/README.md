# Unified Search

ASK search returns only Item or Service results. The customer explicitly selects `ITEM` or `SERVICE`; AI interpretation cannot change that mode.

## Write path

Every searchable Item/Service mutation synchronously composes the complete PostgreSQL `SearchDocument`, obtains its version from `search_projection_version_seq`, and appends the matching outbox event in the canonical database transaction. Hide/delete writes a durable `DELETE` desired state with a new sequence version. The worker never creates projections.

`SearchDocument` stores scalar `businessId` and nullable `branchId`. Read-only lazy associations hydrate the current Business and branch. The projection retains searchable Item/Service text and canonical currency; the response separately hydrates the current public `BusinessProfile`.

## Delivery safety

The worker prepares the exact desired action/version in a short transaction, calls Meilisearch without a database transaction, and confirms in another short transaction. If desired state changed during the network call, confirmation immediately requeues the newest action/version. This makes stale UPSERT and DELETE operations convergent without holding a transaction across the external call.

`projectionVersion > indexedVersion` is dirty. `indexedAt` records confirmation time only and is not a version.

## Embedding

`embeddingText` is built deterministically from canonical fields: title + description + category + tags + businessName. No AI enrichment at index time. Meilisearch's native HuggingFace embedder generates vectors from `embeddingText` using the configured `documentTemplate`.

## Read path

Meilisearch performs a single native hybrid search (`semanticRatio=0.5`) combining keyword and semantic matching in one API call. DeepSeek provides optional query interpretation (price range, city inference, ambiguity detection) that feeds into Meilisearch filters and post-retrieval ranking adjustments. When DeepSeek is unavailable, a regex-based fallback extracts price ranges from the raw query.

PostgreSQL hydrates Meilisearch results via `SearchDocumentService.findSearchableByAggregateIds`. There is no PostgreSQL full-text fallback or dirty overlay.

## Ranking

Three ASK-specific signals adjust the native Meilisearch `_rankingScore`:

1. **Price penalty**: documents outside explicit or inferred price range are penalized
2. **City penalty**: documents in a different city than the inferred city are penalized
3. **Unique offer boost**: active unique offers receive a fixed score boost

There is no deterministic term matching, attribute scoring, weighted concepts, hypothesis diversification, or multi-signal scoring. Ranking is Meilisearch relevance first, with lightweight ASK-specific adjustments.

## Result presentation

Each result is an Item or Service row with its primary catalog image, a compact business avatar/name, short Item/Service information, price when known, and a chat action. Desktop hover previews the full Item/Service image gallery and details in the right panel; mobile row tap opens the same details as a modal. Business-avatar and chat actions do not open result details. Match reasons remain response metadata but are not displayed. `resultId` is the canonical Item/Service aggregate ID.

Results are sectioned into "exact" (no warnings) and "alternatives" (price or city mismatch). UniqueOffers may boost or decorate linked results but are never standalone search documents, scopes, or cards.

## Import and enrichment

Import approval creates canonical Items/Services, complete projections, and outbox events in one transaction. Imported Items use the same moderation decision as manual creation.

The current import approval is one transaction for every valid row in the in-memory preview. It is all-or-nothing but can hold a transaction too long for large files; bounded chunking requires a separately approved partial-success contract.

Platform AI enrichment updates Item/Service/UniqueOffer fields (description, attributes, tags) via `PlatformAiEnrichmentProcessor`. It does not enrich the search projection with semantic metadata.
