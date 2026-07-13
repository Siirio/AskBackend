# Search — Feature Locks

LOCKED | Default sort is intent_match, never price_asc | ASK is intent layer, not marketplace | UnifiedSearchProcessor scoring
LOCKED | AI structures queries only — never selects businesses or invents availability | AI cannot know real-time stock | AiStructuredQuery, DeepSeek integration
LOCKED | PostgreSQL is search engine via in-memory scoring | Meilisearch deferred. No external search dependency | SearchDocument, UnifiedSearchProcessor
LOCKED | Search scope is PRODUCT or SERVICE, no standalone business search | Business page opens from product/service context, not as search result type | search_document, SearchSession
LOCKED | Auto supplier check is NOT customer-visible chat | Must not create conversation_message or customer unread notification | SupplierResponse, request_target
