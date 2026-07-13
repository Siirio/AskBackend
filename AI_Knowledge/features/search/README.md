# Unified Search

Product and service search via PostgreSQL `SearchDocument` with in-memory scoring. DeepSeek AI structures raw queries into `AiStructuredQuery` JSON. Single endpoint covers both scopes — AI determines intent.

## Key decisions
- PostgreSQL is source of truth and search engine. Meilisearch deferred.
- Single endpoint POST /api/v1/search/unified — no more scope toggle.
- DeepSeek AI: structures query (searchTerms, categoryHints, brands, priceRange). NEVER selects businesses or invents availability.
- Scoring: TITLE_MATCH(30) + BODY_MATCH(15) + TOKEN_MATCH(20) + CATEGORY_MATCH(10) + BRAND_MATCH(10) + OFFER_BOOST(25) - PRICE_OVER_BUDGET(40).
- UniqueOffer boosting: DISCOUNT computes effectivePrice. Non-DISCOUNT shows offer name as label.
- Fallback: simple text matching when AI API key unset.
- SearchQueryAlias: data-owned expansion table for broad customer wording.
- Search sessions preserve historical snapshots. Customer history reopens fixed saved result state.
