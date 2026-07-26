# Search AI Architecture

## Purpose

Search AI helps ASK understand a user query and enrich the search projection. It is not a second Item/Service store, a moderation engine, or a result selector. PostgreSQL remains canonical, Meilisearch remains the candidate engine, and the selected `ITEM` or `SERVICE` mode remains immutable.

## Target package map

Use lowercase Java packages even when the product name is written as AI.

```text
kz.ask
├── ai/infrastructure/{client,config}                 shared DeepSeek transport and extraction contract
├── catalog/enrichment/{api,application}              platform-triggered canonical catalog enrichment
└── search
    ├── basic                                         deterministic retrieval and projection
    └── ai_driven/search_query_enrichment             query understanding and index-only enrichment
```

`catalog.enrichment` owns the platform action that updates selected catalog records. `search.ai_driven` owns query understanding and derived search metadata only. Shared provider transport is outside both features; neither feature selects search results.

## Responsibility boundaries

| Role | Input | Output | Must not do |
| --- | --- | --- | --- |
| Catalog enrichment | Existing Item, Service, or UniqueOffer text fields | Missing factual description, additive tags, and additive structured attributes | Replace manual data, invent operational facts, use web search, read images, or decide moderation |
| Search enricher | Canonical search projection plus accepted metadata | Index-only aliases, normalized terms, and evidence-bearing attributes | Change canonical Item/Service data or choose a business/result |
| RASE search consultant | Raw query, immutable mode, and explicit filters | Validated query interpretation or retrieval hints | Change mode/filters, query businesses directly, rank cards, or invent stock, delivery, schedules, or availability |

`RASE` is the advisory search-AI boundary: it may consult an AI provider, but all provider output is validated before retrieval. It is optional; deterministic interpretation is the fallback.

## Data flow

```text
Business, Item, or Service DTO -> accepted metadata -> search projection -> Meilisearch
raw customer query + immutable mode -> RASE consultant -> validated interpretation -> candidate retrieval -> deterministic ranking -> response
```

Catalog enrichment may fill only missing descriptions and add text-supported tags or attributes to Item, Service, and UniqueOffer records. It must not replace manual data, change moderation, select a business, create requests/chats/notifications, or claim availability.

## Moderation is a policy, not a search-AI concern

Moderation returns one `ModerationDecision`-shaped value with an outcome, reason code, evidence, and review requirement. A policy may recognize a contextual signal such as a weapon term in a toy description, but that context affects the single decision. It must not create another processor branch for every category exception.

Policy data needs an owner and a documented source. Short-lived experiments may live in versioned configuration; durable policy data belongs in a managed data source. A generic keyword utility is not a policy system.

## Migration rules

1. Move one live responsibility with its consumer and update every import in the same change.
2. Remove `SearchTermEnricher`'s sport-nutrition and bike-rental literals unless a documented owner supplies a curated vocabulary.
3. Split `StructuredSearchProcessor` by runtime responsibility: request orchestration, RASE consultation, candidate retrieval, deterministic ranking, and response mapping. Processors use DTOs only; repositories and entities remain behind domain services.
4. Move Item/Service mutations and moderation out of processors. Their domain services own persistence; mappers own entity/DTO copying.

## Current violations to remove

- `StructuredSearchProcessor` combines repository/entity access, query structuring, candidate retrieval, ranking, and response construction.
- `SearchTermEnricher` embeds product verticals directly in generic search.
- `BusinessProductProcessor` owns `Item` mutation, three repositories, mapping, search-outbox publication, and moderation.
- Catalog enrichment has one target-aware processor rather than three duplicated feature implementations; target-specific allowlists remain inside that processor until dedicated catalog domain services are introduced.

The current package move preserves the existing runtime path. The next cleanup replaces the remaining non-generic vocabulary and DTO/service violations only after the product documentation is approved feature by feature.
