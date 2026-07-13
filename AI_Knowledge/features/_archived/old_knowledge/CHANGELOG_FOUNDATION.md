# Foundation Changelog

## 2026-07-12 — Backend Restructuring V2: Phases 1-4

Four-phase restructuring per `backend_tasks/12_backend_restructuring_v2.md`. Compilation verified clean after each phase.

### Phase 1: Foundation — Deletions + Schema

**Deleted modules and entities:**
- `messaging/` package (Conversation, ConversationMessage, ConversationParticipant, ConversationLink) — replaced by simplified chat/
- `BrandExperienceController` (12 endpoints) — storefront builder removed
- `BrandPageBlock` entity + table — no more custom storefront pages
- `BusinessCard` entity + table — Canva-like builder removed
- `BrandPageBlockType` enum, `StorefrontPageStatus` enum

**Auth cleanup — email-only:**
- Removed `phone` from AppUser, AuthChallenge entities and V1 migration
- Removed `AuthChallengeChannel.PHONE` enum value
- All SMS adapters and integrations deleted
- AuthChallenge now uses EMAIL channel only (verification code + optional 2FA)

**Role expansion:**
- `AppRole`: CUSTOMER, BUSINESS → expanded to 7 values: CUSTOMER, BUSINESS_OWNER, BUSINESS_MANAGER, BUSINESS_WORKER, PLATFORM_SUPER_ADMIN, PLATFORM_ADMIN, PLATFORM_MODERATOR
- `BusinessMemberRole`: OWNER only → expanded to OWNER, MANAGER, WORKER
- `BranchMemberRole`: synced to OWNER, MANAGER, WORKER
- Hierarchy: OWNER > MANAGER > WORKER. PLATFORM_SUPER_ADMIN is DB-only (no API creation).

**V8 migration:** All schema changes in one migration file — adds new columns/tables, drops legacy tables (conversation*, brand_page_block, business_card, brand_drop_product, brand_drop_tag), drops phone columns.

### Phase 2: Core Entity Restructuring

**Product + Service M2M branches:**
- `product_branch` and `service_branch` join tables — one product/service can now be linked to multiple branches
- Product entity: added `price` (NUMERIC)
- Business entity: added `currency` (VARCHAR(3), default KZT), `shippingMode`, `shippingCityIds` (JSONB)

**BrandDrop → UniqueOffer (booster, not standalone search result):**
- Table renamed: `brand_drop` → `unique_offer`
- New fields: `discount_percent` (INTEGER), `discount_amount` (NUMERIC), `enabled` (BOOLEAN, default true), `currency` (VARCHAR(3)), `tags` (JSONB)
- `UniqueOfferType`: DISCOUNT, NEW_COLLECTION, LIMITED_RELEASE, RESTOCK, CAPSULE, SEASONAL, COLLAB, PREORDER
- `UniqueOfferStatus`: UPCOMING, ACTIVE, ENDED, CANCELLED
- M2M join tables: `unique_offer_product`, `unique_offer_service`, `unique_offer_branch`
- One offer can link to products AND services AND branches simultaneously
- Offer can be toggled (enabled/disabled) without deletion

**SearchDocument cleanup:**
- Removed `brand_drop_id` column — UniqueOffers are boosters, not standalone search documents
- `SearchDocumentType`: removed `DROP` enum value — only PRODUCT and SERVICE remain

**CustomerProfile:** Removed `phone`, added `iconUrl` (VARCHAR(2048))

**Deleted 9 BrandDrop files:** entity, service interface + impl, DTO, repository, type enum, status enum, request DTO, response DTO.

**PublicSearchProcessor updated:** All `BrandDrop` references → `UniqueOffer`. Method `resolveDrops()` → `resolveOffers()`. Uses `UniqueOfferService` instead of `BrandDropService`.

**2 leftover BrandPageBlock DTOs cleaned up** (BrandPageBlockRequest, BrandPageBlockResponse — missed in Phase 1 deletions).

### Phase 3: Chat Restructuring

**ChatConversation simplified — text-only, no statuses:**
- Removed: `status` (ConversationStatus enum), `source`, `searchQuery`, `requestTargetId`
- Added: `customerUnreadCount` (INTEGER, default 0), `businessUnreadCount` (INTEGER, default 0)
- Entity now has only: id, businessId, customerId, subject, customerUnreadCount, businessUnreadCount, lastMessageAt, createdAt, updatedAt

**ChatMessage simplified:**
- Removed `attachmentUrl` — text + emoji only, no files/attachments

**Deleted:** `ConversationStatus` enum, `ChatRequestBridge` component (bridged chats to request_target — no longer needed)

**ChatServiceImpl rewritten:**
- `startConversation()`: no status/source/searchQuery, no ChatRequestBridge call
- `sendMessage()`: increments `businessUnreadCount` (customer messages) or `customerUnreadCount` (business messages); no status transitions, no attachmentUrl
- `markRead()`: resets `customerUnreadCount=0` or `businessUnreadCount=0` based on readerType
- `notifyBusinesses()`: removed searchQuery parameter
- DTO mapping: no status/source/searchQuery, maps unread counts instead

**Controllers simplified:**
- ChatController: `startConversation()` no longer accepts `searchQuery`
- BusinessChatController: removed `updateStatus()` PATCH endpoint entirely
- SystemNotifyRequest: `searchQuery` field kept for backward compat but no longer @NotBlank

### Phase 4: Unified AI-Powered Search

**New endpoint:** `POST /api/v1/search/unified` — single search across PRODUCT + SERVICE catalog. AI structures raw query; no more scope toggle.

**New DTOs:**
- `UnifiedSearchRequest`: query (required), cityId (optional), limit (1-100, default 20)
- `UnifiedSearchResultItem`: type, id, name, description, effectivePrice, originalPrice, imageUrl, categoryName, businessName, branchIds, activeOfferId, offerLabel, score
- `UnifiedSearchResponse`: results, aiStructuredQuery, totalFound
- `AiStructuredQuery` (inner class): intent (goods/service/both), searchTerms, categoryHints, priceRange (min/max), brands, originalQuery
- `PriceRange` (inner class): min, max

**UnifiedSearchProcessor:**
- DeepSeek AI integration: POST to `/chat/completions` with JSON response_format, parses searchTerms/categoryHints/brands/priceRange
- Falls back to simple text matching when `ask.ai.search.api-key` is unset
- Scoring: TITLE_MATCH(30) + BODY_MATCH(15) + TOKEN_MATCH(20) + CATEGORY_MATCH(10) + BRAND_MATCH(10) + OFFER_BOOST(25) - PRICE_OVER_BUDGET(40)
- UniqueOffer boosting: `findBestOffer()` matches by business ID from active UniqueOffers; if DISCOUNT type, computes `effectivePrice` (percent discount: `price * (1 - percent/100)`, amount discount: `price - amount`)
- `formatOfferLabel()`: "-30%" for percent, "-5000 ₸" for amount, or offer name for non-DISCOUNT types
- Price penalty: items over budget max get -40 score

