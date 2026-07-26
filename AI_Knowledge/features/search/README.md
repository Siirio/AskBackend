# Unified Search

ASK search returns only Item or Service results. The customer explicitly selects `ITEM` or `SERVICE`; deterministic or AI-assisted interpretation cannot change that mode.

## Write path

Every searchable Item/Service mutation synchronously composes the complete PostgreSQL `SearchDocument`, obtains its version from `search_projection_version_seq`, and appends the matching outbox event in the canonical database transaction. Hide/delete writes a durable `DELETE` desired state with a new sequence version. The worker never creates projections.

`SearchDocument` stores scalar `businessId` and nullable `branchId`. Read-only lazy associations hydrate the current Business and branch. The projection retains searchable Item/Service text and canonical currency; the response separately hydrates the current public `BusinessProfile`.

## Delivery safety

The worker prepares the exact desired action/version in a short transaction, calls Meilisearch without a database transaction, and confirms in another short transaction. If desired state changed during the network call, confirmation immediately requeues the newest action/version. This makes stale UPSERT and DELETE operations convergent without holding a transaction across the external call.

`projectionVersion > indexedVersion` is dirty. `indexedAt` records confirmation time only and is not a version.

## Read path

Meilisearch performs at most two bounded lexical requests:

1. complete normalized raw query;
2. one combined expanded query containing approved aliases, synonyms, category terms, and related terms.

The two rankings use Reciprocal Rank Fusion. PostgreSQL fallback uses the complete raw/expanded query rather than one first term. PostgreSQL hydrates canonical projection rows and the public Business profile. A query-relevant dirty overlay provides read-your-writes behavior.

Only explicit filters eliminate candidates. Interpreted prices, cities, qualifiers, package sizes, `mustHave`, and `notWanted` values are ranking signals. Coordinates affect ranking only for explicit distance sorting.

## Result presentation

Each result is an Item or Service row with business logo/name, short Item/Service information, price when known, and a chat action. Opening the row presents the full Item/Service description plus the public Business profile: logo, cover, description, phone, email, Instagram, Telegram, and website. `resultId` is the canonical Item/Service aggregate ID.

UniqueOffers may boost or decorate linked results but are never standalone search documents, scopes, or cards.

## Import and enrichment

Import approval creates canonical Items/Services, complete projections, and outbox events in one transaction. Imported Items use the same moderation decision as manual creation.

The current import approval is one transaction for every valid row in the in-memory preview. It is all-or-nothing but can hold a transaction too long for large files; bounded chunking requires a separately approved partial-success contract.

AI extraction runs outside database transactions. A short mutation transaction reloads current entities, applies additive enrichment, rebuilds each still-searchable projection, and publishes the returned sequence version. Inactive or hidden records receive a durable DELETE desired state.
