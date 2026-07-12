# Approved Logic Locks

## LOCKED: Business card draft and published snapshots are separate
- Scope: src/main/java/kz/ask/business/api/BusinessCardController.java; src/main/java/kz/ask/business/application/BusinessCardProcessor.java; src/main/java/kz/ask/business/domain/BusinessCardService*.java; src/main/java/kz/ask/business/domain/entity/BusinessCard.java; src/main/resources/db/migration/V3__business_card.sql
- Approved signal: user requested: do these and lock the rules
- Protected behavior: Business-card draft blocks and published blocks are separate persisted snapshots; save updates draft, publish accepts and saves the latest request body before copying draft to publishedBlocks, owner reads draft, public GET reads only published content, and publishedAt reflects publication time.
- Reuse pattern: Preserve the existing implementation pattern unless the unlock condition is met.
- Allowed changes: Schema evolution, validation, and DTO extraction are allowed if draft/published separation and publish-latest behavior do not regress.
- Unlock condition: Explicit user request to change this behavior or evidence that the core execution pipeline changed.
- Created: 2026-07-06

## LOCKED: Search query expansion covers smartphone wording
- Scope: src/main/java/kz/ask/search/domain/SearchTermEnricher.java
- Approved signal: user requested: do these and lock the rules
- Protected behavior: Search indexing and intent expansion must connect телефон, смартфон, iphone/айфон, Samsung/Galaxy, Apple, and Android terms so businesses are not forced to manually add every synonym/tag and customer product search is not exact-word-only.
- Reuse pattern: Preserve the existing implementation pattern unless the unlock condition is met.
- Allowed changes: Moving the dictionary to data-owned aliases, Meilisearch, embeddings, or LLM metadata is allowed if the same or better semantic recall is preserved and internal tags/scores are not shown as ugly frontend tags.
- Unlock condition: Explicit user request to change this behavior or evidence that the core execution pipeline changed.
- Created: 2026-07-06

