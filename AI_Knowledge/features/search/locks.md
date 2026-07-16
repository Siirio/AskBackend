# Search — Feature Locks

LOCKED | Default sort is intent_match, never price_asc | ASK is intent layer, not marketplace | UnifiedSearchProcessor scoring
LOCKED | AI structures queries only — never selects businesses or invents availability | AI cannot know real-time stock | AiStructuredQuery, DeepSeek integration
LOCKED | Meilisearch is retrieval engine, PostgreSQL is source of truth + hydration | Replaces in-memory scoring with typo-tolerant search. Fallback to PG scoring if Meilisearch unavailable | SearchDocument, StructuredSearchProcessor, MeilisearchService
LOCKED | Search scope is PRODUCT or SERVICE, no standalone business search | Business page opens from product/service context, not as search result type | search_document, SearchSession
LOCKED | Auto supplier check is NOT customer-visible chat | Must not create conversation_message or customer unread notification | SupplierResponse, request_target