**UniqueOfferRepository:** Added `findByStatusIn(List<UniqueOfferStatus>)` for active offer lookup.

**PublicSearchController:** Added `UnifiedSearchProcessor` field, new `/unified` POST endpoint.

### Files Summary

**Created:** UnifiedSearchRequest, UnifiedSearchResultItem, UnifiedSearchResponse, UnifiedSearchProcessor
**Modified:** PublicSearchController, UniqueOfferRepository, PublicSearchProcessor, ChatConversation, ChatMessage, ChatConversationDto, ChatMessageDto, SendMessageRequest, ChatService, ChatServiceImpl, ChatController, BusinessChatController, SystemNotifyRequest, SearchDocument, SearchDocumentType, Product, Business, CustomerProfile, AppRole, BusinessMemberRole, BranchMemberRole, V8__restructuring.sql
**Deleted:** 9 BrandDrop files, 2 BrandPageBlock DTOs, ConversationStatus enum, ChatRequestBridge, messaging/ package, BrandExperienceController, BrandPageBlock entity, BusinessCard entity, BrandPageBlockType, StorefrontPageStatus, AuthChallengeChannel.PHONE

### Remaining Work (Phases 5-6) — COMPLETED 2026-07-12

## 2026-07-12 — Backend Restructuring V2: Phases 5-6

### Phase 5: Roles & Permissions

**BusinessMemberService expanded:**
- Added `createMember(businessId, userId, role)` — generalized member creation for any BusinessMemberRole (OWNER, MANAGER, WORKER)
- Added `findByUser(userId)` — find any BusinessMember for a user (not just OWNER)
- Added `findByBusinessAndUser(businessId, userId)` — find member in specific business
- Added `isManagerOrAboveOfBusiness(businessId, userId)` — hierarchy check: OWNER and MANAGER return true
- Added `getRoleInBusiness(businessId, userId)` — return BusinessMemberRole for hierarchy comparison
- `createOwner()` now delegates to `createMember()` with OWNER role
- `BusinessMapper.toBusinessMemberEntity()` now accepts `BusinessMemberRole role` parameter (was hardcoded OWNER)

**BusinessMemberRepository:** Added `findByUserIdAndStatus(userId, status)` (list by user) and `findByBusinessIdAndUserId(businessId, userId)` (single member lookup).

**StaffManagementProcessor — hierarchy + BusinessMember creation:**
- Now creates both `BusinessMember` AND `BranchMember` when creating staff (was only BranchMember)
- `verifyOwnerAccess()` → `verifyManagementAccess()` — now allows MANAGER+OWNER (was OWNER only)
- MANAGER can only create WORKER-level staff; OWNER can create MANAGER and WORKER
- `CreateStaffRequest.role` field added — default "WORKER"
- `UpdateStaffRequest.role` field added — supports role changes
- Role change validates hierarchy: MANAGER can't promote to MANAGER or OWNER

**InviteProcessor — hierarchy + role support:**
- `verifyOwnerAccess()` → `verifyManagementAccess()` — MANAGER can now create invites
- MANAGER can only invite WORKERs; OWNER can invite MANAGERs and WORKERs
- `CreateInviteRequest.role` field added — default "WORKER"
- Invite role resolution: default WORKER, validates against BranchMemberRole enum

**BranchMemberService:** Added `updateMemberRole(memberId, role)` for staff role changes.

**BranchMemberServiceImpl.isStaffOfBranch():** Removed hardcoded `BranchMemberRole.WORKER` check — now accepts all branch member roles (OWNER, MANAGER, WORKER).

**AuthProcessor — actual role resolution:**
- `authorityForSession()`: Now looks up BusinessMember when bizResult is null (non-OWNER staff). Returns `ROLE_BUSINESS_OWNER`, `ROLE_BUSINESS_MANAGER`, or `ROLE_BUSINESS_WORKER` based on actual BusinessMember record. Falls back to `ROLE_BUSINESS_WORKER` for branch-only staff (backward compat).
- `resolveStartRoute()`: OWNER and MANAGER → "OWNER_BRANCHES", WORKER → "BRANCH_WORKSPACE", STAFF → "BRANCH_WORKSPACE"
- Added `BusinessMemberService` dependency

**SecurityConfig:** Added `POST /api/v1/search/unified` to public endpoints (was missing, fell through to authenticated catch-all).

**BusinessProductProcessor + BusinessServiceProcessor:** `requireAnyAccess()` now uses `isManagerOrAboveOfBusiness()` instead of `isOwnerOfBusiness()` — MANAGER can manage products/services for all branches.

**ErrorCode:** Added `BUSINESS_NOT_FOUND`.

### Phase 6: Shipping Settings + Customer Profile

**Shipping settings CRUD:**
- `ShippingController`: `GET` + `PATCH /api/v1/businesses/{businessId}/shipping` — OWNER-only
- `ShippingProcessor`: reads/writes `shippingMode` and `shippingCityIds` (JSONB) on Business entity
- `ShippingResponse` + `UpdateShippingRequest` DTOs

**Customer profile endpoints:**
- `ProfileController`: `GET /api/v1/profile`, `PATCH /api/v1/profile`, `POST /api/v1/profile/icon?iconUrl=...`
- `ProfileProcessor` → `CustomerProfileService` → `CustomerProfileRepository`
- `CustomerProfileResponse` + `UpdateCustomerProfileRequest` DTOs
- Auto-creates `CustomerProfile` on first access (lazy initialization)
- Icon URL update via simple query parameter (multipart deferred to future iteration)

**New files (13):**
DTOs: `ShippingResponse`, `UpdateShippingRequest`, `CustomerProfileResponse`, `UpdateCustomerProfileRequest`
Domain: `CustomerProfileService`, `CustomerProfileServiceImpl`
Repository: `CustomerProfileRepository`
Processors: `ShippingProcessor`, `ProfileProcessor`
Controllers: `ShippingController`, `ProfileController`

**Modified files (18):**
`BusinessMapper`, `BusinessMemberService`, `BusinessMemberServiceImpl`, `BusinessMemberRepository`, `BusinessService`, `BusinessServiceImpl`, `StaffManagementProcessor`, `InviteProcessor`, `BranchMemberService`, `BranchMemberServiceImpl`, `AuthProcessor`, `SecurityConfig`, `CreateStaffRequest`, `CreateInviteRequest`, `UpdateStaffRequest`, `BusinessProductProcessor`, `BusinessServiceProcessor`, `ErrorCode`

