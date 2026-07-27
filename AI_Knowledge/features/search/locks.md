# Search — Feature Locks

LOCKED | Search mode is exactly ITEM or SERVICE and is explicit user input | AI cannot change the customer-selected result domain | SearchRequest, StructuredSearchProcessor
LOCKED | Search returns only ItemCard or ServiceCard rows | Business profile and UniqueOffer are context, never standalone search results | SearchCardResponse, StructuredSearchProcessor
LOCKED | Default sort is relevance/intent match, never price ascending | ASK is an intent layer, not a marketplace | StructuredSearchProcessor
LOCKED | Search creates no outreach or chat state | Chat is a separate explicit action using business identity | search and messaging domains
LOCKED | Meilisearch is primary bounded lexical retrieval; PostgreSQL is canonical, hydration, and fallback | External index loss must not lose canonical search state | SearchDocument, MeilisearchIndexGateway
LOCKED | Canonical aggregate mutation, complete SearchDocument desired state, and outbox append commit atomically | Prevents invisible or partially published Items/Services | Item, Service, moderation, import, enrichment write paths
LOCKED | Only search_projection_version_seq creates projection/outbox aggregate versions | Mixed timestamp and sequence versions permanently supersede valid events | SearchDocumentService
LOCKED | SearchDocument stores scalar businessId and nullable branchId | Item/Service cards must retain Business profile and optional location context | SearchDocument, SearchDocumentMapper
LOCKED | Worker never holds a database transaction during Meilisearch calls | External latency must not hold database connections or locks | SearchIndexDeliveryServiceImpl
LOCKED | Delivery confirmation requeues the newest desired action/version after any interleaving mutation | Old UPSERT/DELETE may not remain final in Meilisearch | SearchDeliveryConfirmationProcessor
LOCKED | Dirty overlay is query-relevant and activates only for a nonblank query | Read-your-writes must not inject unrelated projections | StructuredSearchProcessor
LOCKED | Explicit filters are hard; interpreted query signals are soft | AI/text inference may be wrong | SearchPlan, StructuredSearchProcessor, MeilisearchIndexGatewayImpl
LOCKED | Distance affects rank only for explicit distance sorting | Coordinates alone are not location intent | StructuredSearchProcessor
LOCKED | Public resultId equals aggregateId | SearchDocument UUID is internal projection identity | SearchCardResponse
LOCKED | At most two Meilisearch lexical requests execute per public query and use deterministic rank fusion | Recall expansion remains bounded | MeilisearchIndexGatewayImpl
LOCKED | One additional semantic Meilisearch lane may execute and must degrade independently without disabling lexical retrieval | Semantic recall must not make search dependent on embeddings | MeilisearchIndexGatewayImpl
LOCKED | Retrieval preserves raw lexical, expanded lexical, and semantic lane ranks plus fusion score until deterministic ranking finishes | Reconstructing relevance from a flat aggregate-ID position discards useful evidence | SearchCandidateDto, SearchCandidateSetDto
LOCKED | Generative AI does not rank businesses; weighted hypotheses may guide deterministic scoring and diversification | Query understanding may be probabilistic while eligibility and final policy remain deterministic | SearchPlan, StructuredSearchProcessor
LOCKED | Semantic aliases, concepts, use cases, summaries, evidence, and model metadata belong to SearchDocument only | Derived search meaning must not pollute canonical Item or Service facts | SearchDocument, SearchProjectionComposer, SearchSemanticEnrichmentProcessor
LOCKED | Controlled concept IDs are validated by the backend and unknown IDs are discarded | Free-form model labels create ontology drift | SearchConcept, SearchConceptOntology, DeepSeekAttributeExtractor
LOCKED | Canonical search terminology is ITEM and SERVICE | PRODUCT/ALL compatibility values create contract drift | search enums, request/response, AI prompts
