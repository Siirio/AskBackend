# Search — Feature Locks

LOCKED | Default sort is intent_match, never price_asc | ASK is intent layer, not marketplace | UnifiedSearchProcessor scoring
LOCKED | AI structures queries only — never selects businesses or invents availability | AI cannot know real-time stock | AiStructuredQuery, DeepSeek integration
LOCKED | Frontend-selected scope is immutable | Search runs within ITEM or SERVICE chosen by the customer; AI cannot change it | search request DTOs, StructuredSearchProcessor
LOCKED | Meilisearch is retrieval engine, PostgreSQL is source of truth + hydration | Replaces in-memory scoring with typo-tolerant search. Fallback to PG scoring if Meilisearch unavailable | SearchDocument, StructuredSearchProcessor, MeilisearchService
LOCKED | Search scope is ITEM or SERVICE, no standalone business search | Business page opens from Item/Service context, not as a search result type | search_document, SearchSession
LOCKED | Search creates no outreach or chat state | Search is Item/Service retrieval only; customer contact is an explicit separate action | search domain, chat domain
