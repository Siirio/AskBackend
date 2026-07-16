# ASK Backend — Changelog

Format: `YYYY-MM-DD | {decision/rationale} | {affected files/features}`

## 2026-07-14 | Cross-repo lookups fixed: the frontend rebuilt on Next.js + Vertical Slice Architecture, and its feature folders are named after ITS slices, not our modules (messaging→chats, service→services, request→requests, identity→auth+profile, offers/import→business-cabinet/catalog). Feature Index gained a Frontend slice column as the lookup key; paths corrected from ../ask-frontend to ../Ask_Frontend | CLAUDE.md, AGENTS.md
2026-07-13 | AI system V2.0: self-bootstrapping CLAUDE.md orchestrator replaces AGENTS.md-only setup | CLAUDE.md, AGENTS.md, AI_Knowledge/*
## 2026-07-13 | AI system V2.0: self-bootstrapping CLAUDE.md orchestrator replaces AGENTS.md-only setup | CLAUDE.md, AGENTS.md, AI_Knowledge/*
2026-07-12 | Backend Restructuring V2 (6 phases): deleted messaging/ + BrandExperienceController + BrandPageBlock + BusinessCard; email-only auth; 7 AppRoles; M2M product_branch/service_branch; BrandDrop→UniqueOffer booster; simplified chat (text-only, no statuses, unread counts); UnifiedSearchProcessor with DeepSeek AI + in-memory scoring; roles/permissions with MANAGER hierarchy; shipping settings + customer profile | 50+ files, V8 migration
2026-07-04 | Search infrastructure audit: Meilisearch (deferred), contact privacy architecture, storefront builder, drops, public ingestion specs created (tasks 07-11) | AGENTS.md, ARCHITECTURE_NARRATIVE.md, 5 new task specs
2026-07-01 | Anti-marketplace contract: brand layer + decision layer cards, no buy-box, intent_match default sort, no public ratings, UniqueOffers as brand signals | AGENTS.md, ProductVision
2026-06-30 | DeepSeek structured search + AI Autodump: intent structuring endpoint, AI-assisted import pipeline (raw dump → draft → preview → publish) | SearchPlan, UnifiedSearchProcessor, AI_AUTODUMP_ARCHITECTURE
2026-06-25 | Snake_case/camelCase sync: frontend transformKeys() in httpClient; CORS PATCH fix; public reference endpoints (cities, categories) | SecurityConfig, CorsConfig, httpClient.ts
2026-06-22 | Product Excel Import: fastexcel parser, auto-mapping engine, 5-endpoint import pipeline | 26 new files, catalog/import domain
2026-06-21 | Identity Auth + Staff Management + Role Simplification (OWNER/STAFF only → later expanded to OWNER/MANAGER/WORKER) | identity/, business/, V1-V2 migrations
2026-06-18 | Initial architecture: UUIDv7 entities, feature-first packages, Lombok JPA foundation | pom.xml, BaseUuidV7Entity, entity foundations
