# ASK Backend — Project Locks

Format: `LOCKED | {what} | {why} | {scope}`

Breaking requires: (1) explicit user approval, (2) proof surrounding extension is insufficient.

## Search Locks
LOCKED | Default search sort is intent_match, never price_asc | ASK is an intent layer, not a marketplace. Price ascending commoditizes brands | StructuredSearchProcessor, SearchV2Response
LOCKED | No buy-box logic collapsing different brands into one SKU comparison | Each brand owns its presentation. SKU comparison = marketplace behavior | All search result rendering
LOCKED | Frontend-selected search scope is immutable | Customer selects PRODUCT or SERVICE; AI structures only that selected scope | search request DTOs, StructuredSearchProcessor
LOCKED | AI (DeepSeek) structures queries only — never selects businesses or invents availability | AI cannot know real-time stock/availability | StructuredSearchProcessor, SearchV2Request
LOCKED | Search is catalog-only | Search never creates requests, supplier outreach, notifications, or chats | search domain, request/chat integrations
LOCKED | Meilisearch is retrieval engine, PostgreSQL is source of truth + hydration | Replaces in-memory scoring with typo-tolerant, synonym-aware search. Fallback to PG if Meilisearch unavailable | StructuredSearchProcessor, MeilisearchService
LOCKED | UniqueOffers are boosters and brand signals, not standalone search results | They boost linked products/services +25 score. DISCOUNT computes effective price. Non-DISCOUNT shows offer name as label | UniqueOffer, unique_offer_product/service/branch tables

## Data Locks
LOCKED | One customer and business share one durable conversation | Branches and catalog cards are entry points, not conversation identity | ChatConversation, ChatServiceImpl, business inbox
LOCKED | Business membership defaults apply to all branches | Branch overrides may change role or deny access; multiple owners are allowed | BusinessMember, branch access overrides, staff authorization
LOCKED | Never invent stock, logistics, schedules, or availability | Must come from supplier input or trusted integration data | Product, ServiceBranchOffer, Booking
LOCKED | No intermediate import entities between file upload and catalog | Files transfer directly to catalog. User exit = progress lost | CatalogImport, RawCatalogRow, CatalogImportColumnMapping, all import pipeline
LOCKED | Delivery is per-branch, not per-business | Each branch sets its own delivery mode. No business-level delivery profile | BusinessDeliveryProfile, BusinessBranchDeliveryOverride, shipping
LOCKED | One concrete sellable variation = one Product entity | Search grouping via tags + SearchDocument. No variant tables | Product entity, catalog domain
LOCKED | One published catalog item has one curated category and one canonical attributes map | Description and tags supplement JSONB attributes; imports may propose values before approval | Product, ServiceOffering, import and catalog mappers
LOCKED | Email-based auth for MVP | Password/OTP and Google OAuth use verified email. SMS remains disabled until a real provider is connected | identity domain, AuthChallenge, AppUser, Google OAuth

LOCKED | Business has both bin (БИН for ТОО) and iin (ИИН for ИП) fields | Kazakhstan legal identifiers differ by legal form. Both fields needed, set based on whether business is ИП or ТОО | Business entity, business table
LOCKED | Product and ServiceOffering must be creatable without a branch | Online stores don't have physical branches. Branch assignment comes later when needed | Product, ServiceOffering entities, catalog/service creation flows

## Architecture Locks
LOCKED | Single modular monolith with feature-first packages | One backend for all clients (Android, iOS, web) | kz.ask.* package structure
LOCKED | Layer: Controller → Processor → DomainService → Repository | Processors work with DTOs only. Mappers called only from service impls. No cycles | All feature packages
LOCKED | Entities never leak outside domain service | Return DTOs, not entities. Even service interfaces must not expose entities | All service interfaces
LOCKED | Unit tests are Mockito-only (no Spring context, no DB); Maven runs only on explicit user request | Spec section 25 mandates unit tests; builds stay user-controlled | Build, CI, src/test
LOCKED | No defaults at any level — no DEFAULT in DDL, no field initializers in entity classes | All values set explicitly by business logic before save. DB and entities store only what code explicitly writes | All entities, all Flyway migrations
