# Search AI Architecture

## Purpose

Search AI helps ASK understand a user query and enrich the search projection. It is not a second catalog, a moderation engine, or a result selector. PostgreSQL remains canonical, Meilisearch remains the candidate engine, and the selected `PRODUCT` or `SERVICE` scope remains immutable.

## Target package map

Use lowercase Java packages even when the product name is written as AI.

```text
kz.ask.search.search
├── basic
│   ├── api/PublicSearchController
│   ├── application/processor/StructuredSearchProcessor
│   ├── domain/{SearchProjectionService, MeilisearchService, SearchOutboxService}
│   └── infrastructure/{config, mapper, repository, scheduler}
└── ai
    ├── api/PlatformAiEnrichmentController
    ├── application/PlatformAiEnrichmentProcessor
    ├── domain/{SearchIntentStructurer, SearchAiEnrichmentService}
    └── infrastructure/{cache, client, config, repository, scheduler}
```

`basic` owns deterministic retrieval and projection. `ai` owns provider-facing query interpretation and enrichment. The basic slice may call the AI slice as an optional consultant; the AI slice does not select results.

## Responsibility boundaries

| Role | Input | Output | Must not do |
| --- | --- | --- | --- |
| Item setter enricher | Item creation/update DTO and accepted evidence | Proposed item search metadata | Persist an item, set an entity field, publish an outbox event, or decide moderation |
| Service setter enricher | Service creation/update DTO and accepted evidence | Proposed service search metadata | Persist a service, set an entity field, publish an outbox event, or decide booking or availability |
| Search enricher | Canonical search projection plus accepted metadata | Index-only aliases, normalized terms, and evidence-bearing attributes | Change canonical catalog data or choose a business/result |
| RASE search consultant | Raw query, immutable scope, and explicit filters | Validated query interpretation or retrieval hints | Change scope/filters, query businesses directly, rank cards, or invent stock, delivery, schedules, or availability |

`RASE` is the advisory search-AI boundary: it may consult an AI provider, but all provider output is validated before retrieval. It is optional; deterministic interpretation is the fallback.

## Data flow

```text
catalog DTO -> item/service setter enricher -> accepted metadata -> search projection -> search enricher -> Meilisearch
raw customer query + immutable scope -> RASE consultant -> validated interpretation -> candidate retrieval -> deterministic ranking -> response
```

No arrow from an enricher goes to a catalog entity, moderation status, business selection, request, chat, notification, or availability claim.

## Moderation is a policy, not a search-AI concern

Moderation returns one `ModerationDecision`-shaped value with an outcome, reason code, evidence, and review requirement. A policy may recognize a contextual signal such as a weapon term in a toy description, but that context affects the single decision. It must not create another processor branch for every category exception.

Policy data needs an owner and a documented source. Short-lived experiments may live in versioned configuration; durable policy data belongs in a managed data source. A generic keyword utility is not a policy system.

## Migration rules

1. Move one live responsibility with its consumer and update every import in the same change.
2. Remove `SearchTermEnricher`'s sport-nutrition and bike-rental literals unless a documented owner supplies a curated vocabulary.
3. Split `StructuredSearchProcessor` by runtime responsibility: request orchestration, RASE consultation, candidate retrieval, deterministic ranking, and response mapping. Processors use DTOs only; repositories and entities remain behind domain services.
4. Move catalog mutations and moderation out of processors. Their domain services own persistence; mappers own entity/DTO copying.

## Current violations to remove

- `StructuredSearchProcessor` combines repository/entity access, query structuring, candidate retrieval, ranking, and response construction.
- `SearchTermEnricher` embeds product verticals directly in generic search.
- `BusinessProductProcessor` owns `Item` mutation, three repositories, mapping, search-outbox publication, and moderation.
- `PlatformAiEnrichmentProcessor` mutates `SearchDocument` directly instead of using a DTO-only domain service boundary.

The current package move preserves the existing runtime path. The next cleanup replaces the remaining non-generic vocabulary and DTO/service violations only after the product documentation is approved feature by feature.
