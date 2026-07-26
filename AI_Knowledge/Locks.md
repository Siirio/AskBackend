# ASK Backend — Project Locks

Format: `LOCKED | {what} | {why} | {scope}`

Breaking requires: (1) explicit user approval, (2) proof surrounding extension is insufficient.

## Search Locks
LOCKED | Default search sort is intent_match, never price_asc | ASK is an intent layer, not a marketplace. Price ascending commoditizes brands | StructuredSearchProcessor, SearchV2Response
LOCKED | No buy-box logic collapsing different brands into one SKU comparison | Each brand owns its presentation. SKU comparison = marketplace behavior | All search result rendering
LOCKED | Search mode is exactly ITEM or SERVICE and is selected by the user | Customer controls the result domain; AI cannot change it | SearchRequest, StructuredSearchProcessor
LOCKED | AI may structure queries and perform platform-only text catalog enrichment, but never selects businesses or invents availability | Catalog enrichment may only fill missing descriptions and add text-supported tags/attributes; AI cannot know operational facts | StructuredSearchProcessor, PlatformAiEnrichmentProcessor, catalog entities
LOCKED | Search returns only Item or Service rows enriched with public Business profile and optional branch context | Business is presentation/context, not a standalone result; search never creates requests, outreach, notifications, or chats | search domain, SearchCardResponse
LOCKED | Meilisearch is retrieval engine, PostgreSQL is source of truth + hydration | Replaces in-memory scoring with typo-tolerant, synonym-aware search. Fallback to PG if Meilisearch unavailable | StructuredSearchProcessor, MeilisearchService
LOCKED | UniqueOffers only boost or decorate linked Item/Service results and never appear as standalone search results | Offers are brand signals, not another search corpus | UniqueOffer, unique_offer_product/service/branch tables

## Data Locks
LOCKED | One customer and business share one durable conversation | Branches and catalog cards are entry points, not conversation identity | ChatConversation, ChatServiceImpl, business inbox
LOCKED | Business membership defaults apply to all branches | Branch overrides may change role or deny access; multiple owners are allowed | BusinessMember, branch access overrides, staff authorization
LOCKED | Never invent stock, logistics, schedules, or availability | Must come from supplier input or trusted integration data | Product, ServiceBranchOffer, Booking
LOCKED | No intermediate import entities between file upload and Item/Service creation | Files transfer directly to the target feature. User exit = progress lost | import pipeline
LOCKED | Delivery is per-branch, not per-business | Each branch sets its own delivery mode. No business-level delivery profile | BusinessDeliveryProfile, BusinessBranchDeliveryOverride, shipping
LOCKED | One concrete sellable variation = one Item entity | Search grouping uses tags and SearchDocument. No variant tables | Item entity
LOCKED | Business, Item, and Service each store one typed flat category plus canonical attributes | Description and tags supplement attributes; categories are SYSTEM or USER and never hierarchical | Business, Item, Service, category/search flows
LOCKED | Email-based auth for MVP | Password/OTP and Google OAuth use verified email. SMS remains disabled until a real provider is connected | identity domain, AuthChallenge, AppUser, Google OAuth

LOCKED | Business has both bin (БИН for ТОО) and iin (ИИН for ИП) fields | Kazakhstan legal identifiers differ by legal form. Both fields needed, set based on whether business is ИП or ТОО | Business entity, business table
LOCKED | Item and Service must be creatable without a branch | Online businesses do not need physical branches. Branch association comes later only when location-specific behaviour is required | Item, Service, branch association flows

## Architecture Locks
LOCKED | Single modular monolith with feature-first packages | One backend for all clients (Android, iOS, web) | kz.ask.* package structure
LOCKED | Layer: Controller → Processor → DomainService → Repository | Processors work with DTOs only. Mappers called only from service impls. No cycles | All feature packages
LOCKED | Entities never leak outside domain service | Return DTOs, not entities. Even service interfaces must not expose entities | All service interfaces
LOCKED | Unit tests are Mockito-only (no Spring context, no DB); Maven runs only on explicit user request | Spec section 25 mandates unit tests; builds stay user-controlled | Build, CI, src/test
LOCKED | No defaults at any level — no DEFAULT in DDL, no field initializers in entity classes | All values set explicitly by business logic before save. DB and entities store only what code explicitly writes | All entities, all Flyway migrations
