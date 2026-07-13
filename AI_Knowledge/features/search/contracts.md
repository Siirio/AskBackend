# Search — REST API Contracts

## Unified Search
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /api/v1/search/unified | No | Single search across PRODUCT + SERVICE catalog |

### UnifiedSearchRequest
- query (string, required) — raw user query, any language
- cityId (UUID, optional) — city filter
- limit (1-100, default 20)

### UnifiedSearchResponse
- results: Array<UnifiedSearchResultItem>
- aiStructuredQuery: AiStructuredQuery (internal, for debugging)
- totalFound: int

### UnifiedSearchResultItem
type, id, name, description, effectivePrice, originalPrice, imageUrl, categoryName, businessName, branchIds, activeOfferId, offerLabel, score

### AiStructuredQuery (internal)
intent (goods/service/both), searchTerms[], categoryHints[], priceRange {min, max}, brands[], originalQuery

## Legacy
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /api/v1/search | No | Legacy literal search (compatibility) |

## Search Sessions (history)
searchSession → searchSnapshot → searchResultSnapshot (saves what user saw, not live data)
Retention: customer history ~10 days. Business operational history longer-lived.
