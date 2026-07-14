# Search — REST API Contracts

## Structured Search (V2)
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /api/v1/search | No | AI-structured search across PRODUCT + SERVICE catalog |

### SearchV2Request
- rawQuery (string, required) — raw user query, any language
- scope (string, optional) — PRODUCT or SERVICE
- selectedCategory (string, optional) — category filter
- city (string, optional) — city filter
- sort (string, optional) — intent_match (default), price_asc, price_desc
- userLocation (SearchLocationRequest, optional) — lat/lng for distance
- language (string, optional) — user language

### SearchV2Response
- searchSessionId (UUID) — search session for history and tabs
- rawQuery (String) — original query preserved
- scope (String) — PRODUCT or SERVICE
- understoodQuery (String) — how AI understood the query (Russian)
- sections (List<SearchV2SectionResponse>) — result sections in display order
- supplierCheckCount (Integer) — how many auto supplier checks were created

### SearchV2SectionResponse
- type (String) — section type (exact_products, similar_products, fresh_drops, etc.)
- title (String) — section title in Russian
- cards (List<SearchV2CardResponse>) — cards for this section

### SearchV2CardResponse
- component (String) — ProductCard, ServiceCard, DropCard, BusinessCandidateCard
- resultId (UUID), businessId (UUID), businessName (String)
- brandColor (String), brandLogoUrl (String)
- title (String), price (BigDecimal), availability (String)
- badges (List<String>), distanceMeters (Integer)
- branchName (String), hasActiveDrop (Boolean)
- contactActions (List<ContactActionSummaryResponse>)

## Search Engine
- PostgreSQL is the source of truth and search engine via in-memory scoring
- Meilisearch integration is under investigation — evaluating whether it improves query understanding/guessing
- AI (DeepSeek) structures raw queries into SearchPlan JSON; backend validates and executes
- Default sort is intent_match (relevance). price_asc/price_desc available as user choice, not default