### Files Summary (All 6 Phases)

**Created:** UnifiedSearchRequest, UnifiedSearchResultItem, UnifiedSearchResponse, UnifiedSearchProcessor, ShippingResponse, UpdateShippingRequest, CustomerProfileResponse, UpdateCustomerProfileRequest, CustomerProfileService, CustomerProfileServiceImpl, CustomerProfileRepository, ShippingProcessor, ProfileProcessor, ShippingController, ProfileController
**Modified:** PublicSearchController, UniqueOfferRepository, PublicSearchProcessor, ChatConversation, ChatMessage, ChatConversationDto, ChatMessageDto, SendMessageRequest, ChatService, ChatServiceImpl, ChatController, BusinessChatController, SystemNotifyRequest, SearchDocument, SearchDocumentType, Product, Business, CustomerProfile, AppRole, BusinessMemberRole, BranchMemberRole, V8__restructuring.sql, BusinessMapper, BusinessMemberService, BusinessMemberServiceImpl, BusinessMemberRepository, BusinessService, BusinessServiceImpl, StaffManagementProcessor, InviteProcessor, BranchMemberService, BranchMemberServiceImpl, AuthProcessor, SecurityConfig, CreateStaffRequest, CreateInviteRequest, UpdateStaffRequest, BusinessProductProcessor, BusinessServiceProcessor, ErrorCode
**Deleted:** 9 BrandDrop files, 2 BrandPageBlock DTOs, ConversationStatus enum, ChatRequestBridge, messaging/ package, BrandExperienceController, BrandPageBlock entity, BusinessCard entity, BrandPageBlockType, StorefrontPageStatus, AuthChallengeChannel.PHONE

## 2026-07-05 - Business Cabinet Save And Import Fixes

- Business registration/auth payloads stay camelCase on the frontend so the shared API adapter sends the backend's expected snake_case once, avoiding malformed keys such as double-underscored confirmation fields.
- Product and service creation must send a real category UUID from backend categories; frontend forms no longer fall back to `"default"`.
- Added branch-scoped multipart AI Autodump file upload at `POST /api/v1/business-admin/branches/{branchId}/autodump-sessions/files` for `.txt`, `.md`, and `.pdf` files. Existing Excel upload remains at `POST /api/v1/business-admin/branches/{branchId}/product-imports` for `.xlsx`.

## 2026-07-04 — Freshness Audit: Meilisearch, Contact Privacy, Storefront, Drops, Public Ingestion

Deep audit of all MD files against the current Ask product direction. Five new backend task specs created (07-11), AGENTS.md and ARCHITECTURE_NARRATIVE.md updated with search infrastructure, contact privacy architecture, storefront builder, drops-as-search-signals, and public business ingestion pipeline.

### Search Infrastructure (Task 07)

- Documented three-layer search: PostgreSQL (source of truth) → Meilisearch (fast search projection) → AI (query structuring into SearchPlan JSON).
- Meilisearch index `ask_products_services` schema: 20 fields including `_geo` for geo-search, computed `freshnessScore`/`activityLevel`/`hasActiveDrop`.
- Search Orchestrator `POST /api/v1/search/v2`: raw query → AI intent structurer → SearchPlan → Meilisearch query → PostgreSQL hydration → SearchResponse with sections (exact_products, similar_products, fresh_drops, suitable_storefronts, over_budget, needs_confirmation).
- Sync: direct on CRUD for MVP, outbox pattern for production. Full rebuild via `POST /api/v1/admin/search/rebuild-index`.
- SearchResultCard DTO: component type (ProductCard/ServiceCard/DropCard/BusinessCandidateCard), matchReasons (max 4), badges, availability, hasActiveDrop.

### Contact Privacy Architecture (Task 09)

- HMAC-SHA256 for dedup only (mathematically irreversible). AES-256-GCM encrypted vault for contact storage.
- contactActionId pattern: frontend receives one-time/short-lived tokens, backend resolves to redirect/deep-link/display value.
- ContactResolveResponse: actionType (REDIRECT/DISPLAY/DEEP_LINK/CHAT), provider, deepLink/redirectUrl/displayValue.
- BusinessContact extended: contact_hash, encrypted_value, display_value, visibility (PUBLIC/AFTER_CONTACT/INTERNAL).
- BusinessExternalLink entity: provider (TWO_GIS/INSTAGRAM/TELEGRAM/SITE/WHATSAPP), confidence (VERIFIED/LIKELY/UNVERIFIED), visibility.
- Migration plan for existing contacts: compute hash, encrypt value, create external links for social contacts.

### Storefront Builder (Task 10)

- Constrained Canva-like builder (Puck recommended, MIT), NOT free-form Webflow.
- BrandProfile: brandColor, logoUrl, coverUrl, toneOfVoice, description, social links.
- StorefrontBlock types: HERO, PRODUCTS, DROPS, ABOUT, LOOKBOOK, BRANCHES, CONTACTS, FAQ, PROMO, WHY_THIS_MATCHES.
- Each block has blockType, displayOrder, configJson, enabled.
- Draft/published versioning: PUT draft saves, POST publish copies draft → published. Clients see only published.
- Brand color from BrandProfile used as search result card accent. Color/logo changes trigger Meilisearch re-sync.

### Drops As Search Signals (Task 11)

- Drop entity: name, description, type (NEW_COLLECTION/LIMITED_RELEASE/RESTOCK/CAPSULE/SEASONAL/COLLAB/PREORDER), status (UPCOMING/ACTIVE/ENDED/CANCELLED), startDate, endDate, coverUrl, productIds, tags.
- Drops indexed in Meilisearch as type=DROP. Appear in `fresh_drops` search section as DropCard.
- Active drops boost ranking: hasActiveDrop=true → +10% freshnessScore. Query keywords "новый"/"дроп"/"коллекция"/"релиз" → drops ranked higher.
- UPCOMING and ACTIVE indexed; ENDED and CANCELLED removed from index.
- Drop does not require linked products — can be an announcement.

### Public Business Candidate Ingestion (Task 08)

- Discovery pipeline: 2GIS, Instagram, Telegram, public websites → BusinessExternalLink + BusinessCandidate.
- BusinessCandidate: PENDING_REVIEW → admin approve → Business created. Not visible in client search until approved.
- Admin endpoints: ingest signals, list candidates, approve/reject.
- Privacy rule: only public business signals, no scraping of private profiles or full history.

### AGENTS.md And Architecture Docs Updated

