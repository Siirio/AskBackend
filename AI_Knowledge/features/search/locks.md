# Search — Feature Locks

LOCKED | Search mode is exactly ITEM or SERVICE and is explicit user input | AI cannot change the customer-selected result domain | SearchRequest, StructuredSearchProcessor
LOCKED | Search returns only ItemCard or ServiceCard rows | Business profile and UniqueOffer are context, never standalone search results | SearchCardResponse, StructuredSearchProcessor
LOCKED | Default sort is relevance, never price ascending | ASK is an intent layer, not a marketplace | StructuredSearchProcessor
LOCKED | Search creates no outreach or chat state | Chat is a separate explicit action using business identity | search and messaging domains
LOCKED | Meilisearch is the sole retrieval engine; PostgreSQL is canonical and hydration only | External index loss must not lose canonical search state | SearchDocument, MeilisearchIndexGateway
LOCKED | Canonical aggregate mutation, complete SearchDocument desired state, and outbox append commit atomically | Prevents invisible or partially published Items/Services | Item, Service, moderation, import, enrichment write paths
LOCKED | Only search_projection_version_seq creates projection/outbox aggregate versions | Mixed timestamp and sequence versions permanently supersede valid events | SearchDocumentService
LOCKED | SearchDocument stores scalar businessId and nullable branchId | Item/Service cards must retain Business profile and optional location context | SearchDocument, SearchDocumentMapper
LOCKED | Worker never holds a database transaction during Meilisearch calls | External latency must not hold database connections or locks | SearchIndexDeliveryServiceImpl
LOCKED | Delivery confirmation requeues the newest desired action/version after any interleaving mutation | Old UPSERT/DELETE may not remain final in Meilisearch | SearchDeliveryConfirmationProcessor
LOCKED | Explicit filters are hard; interpreted query signals are soft | AI/text inference may be wrong | SearchPlan, StructuredSearchProcessor
LOCKED | Distance affects rank only for explicit distance sorting | Coordinates alone are not location intent | StructuredSearchProcessor
LOCKED | Search filters and sorts execute server-side across the full eligible catalogue before pagination | A loaded-page refinement produces incomplete and misleading results | SearchRequest, SearchPlan, MeilisearchIndexGatewayImpl, ResultsPage
LOCKED | Search returns bounded pages and permits traversal until the catalogue is exhausted | Infinite scroll must not stop at an internal candidate cap or download the catalogue in one batch | SearchRequest, MeilisearchIndexGatewayImpl, ResultsPage
LOCKED | Match reasons may remain response metadata but are not displayed | Customer result presentation does not expose “why this matched” | SearchCardResponse, ResultCard, ResultsPage
LOCKED | Public resultId equals aggregateId | SearchDocument UUID is internal projection identity | SearchCardResponse
LOCKED | Relevance, distance, and price use native Meilisearch ordering; Unique-Offers ordering composes active and inactive Meilisearch result ranges before returning the requested page | Global ordering must remain stable across page boundaries | MeilisearchIndexGatewayImpl
LOCKED | Generative AI does not rank businesses; it provides optional query interpretation only | Query understanding may be probabilistic while ranking remains deterministic | SearchPlan, StructuredSearchProcessor
LOCKED | embeddingText is built deterministically from canonical fields; no AI enrichment at index time | Keeps index-time logic simple and predictable | SearchProjectionComposer, SearchDocument
LOCKED | Canonical search terminology is ITEM and SERVICE | PRODUCT/ALL compatibility values create contract drift | search enums, request/response, AI prompts
