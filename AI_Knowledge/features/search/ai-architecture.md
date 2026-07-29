# Search AI Architecture

## Purpose

Search AI provides optional query interpretation (price range, city inference, ambiguity detection). It does not rank results, select businesses, enrich documents at index time, or change the user's selected mode.

## Target package map

```text
kz.ask
├── ai/infrastructure/{client,config}                 shared DeepSeek transport
├── catalog/enrichment/{api,application}              platform-triggered catalog enrichment
└── search
    ├── basic                                         projection, retrieval, ranking, delivery
    └── search_query_enrichment                       query interpretation (AI or regex fallback)
```

`catalog.enrichment` owns the platform action that updates selected catalog records (description, attributes, tags). `search.search_query_enrichment` owns query interpretation only. Shared provider transport is outside both features; neither feature selects search results.

## Responsibility boundaries

| Role | Input | Output | Must not do |
| --- | --- | --- | --- |
| Catalog enrichment | Existing Item, Service, or UniqueOffer text fields | Missing factual description, additive tags, and additive structured attributes | Replace manual data, invent operational facts, or enrich search projections |
| Query interpreter | Raw query, mode, explicit filters | Normalized query, inferred prices/city, ambiguity, suggestions | Change mode/filters, rank cards, or invent availability |

## Read path data flow

```text
raw customer query + mode + explicit filters
  → DeepSeek query interpretation (or regex fallback)
  → single Meilisearch native hybrid search (semanticRatio=0.5)
  → PostgreSQL hydration via SearchDocumentService
  → 3-signal ranking (price penalty, city penalty, offer boost)
  → sectioning (exact / alternatives)
  → response
```

## Write path data flow

```text
Item/Service mutation → deterministic SearchDocument projection → outbox event
  → worker delivers to Meilisearch → confirmation
```

`embeddingText` is composed deterministically from title + description + category + tags + businessName. Meilisearch's HuggingFace embedder generates vectors from this field. No AI writes to the search projection.

## Current violations resolved

- `StructuredSearchProcessor` is now a clean orchestration pipeline: interpret → search → hydrate → rank → respond (~350 lines).
- `SearchTermEnricher` removed — no product verticals embedded in search.
- Semantic enrichment processors removed — no index-time AI enrichment.
- PostgreSQL full-text fallback and dirty overlay removed.
- Reciprocal Rank Fusion replaced by native Meilisearch hybrid search.
- Deterministic ontology (`SearchConceptOntology`), concepts, hypotheses deleted.
