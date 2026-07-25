# Search — Feature Locks

LOCKED | Default sort is intent_match, never price_asc | ASK is intent layer, not marketplace | StructuredSearchProcessor scoring
LOCKED | AI structures queries only — never selects businesses or invents availability | AI cannot know real-time stock | DeepSeek integration
LOCKED | Meilisearch is retrieval engine, PostgreSQL is source of truth + hydration | Typo-tolerant search with PG fallback | SearchDocument, StructuredSearchProcessor, MeilisearchService
LOCKED | Search creates no outreach or chat state | Search is retrieval only; customer contact is an explicit separate action | search domain, chat domain
LOCKED | Canonical aggregate mutation, PostgreSQL SearchDocument projection, and SearchOutboxEvent are committed atomically in a single @Transactional | Out-of-sync projection causes invisible items; async SearchDocument creation with empty shells caused DEAD events | BusinessProductProcessor, BusinessServiceProcessor, ModerationProcessor, SearchDocumentServiceImpl, SearchOutboxServiceImpl
LOCKED | New items publish to search immediately on manual creation via synchronous SearchDocument projection | Owner must see their item in search right after creating it; moderation rejection removes it; projection version tracks staleness | BusinessProductProcessor.upsertSearchProjection, SearchDocumentService
LOCKED | Search visibility requires isActive=true AND moderationStatus=APPROVED for items, isActive=true for services | Moderated/rejected items must not appear in public search; inactive items hidden | BusinessProductProcessor.upsertSearchProjection, BusinessServiceProcessor.upsertSearchProjection
LOCKED | City is a soft signal unless user explicitly selects it | Hard city filter without user choice hides valid results | SearchRequest.city, StructuredSearchProcessor
LOCKED | DELETE operations use aggregateId, not SearchDocument UUID, for Meilisearch document identity | SearchDocument may be deleted before outbox event processes; aggregateId survives deletion | MeilisearchDocumentMapper, SearchIndexDeliveryServiceImpl
LOCKED | SearchDocument is never created by the async outbox worker; workers only read existing documents created synchronously by processors | Empty-shell findOrCreate() caused constraint violations and DEAD events | SearchProjectionServiceImpl, SearchOutboxScheduler