- Backend AGENTS.md: added Search Infrastructure, SearchPlan JSON Contract, Contact Privacy sections.
- ARCHITECTURE_NARRATIVE.md: added Search Infrastructure (three-layer diagram, Meilisearch schema, SearchPlan flow) and Contact Privacy And Actions (HMAC/vault architecture, BusinessExternalLink entity table).
- Visual style direction corrected: dark graphite (#070807), warm ivory (#f4eee6), orange accent (#ff5a1f). Old teal (#0d9b7c) references removed.
- Task README updated: tasks 07-11 added with status "Спецификация".

### Files Created (5)

`07_meilisearch_search_engine_integration.md`, `08_public_business_candidate_ingestion.md`, `09_contact_action_privacy_and_redirects.md`, `10_brand_storefront_builder_backend.md`, `11_drops_events_search_indexing.md`.

### Files Deleted (7)

`anti_marketplace_gap_analysis.md`, `ASK_FRONTEND_REDESIGN_REWORK_PROMPT.md` (absorbed into reference stack), `AskFrontend/ARCHITECTURE_DOCUMENTATION.md`, `AI_AUTODUMP_IMPORT_ARCHITECTURE.md` (has _ACTUALIZED version), `AskMvp/` (separate prototype), `ASK_PROJECT_KNOWLEDGE_DUMP_2026-06-29.md`, `.playwright-mcp/figma-gap-analysis.md`.

## 2026-07-01 - Anti-Marketplace Brand And Decision Card Contract

- Search result cards now carry a brand layer and a decision layer instead of marketplace ranking proof.
- Added brand profile, storefront block, and brand drop foundations for business-owned presentation.
- Supplier responses now carry source type so auto reply, staff reply, business confirmation, data update, and supplier check confirmation can stay separate.
- Default public search intent remains `intent_match`; cheapest-first, rating-first, and public hidden-score ranking are not default Ask behavior.

## 2026-06-30 - DeepSeek Structured Search And AI Autodump Architecture

- Added backend AI intent structuring endpoints for local structured search: `POST /api/v1/search/intent-structure` and `POST /api/v1/search`.
- Structured search now uses a provider-backed intent structurer before public search and then searches using scope, semantic query, keywords, synonyms, must-have terms, and fallback expansion terms.
- Reworked structured search so AI-inferred category is mapped to a `SearchPlan` with canonical aliases and Java scoring instead of becoming a SQL category hard filter.
- Added Search V2 semantic hard gates, budget parsing, city/budget fallback warnings, result sections, match reasons, and per-card score/confidence. Structured search must not return the whole active catalog just because category, supplier, or price loosely matches.
- Added `V5__search_v2_semantic_tags.sql` to enrich already-seeded local/staging search documents with concrete semantic tags such as smartphone, laptop, vacuum, manicure, haircut, gender qualifier, and feature phrase tags.
- Added reusable search term enrichment for sports nutrition imports so queries like `спортпит` and `креатин` map to concrete indexed product tokens instead of relying on literal Excel/category wording only.
- Added `V7__search_v2_sports_nutrition_tokens.sql` to backfill sports nutrition tokens and query aliases into existing local search documents.
- Added Search V2 package-weight constraints so requests such as `батончик > 900 грамм` compare indexed package sizes like `2 кг` as grams instead of requiring the literal text `900 грамм`.
- Added bike-rental term enrichment and `V8__general_service_category_and_bike_tokens.sql` so `велики`, `велосипед`, and `прокат велосипедов` can find service documents created from AI Autodump.
- Added a root `Общее` category and changed AI service import category fallback to use it when the AI category does not match an existing active category, instead of defaulting unknown services to beauty or repair.
- AI Autodump service publish now maps duration and schedule text from drafts into `ServiceBranchOffer` and search summaries, preserving inputs like `1 час` for rental/service cards.
- Search document token enrichment now drops index tokens longer than the `search_document_token.token` 255-character storage limit so large CSV rows or descriptions cannot break publish.
- Configured the AI search client for DeepSeek Chat Completions through `DEEPSEEK_API_KEY`, `DEEPSEEK_BASE_URL`, `DEEPSEEK_SEARCH_MODEL`, and `DEEPSEEK_SEARCH_MAX_TOKENS`.
- Kept legacy `GET /api/v1/search` available for literal search compatibility.
- Added `AI_AUTODUMP_IMPORT_ARCHITECTURE.md` as the real backend/database direction for messy data import: raw dump -> import session -> AI job -> draft product/service cards -> business preview/edit -> approve -> publish into searchable catalog.
- Implemented AI Autodump publish so approved drafts create real product/service branch offers and sync `search_document` instead of only marking drafts as published.
- Updated backend ERD and UX contract with the boundary that AI creates drafts or internal search context only; it must not publish live records or invent availability truth.

## 2026-06-27 - Public Search Alias Expansion

- Added `search_query_alias` as a data-owned search expansion table for broad customer wording such as `барбершоп` -> `стрижка`.
- Public search now performs additional deduplicated lookup passes for active aliases while keeping product/service scope filtering.
- Added staging catalog seed data for `Умные часы Apple Watch SE 44mm` so the simple `Часы` showcase query has a real catalog result.
- Updated `PRODUCT_SERVICE_FOUNDATION_ERD.md` with the alias table and alias expansion boundary.

## 2026-06-23 - T11 Business Product Endpoints Actualization

Rebased `feature/T11-product-endpoints` onto current `dev` and fully actualized the implementation to match the latest `03_business_admin_product_endpoints.md` specification. Removed all obsolete role/permission logic inherited from the pre-simplification workflow.

### Rebase

- Cherry-picked the single T11 commit (`impl logic`) onto `dev` HEAD (`fcb82e1`).
- Resolved 4 merge conflicts: `ProductOfferRepository.java` (add/add), `ProductRepository.java` (add/add), `SecurityConfig.java` (whitespace), `ErrorCode.java` (enum member merge).

### Role/Permission Actualization

- **Removed `requireManagerOrAbove()`** from `BusinessProductProcessor`. The `BranchMemberRole` enum now contains only `STAFF` (no `MANAGER`/`OPERATOR`), making the old role-gate a compilation error and a behavioral mismatch with the current MD spec.
- **Unified all product operations under `requireAnyAccess()`**: Owner of the business OR any staff assigned to the branch can now list, create, update, and delete products. This matches `03_business_admin_product_endpoints.md` line 23-24: "Both Owner and Staff assigned to that branch can manage products" and "There is no Manager/Operator permission split for product management."
- **Removed the `enabled`-only toggle shortcut**: Previously, Operators (non-Manager staff) could only toggle `enabled` on existing products. Since the role split no longer exists, all staff can perform all product mutations.
- Replaced `businessService.findBranchById()` → `businessBranchService.findById()` (returns `BusinessBranchDto`).
- Replaced `businessService.findBranchMembers()` → `branchMemberService.findByBranch()` (returns `List<BranchMemberDto>`).

### API Contract Changes

- **Controller now returns `ResponseEntity<T>`** (per `CODE_RULES.md` line 102: "Every controller method must return `ResponseEntity<T>` from `org.springframework.http.ResponseEntity`"). This changes status delivery from `@ResponseStatus` to explicit `ResponseEntity.status(HttpStatus.CREATED).body(...)` for 201 and `ResponseEntity.ok(...)` for 200. The JSON body shape is unchanged (same DTO), but HTTP semantics are now explicit.
- **Removed `@ResponseStatus`**: Status now comes from `ResponseEntity`, per CODE_RULES line 106.
- **Removed `status` field from `BusinessProductRowResponse`**: The response DTO now contains exactly the 11 fields listed in the MD spec (productId, productOfferId, branchId, categoryId, name, description, sku, tags, price, enabled, updatedAt). Internal status is still tracked via `ProductOfferDto.status` but not exposed in API responses.
- **Removed `status` field from `BusinessProductRowResponse`**: The response DTO now contains exactly the 11 fields listed in the MD spec (productId, productOfferId, branchId, categoryId, name, description, sku, tags, price, enabled, updatedAt). Internal status is still tracked via `ProductOfferDto.status` but not exposed in API responses.
- **Removed `hasOnlyEnabledField()` from `BusinessProductUpdateRequest`**: This method was only used by the now-deleted role-splitting logic.

### DTO And Internal Transfer Changes

- **`ProductOfferDto`**: Added `businessId` (UUID) and `categoryLabel` (String) fields to carry richer data from the service layer to the processor and search service.
- **`ProductOfferMapper.toDto()`**: Now populates `businessId` from `product.getBusiness().getId()` and `categoryLabel` from `product.getCategory().getName()`, with null-safe accessors for nullable Category.
- **`toRowResponse()`**: No longer maps `status` — only the 11 MD-specified fields.

### Search Document Sync Expansion

- **`SearchDocumentService.syncProductDocument()`**: Signature expanded from 5 parameters to 10 — now accepts `businessId`, `branchId`, `categoryLabel`, `sku`, and `price` in addition to the existing `productOfferId`, `title`, `summary`, `tags`, and `live`. Matches MD spec line 89: "Search document строится по названию, описанию, тегам, категории, бизнесу, филиалу и цене."
- **`SearchDocumentServiceImpl.syncProductDocument()`**: Now fully populates the `SearchDocument` entity — sets `business`, `branch`, `categoryLabel`, `sku`, and `price` using `getReferenceById()` for foreign entities.
- Added `BusinessRepository` and `BusinessBranchRepository` to the impl (used only via `getReferenceById()`, compliant with CODE_RULES).

### Obsolete Code Removed

- `BusinessProductProcessor.requireManagerOrAbove()` — entire method deleted.
- `BusinessProductProcessor.resolveBranchRole()` — entire method deleted.
- `BusinessProductUpdateRequest.hasOnlyEnabledField()` — method and `@JsonIgnore` import deleted.
- Imports for `BranchMember` (entity) and `BranchMemberRole` (enum) removed from Processor.

### Files Changed (8)

`BusinessProductController.java`, `BusinessProductProcessor.java`, `BusinessProductRowResponse.java`, `BusinessProductUpdateRequest.java`, `ProductOfferDto.java`, `ProductOfferMapper.java`, `SearchDocumentService.java`, `SearchDocumentServiceImpl.java`.

## 2026-06-22 - Product Excel Import Implementation (Task 06)

Full production backend implementation for Product Excel Import. Backend now owns Excel parsing (fastexcel-reader), auto-mapping, row normalization, validation, and product/offer/search-document creation. Frontend prototype branch (`feature/product-excel-import`) is preserved as UX reference only.

### Endpoints (5 REST)

Base path: `/api/v1/business-admin/branches/{branchId}/product-imports`

| Method | Path | Purpose |
|--------|------|---------|
| `POST` | `/product-imports` | Upload .xlsx, parse, auto-map, save raw rows → `MAPPING_REQUIRED` |
| `POST` | `/product-imports/{importId}/mapping` | Save user mapping, normalize rows, return preview → `PREVIEW_READY` |
| `GET` | `/product-imports/{importId}/preview` | Get current preview state |
| `POST` | `/product-imports/{importId}/approve` | Create Product + ProductOffer + SearchDocument → `IMPORTED` |
| `POST` | `/product-imports/{importId}/cancel` | Cancel import → `CANCELLED` |

### Architecture

Standard layer chain: `CatalogImportController` → `ProductImportProcessor` (@Transactional) → `ProductImportServiceImpl` / `CatalogImportServiceImpl` → repositories. Access control via `isOwnerOrStaffOfBranch` (both Owner and Staff can import).

### New Components

- **ExcelParser** — fastexcel streaming parser, reads first sheet, header row → columns, data rows → List<Map<String,String>>
- **AutoMappingEngine** — 30+ Russian/English patterns per TargetField, forced IGNORE for stock/quantity/warehouse/availability columns, confidence scoring
- **RowNormalizer** — deserializes raw row JSON, applies mappings, validates (NAME required → INVALID, PRICE parse → WARNING), produces normalized JSON + error/warning lists
- **CatalogImportService** (interface + impl) — CRUD for CatalogImport, CatalogImportColumnMapping, RawCatalogRow
- **ProductImportService** (interface + impl) — orchestration: parseAndStore, applyMappings, buildPreview, approveImport, cancelImport
- **CatalogImportMapper** — hand-written entity ↔ DTO mapping with Jackson ObjectMapper

### Data Model Changes (V2__product_import.sql)

- **Product**: added `category_label`, `characteristics_json` (TEXT); made `category_id` nullable
- **CatalogImport**: added `business_id`, `branch_id`, `created_by` FKs; `total_rows`, `valid_rows`, `invalid_rows`, `warning_rows` counters
- **CatalogImportColumnMapping**: added `characteristic_name`, `approved` (boolean), `confidence` (double)
- **RawCatalogRow**: added `normalized_data_json`, `validation_errors_json`, `validation_warnings_json` (TEXT); `status` (RawRowStatus enum)
- **SearchDocument**: added `category_label`, `sku`, `characteristics_json` (TEXT); `business_id`, `branch_id` FKs; `price` (numeric)

### New Enums

- **TargetField**: NAME, CATEGORY_LABEL, DESCRIPTION, SKU, PRICE, TAGS, IGNORE, APPEND_TO_DESCRIPTION, CHARACTERISTIC
- **RawRowStatus**: PENDING, VALID, WARNING, INVALID
- **CatalogImportStatus** updated: UPLOADED, MAPPING_REQUIRED, PREVIEW_READY, IMPORTED, FAILED, CANCELLED

### Files Created (26)

Migration (1), enums (2), domain services (6), DTOs (5), repositories (7), mapper (1), processor (1), controller (1), service interfaces (2).

### Files Modified (9)

Entities (5): Product, CatalogImport, CatalogImportColumnMapping, RawCatalogRow, SearchDocument. Enums (1): CatalogImportStatus. Error codes (1): +7 import-specific entries. Business service (1): +resolveDataSource. Build (1): +fastexcel-reader dependency.

### Documentation

- Rewrote `06_product_excel_import_endpoints.md` to match implemented architecture with real DTOs, statuses, flow, and rules.
- Updated frontend `FRONTEND_BACKEND_CONTRACT.md` to reflect backend-owned import, real endpoint URLs, and real DTO shapes.

## 2026-06-22 - Role Simplification: Owner/Staff Only

Removed MANAGER and OPERATOR roles from all backend contracts and task files. Business roles are now only OWNER and STAFF.

- `UX_UI_BACKEND_CONTRACT.md`: Entry point model updated (Staff replaces Manager/Operator). Added explicit prohibition paragraph.
- `AUTH_BACKEND_CONTRACT.md`: Authority table updated to ROLE_BUSINESS_OWNER and ROLE_BUSINESS_STAFF only. startRoute updated to OWNER_BRANCHES/BRANCH_WORKSPACE.
- `FRONTEND_API_TASKS.md`: All MANAGER/OPERATOR authority references replaced with STAFF. AuthBusinessContextResponse memberRole now OWNER or STAFF. Start routes updated.
- `PRODUCT_SERVICE_FOUNDATION_ERD.md`: branch_member role column narrowed to STAFF only. branch_invite role always STAFF.
- `03_business_admin_product_endpoints.md`: Access rules updated to Owner+Staff, no Manager/Operator split.
- `04_business_admin_service_endpoints.md`: Access rules updated to Owner+Staff, no Manager/Operator split.
- Business authorities: only ROLE_BUSINESS_OWNER and ROLE_BUSINESS_STAFF. ROLE_BUSINESS_MANAGER and ROLE_BUSINESS_OPERATOR removed.

## 2026-06-22 - Automatic Supplier Check Contract

Updated backend contracts and search task direction:

- Submitted search sessions now have locked scope: `PRODUCT` or `SERVICE`.
- Product search can automatically create supplier check/request linked to `searchSessionId`.
- Auto supplier check selects business/branch targets using city, category, tags, branch profile, and similar product evidence.
- Auto supplier check is not standalone business search.
- Auto supplier check is business-facing as Activity/request item and customer-facing as `Подходящие магазины`.
- Sending auto supplier check must not create a customer-visible outgoing chat message or customer unread notification.
- Customer `Чаты` tab appears only after real chat interaction.

## 2026-06-21 - Staff Management Implementation

Added branch-level staff management with temporary password activation, invite codes, and granular authority.

### Membership Model

- `business_member` now represents business-level OWNER role only.
- New `branch_member` table for branch-level MANAGER and OPERATOR roles.
- New `branch_invite` table for invite code-based staff onboarding.
- Authority stored as `String authority` in `auth_session` (replaces `AppRole role` enum): ROLE_BUSINESS_OWNER, ROLE_BUSINESS_MANAGER, ROLE_BUSINESS_OPERATOR, ROLE_CUSTOMER.

### Staff Activation Flow

- Owner creates staff via `POST /staff` — system generates temporary BCrypt-hashed password.
- Temp password plain text is AES-encrypted for owner visibility until activation.
- Staff logs in via unified `POST /auth/login` — system returns `activationRequired: true`.
- Staff changes password via `POST /auth/change-temporary-password` — account becomes ACTIVE.
- Staff statuses: PENDING_ACTIVATION, ACTIVE, PASSWORD_RESET_REQUIRED, DISABLED.

### Schema Changes

- `auth_session`: added `authority VARCHAR(50)`, `activation_required BOOLEAN DEFAULT FALSE`; dropped `role`.
- `app_user`: added `must_change_password`, `temp_password_encrypted`, `activated_at`.
- Created `branch_member` table with UNIQUE(branch_id, user_id).
- Created `branch_invite` table with UNIQUE code and expiry fields.
- Indexes on branch_member branch/user, branch_invite branch/code, app_user role.

### Service Naming Convention

- `IdentityDomainService` → `IdentityService` (interface) + `IdentityServiceImpl`.
- `BusinessDomainService` → `BusinessService` (interface) + `BusinessServiceImpl`.

### Endpoints Added

- `POST /api/v1/auth/login` — unified password login for all roles.
- `POST /api/v1/auth/change-temporary-password` — first-login password change.
- `POST /api/v1/businesses/{bId}/branches/{brId}/staff` — create staff.
- `GET /api/v1/businesses/{bId}/branches/{brId}/staff` — list staff.
- `POST /api/v1/businesses/{bId}/branches/{brId}/staff/{id}/update` — update staff (role, disable).
- `POST /api/v1/businesses/{bId}/branches/{brId}/staff/{id}/reset-password` — reset staff password.
- `POST /api/v1/businesses/{bId}/branches/{brId}/invites` — create invite.
- `GET /api/v1/businesses/{bId}/branches/{brId}/invites` — list invites.
- `DELETE /api/v1/businesses/{bId}/branches/{brId}/invites/{id}` — revoke invite.

## 2026-06-21 - Data Model Normalization After Task 05

Synchronized the implemented entities, `V1__init.sql`, and first-read documentation with the current frontend-backed UX direction.

### Removed Old Model Fields

- Removed product stock tracking from MVP schema and entities: no `stockStatus`, `stockQuantity`, `stock_status`, or `stock_quantity`.
- Removed availability scoring from MVP schema and entities: no `AvailabilityConfidence`, `availabilityConfidence`, or `availability_confidence`.
- Removed freshness tracking from MVP schema and entities: no `freshnessAt` or `freshness_at`.
- Removed standalone business search indexing fields from search documents and counters from snapshots.
- Removed service `ConfirmationPolicy`; MVP services are request-to-book and business-confirmed, not automatic booking truth.

### Current Implemented Model

- `ProductOffer.enabled` controls whether a branch-level product offer appears in live product search.
- `ServiceBranchOffer.active` controls whether a branch-level service offer appears in live service search.
- `ServiceBranchOffer.scheduleText` stores display schedule/conditions text only.
- `SearchDocument` indexes product offers and service branch offers only.
- `SearchResultSnapshot.business` and `SearchResultSnapshot.branch` are context for product/service rows, not standalone business results.
- `BusinessContact` now uses `contactValue` and `primaryContact`, mapped to `contact_value` and `is_primary`.
- `SupplierResponseStatus` now uses product/service-specific values: `HAS_ITEM`, `NO_ITEM`, `NEED_CLARIFICATION`, `HAS_ANALOG`, `CAN_PROVIDE`, `CANNOT_PROVIDE`, `SUGGEST_OTHER_TIME`.

### Documentation Updated

- Updated `README.md` to remove old "business search and availability layer" wording and replace it with product/service search wording.
- Updated `ARCHITECTURE_NARRATIVE.md` with the current data model alignment and explicit removed-field list.
- Updated `PRODUCT_SERVICE_FOUNDATION_ERD.md` with `enabled`, `active`, `schedule_text`, `contact_value`, `is_primary`, and current search document shape.
- Updated `UX_UI_BACKEND_CONTRACT.md` to make the frontend flow the source to refresh from before backend task/entity changes.
- Updated `AUTH_BACKEND_CONTRACT.md` to state that email is the real MVP verification channel and SMS is disabled until a real provider is connected.

## 2026-06-21 - Identity Auth Implementation (Task 05)

Implemented identity authentication and business branch onboarding endpoints for Ask: customer login/register, business login/register, code verification, current session, and logout.

### Auth Model
- Email-or-phone login/registration: exactly one primary identifier is used for the auth challenge.
- Verification codes are 6 digits and stored hashed in the database.
- Passwords are stored through Spring Security `PasswordEncoder` with BCrypt.
- Bearer session tokens are random opaque tokens; only token hashes are stored.
- Remember-me extends session TTL through backend config.
- Business registration data is persisted only after successful contact verification.

### Business Onboarding
- Business registration creates `Business`, `BusinessBranch`, `BusinessMember`, and initial `BusinessContact` records.
- Registration contact becomes the first public branch contact.
- Online-only branches may omit physical address fields.

### Email and SMS
- Email verification is the real MVP channel and uses `SmtpEmailCodeSender` with JavaMailSender.
- Email delivery failure stops the auth challenge instead of pretending success.
- SMS interfaces exist for future provider integration, but SMS is disabled by default and current logging/noop implementations return delivery-unavailable errors.

### Security And Configuration
- `SecurityConfig` uses stateless sessions and permits only auth start/register/verify endpoints without authentication.
- `JwtAuthFilter` validates Bearer tokens through `IdentityDomainService.findSessionByToken`.
- Auth settings live under `auth.*`; SMTP settings use `spring.mail.*` and environment overrides.

### Schema
- `V1__init.sql` creates the initial schema for identity, business, catalog, service, request, messaging, booking, and search domains.
- The schema is production-facing for persistent business onboarding data, not throwaway mock data.

## 2026-06-21 - Auth, UX Contract, And Backend Task Actualization

Added `AI_Knowledge/client_contracts/AUTH_BACKEND_CONTRACT.md` as the backend-facing source for customer and business auth: email-or-phone login and registration, real email verification, optional SMS verification, 6-digit confirmation, remember-me sessions, current session restore, logout, and branch/store onboarding.

Synchronized `UX_UI_BACKEND_CONTRACT.md`, architecture narrative, ERD, and Tasks 00-05 with the current frontend expected UX: product/service search only, no standalone business search, no `availabilityConfidence`, no `stockQuantity`, no `freshnessAt`, no `chatAvailable`, distance calculated only from customer coordinates to branch coordinates, and production-facing business onboarding with persistent branch, product, and service data.

## 2026-06-19 - Search Session Snapshot Foundation

Added Task 00 implementation for search history foundation: `SearchSession`, `SearchSnapshot`, `SearchResultSnapshot`, and related search enums. Updated the product/service ERD so customer history reopens fixed saved result state instead of pretending live search documents still represent the original result set.

## 2026-06-19 - Documentation Structure Cleanup

Moved first-session guidance into `AI_Knowledge/first_steps`, moved large product/service architecture into `AI_Knowledge/data_architecture`, and added `AI_Knowledge/client_contracts/UX_UI_BACKEND_CONTRACT.md` as the backend-facing extraction from the UX/UI flow.

Separated agent workflow from backend code rules: `AGENTS.md` is now the short agent entrypoint and task router, while `AI_Knowledge/CODE_RULES.md` remains the code architecture rule file.

Moved Codex plugin and MCP expectations into `codex/CODEX_INFRASTRUCTURE.md` and removed project-local MCP, plugin, old skill, audit, origin, deprecated web-staging, and archive notes that were not useful for new backend programmers.

## 2026-06-18 - Project Verification Scope Correction

Updated project guidance to stop requiring or creating tests for AskBackend foundation work unless the project rule is explicitly reversed. Verification language now points to compile, migration, OpenAPI contract, review, provider-contract, and no-secret checks.

## 2026-06-18 - Product And Service ERD Strategy

Added `PRODUCT_SERVICE_FOUNDATION_ERD.md` with the MVP entity strategy and ERD. The product model now treats each concrete sellable variation as a `product` with tags for search, defers variant/attribute tables, adds `booking` for confirmed service lifecycle, uses universal `conversation` tables for messaging, and keeps `service_resource` as an optional abstract capacity resource rather than a specialist account.

## 2026-06-18 - Initial Backend Architecture And Entities

Initialized the Spring Boot Maven project skeleton, root `AGENTS.md`, `CODE_RULES.md`, feature package folders, UUIDv7 base entity, Lombok-backed JPA entity classes, and enum foundations. Only entities and architecture scaffolding were added; no controllers, processors, services, repositories, DTOs, migrations, or tests were created.

## 2026-06-18 - Search-First Strategy Actualization

Updated the product strategy:

- Ask is now described as local search for products and services across city businesses.
- Customers should see known products/services first when Ask has data.
- If a product or service exists in the database, Ask should show where it is available or which business provides it.
- ProductRequest/request routing remains as fallback for missing product/service results or customer-requested business confirmation.
- Catalog, service data, search indexing, Excel/CSV import, and persisted business-entered data are core product architecture, not distant optional additions.
- Data-truth rules still apply: do not invent stock, slots, logistics, delivery, or guaranteed availability without supplier or integration data.

## 2026-06-17 - Goal Actualization

Updated the product architecture idea:

- AskBackend is one backend for Android, iOS, and any future website.
- AskBackend should not copy frontend FSD literally; it should use package-by-feature, modular monolith/domain modules, and clean/hexagonal boundaries.
- Client implementations should use a shared design-independent API/client abstraction instead of duplicating heavy business logic per platform.
- Product catalogs should support Excel and CSV import workflows so sellers do not recreate existing data manually.
- Service-provider management should lean toward a web cabinet for larger service datasets, schedules, free windows, discounts, conditions, specialists, and branches.
- Mobile application direction is customer side plus seller/supplier side; website direction is primarily service-provider administration.

## 2026-06-17 - Restored Portable Foundation

Restored the foundation document set after an accidental over-revert.

### Created Or Restored

- `README.md`
- `ARCHITECTURE_NARRATIVE.md`
- `FIRST_READ_THIS.md`
- `IMPLEMENTATION_PIPELINE.md`
- `AGENTS.md`
- `SELF_AWARE_ORIGIN.md`
- `FOUNDATION_AUDIT.md`
- `DEPRECATED_WEB_STAGING_NOTES.md`
- `CHANGELOG_FOUNDATION.md`
- `.gitignore`
- `archive/README.md`
- `mcp/README.md`
- `plugins/README.md`
- `skills/README.md`
- `skills/backend-spring-ask.md`
- `skills/frontend-ask.md`
- `skills/architecture-system-analysis.md`
- `skills/catalog-integration.md`
- `skills/services-search.md`
- `skills/ai-workflow-consistency.md`
- `skills/mcp-dashboard-usage.md`

### Preserved

- Product vision.
- Manual request fallback direction.
- Catalog/import/search direction.
- Services/scheduling analysis direction.
- Backend architecture rules.
- AI workflow consistency.
- MCP/dashboard guidance.
- Old browser-prototype lessons as archive-only context.

### Adjusted

- Removed the idea that future users or Codex agents need access to any old local project path.
- Reframed origin as local foundation knowledge for the new project.
- Kept the required docs and skills from the initial prompt.

### Excluded

- Old application business code.
- Local Codex configs and secrets.
- Generated runtime state.
- Machine-specific setup.
- Web-staging-only mechanics as active backend requirements.

## 2026-06-25 — Session: Public API endpoints, Security fixes, CORS, Snake-case sync

### Context

User reported 403 Forbidden on `/api/v1/auth/profile` and all edit/save endpoints. Investigation revealed a cascade of issues: missing public endpoints in security config, missing PATCH in CORS, and critically — snake_case/camelCase mismatch between backend and frontend preventing JWT tokens from being stored.

### Root Cause: Jackson snake_case + Frontend camelCase = Token never stored

- Backend `application.yml` has `jackson.property-naming-strategy: SNAKE_CASE` — all JSON keys are snake_case (`access_token`, `display_name`, `business_id`).
- Frontend TypeScript types use camelCase (`accessToken`, `displayName`, `businessId`).
- `persistSession(session)` called `setStoredToken(session.accessToken)` — but `session.accessToken` was `undefined` because the JSON had `access_token`.
- `setStoredToken(undefined)` triggered `localStorage.removeItem()` — token NEVER persisted.
- All authenticated API requests sent no `Authorization` header → anonymous → Spring Security returned 403.
- **Fix (frontend):** Added `transformKeys()` in `httpClient.ts` that recursively converts snake_case → camelCase on every `apiRequest()` response. Direct `fetch()` calls in App.tsx also apply `transformKeys()`.
- **This is a synchronized contract:** Both sides must stay stable. Changing Jackson naming in backend will break frontend. Changing frontend property names will break the app.

### Security Config Changes

- Added `PATCH` to CORS allowed methods (was missing — PATCH requests like branch updates would fail preflight).
- Added public GET endpoints: `/api/v1/cities`, `/api/v1/categories`, `/api/v1/categories/*/subcategories` — these are public reference data, users need them even before auth (city dropdown on registration form).

### New Backend Endpoints

| Endpoint | Method | Auth | Purpose |
|---|---|---|---|
| `/api/v1/cities` | GET | No | List all ACTIVE cities |
| `/api/v1/categories` | GET | No | List root categories |
| `/api/v1/categories/{parentId}/subcategories` | GET | No | List subcategories |
| `/api/v1/auth/profile` | POST | Yes | Update displayName/email/phone |
| `/api/v1/businesses/{businessId}/branches` | GET | Yes | List branches for business |
| `/api/v1/businesses/{businessId}/branches` | POST | Yes | Create branch |
| `/api/v1/businesses/{businessId}/branches/{branchId}` | PATCH | Yes | Update branch |

### New/Modified Files

**Created:**
- `CategoryController.java` — GET endpoints for category listing
- `BranchController.java` — CRUD endpoints for branches
- `BranchManagementProcessor.java` — Owner-access-gated branch orchestration
- `UpdateProfileRequest.java` — DTO with displayName, email, phone

**Modified:**
- `SecurityConfig.java` — Added PATCH to CORS, added public GET endpoints
- `CorsConfig.java` — Added PATCH to allowed methods (both beans)
- `CategoryService.java` / `CategoryServiceImpl.java` — Added listRootCategories(), listSubcategories()
- `CategoryRepository.java` — Added findByParentIsNullAndStatus(), findByParentIdAndStatus()
- `CityService.java` / `CityServiceImpl.java` — Added listAll()
- `CityController.java` — Added GET /api/v1/cities list endpoint
- `BusinessBranchService.java` / `BusinessBranchServiceImpl.java` — Added listByBusiness(), update()
- `BusinessBranchDto.java` — Added cityId, cityName, address, onlineOnly, status fields
- `BusinessMapper.java` — Updated toBusinessBranchDto() with city info and all fields
- `IdentityService.java` / `IdentityServiceImpl.java` — Added updateProfile()
- `AuthProcessor.java` — Added updateProfile() method
- `AuthController.java` — Added POST /api/v1/auth/profile endpoint

### How to Test

```bash
# Start backend
cd AskBackend && mvn spring-boot:run -Dspring-boot.run.profiles=local

# Register a customer
curl -X POST http://localhost:9090/api/v1/auth/customer/register \
  -H "Content-Type: application/json" \
  -d '{"display_name":"Test","email":"test@test.com","password":"test1234","password_confirmation":"test1234","accepted_user_agreement":true,"remember_me":true}'

# Get verification code from stdout (LoggingEmailCodeSender logs it)
# Then verify
curl -X POST http://localhost:9090/api/v1/auth/verify \
  -H "Content-Type: application/json" \
  -d '{"auth_challenge_id":"<id>","code":"<6-digit-code>"}'

# Use the access_token from response to test profile update
curl -X POST http://localhost:9090/api/v1/auth/profile \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"display_name":"New Name"}'
```

### Pending Backend Work
- Country entity, table, V4 migration with seed data
- City entity: add ManyToOne to Country
- CountryController: GET /api/v1/countries
- CityController: optional countryId filter on list
- Rebuild backend JAR after all changes

