# ASK Backend — Project Locks

Format: `LOCKED | {what} | {why} | {scope}`

Breaking requires: (1) explicit user approval, (2) proof surrounding extension is insufficient.

## Search Locks
LOCKED | Default search sort is intent_match, never price_asc | ASK is an intent layer, not a marketplace. Price ascending commoditizes brands | StructuredSearchProcessor, SearchV2Response
LOCKED | No buy-box logic collapsing different brands into one SKU comparison | Each brand owns its presentation. SKU comparison = marketplace behavior | All search result rendering
LOCKED | AI (DeepSeek) structures queries only — never selects businesses or invents availability | AI cannot know real-time stock/availability | StructuredSearchProcessor, SearchV2Request
LOCKED | Meilisearch is retrieval engine, PostgreSQL is source of truth + hydration | Replaces in-memory scoring with typo-tolerant, synonym-aware search. Fallback to PG if Meilisearch unavailable | StructuredSearchProcessor, MeilisearchService
LOCKED | UniqueOffers are boosters and brand signals, not standalone search results | They boost linked products/services +25 score. DISCOUNT computes effective price. Non-DISCOUNT shows offer name as label | UniqueOffer, unique_offer_product/service/branch tables

## Data Locks
LOCKED | Auto-reply does NOT count as confirmation | Only real business confirmation advances status. Auto-reply != human confirmation | CustomerRequest, SupplierResponse
LOCKED | Contact privacy: contactActionId pattern, never raw phone/username to frontend | HMAC for dedup, encrypted vault for storage | BusinessContact, ContactResolveResponse, frontend contactActionId flow
LOCKED | Never invent stock, delivery, logistics, schedules, or availability | Must come from supplier input or trusted integration data | Product, ServiceBranchOffer, Booking
LOCKED | One concrete sellable variation = one Product entity | Search grouping via tags + SearchDocument. No variant tables | Product entity, catalog domain
LOCKED | Email-only auth for MVP | SMS disabled until real provider connected. Phone removed from entities V8 | identity domain, AuthChallenge, AppUser

## Architecture Locks
LOCKED | Single modular monolith with feature-first packages | One backend for all clients (Android, iOS, web) | kz.ask.* package structure
LOCKED | Layer: Controller → Processor → DomainService → Repository | Processors work with DTOs only. Mappers called only from service impls. No cycles | All feature packages
LOCKED | Entities never leak outside domain service | Return DTOs, not entities. Even service interfaces must not expose entities | All service interfaces
LOCKED | No tests, no Maven auto-run | Project rule until explicitly reversed | Build, CI
