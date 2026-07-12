# AskBackend Agent Rules

These are the nearest project instructions for AskBackend. Read this file before changing code or project structure.

## First Session

If this is the first Codex session in this repository, read:

1. `AI_Knowledge/first_steps/FIRST_READ_THIS.md`
2. `codex/CODEX_INFRASTRUCTURE.md`
3. `AI_Knowledge/CODE_RULES.md`

For later sessions, read only the documents relevant to the task, plus any file you are about to edit.

## Agent Workflow

- Search before creating files, endpoints, DTOs, services, configs, or docs.
- Read `AI_Knowledge/CODE_RULES.md` before backend code changes.
- Read `AI_Knowledge/data_architecture/PRODUCT_SERVICE_FOUNDATION_ERD.md` before changing entities, migrations, repositories, catalog, services, booking, messaging, search, or fallback request behavior.
- Read `AI_Knowledge/client_contracts/UX_UI_BACKEND_CONTRACT.md` before changing request/response statuses, customer request flow, supplier response flow, chat, contact actions, or API DTOs used by clients.
- If changing entities, migrations, DTO contracts, auth flow, search flow, product/service visibility, request statuses, or onboarding rules, update `AI_Knowledge/CHANGELOG_FOUNDATION.md` and the matching architecture/client-contract docs in the same turn.
- If backend behavior depends on UX and docs disagree, refresh backend docs from `AskFrontend/AI_Knowledge/product_ux/EXPECTED_UX_UI_FLOW.md` before generating or changing backend tasks.
- Keep changes scoped to the current task.
- Do not run `git commit` or `git push` unless the user explicitly asks in the current turn.
- Do not create or write tests unless the user explicitly reverses this project rule.
- Do not run Maven commands unless the user explicitly asks.
- Do not copy secrets, local Codex configs, auth files, sqlite state, generated caches, plugin caches, runtime binaries, or machine-specific paths into this repository.

## Task Routing

- Use simple shell and file review for local docs or code inspection.
- Use current documentation lookup only when library, SDK, CLI, framework, cloud, or provider behavior may have changed.
- Use browser or Playwright only for visible frontend behavior.
- Use Render, Supabase, GitHub, OpenAI, and other provider tools only when authenticated and in scope.
- Use Dashboard or lifecycle tooling only for substantial starts, architectural pivots, and completion records.
- If a likely Codex tool is not visible, use `tool_search` before assuming it is unavailable.

## Search Infrastructure

- **PostgreSQL = source of truth and search engine**: businesses, branches, products, services, contacts, UniqueOffers, raw imports, approvals. Search runs directly against PostgreSQL via `SearchDocument` table with in-memory scoring.
- **DeepSeek AI = query structuring helper**: receives raw user query (any language, slang, typos), returns `AiStructuredQuery` JSON with searchTerms, categoryHints, brands, priceRange, intent. AI never selects businesses or invents availability.
- **UnifiedSearchProcessor**: single search endpoint (`POST /api/v1/search/unified`) queries PRODUCT + SERVICE documents together, scores in-memory, applies UniqueOffer boosting. No separate scope toggle — AI determines intent.
- **UniqueOffer boosting**: active UniqueOffers linked to a product/service's business add +25 score. DISCOUNT offers compute effective price (percent or amount). Non-DISCOUNT offers display the offer name as label.
- **Scoring**: TITLE_MATCH(30) + BODY_MATCH(15) + TOKEN_MATCH(20) + CATEGORY_MATCH(10) + BRAND_MATCH(10) + OFFER_BOOST(25) - PRICE_OVER_BUDGET(40). Results sorted by score descending.
- Fallback when API key is unset: simple text matching without AI structuring.
- Meilisearch integration is deferred (PostgreSQL-only search is sufficient for MVP). If added later, it would be a rebuildable projection, never the authority.

## AiStructuredQuery Contract

DeepSeek AI returns structured query intent, not final results:

```json
{
  "intent": "goods",
  "searchTerms": ["джинсы", "levis", "винтаж"],
  "categoryHints": ["Одежда", "Секонд-хенд"],
  "priceRange": { "min": null, "max": 40000 },
  "brands": ["Levi's"],
  "originalQuery": "винтажные levi's джинсы 90s рядом"
}
```

Backend validates and uses for in-memory scoring — no external search engine dependency.

## Contact Privacy

- `contact_hash` / HMAC — for dedupe and safe matching only. Hash is never reversible.
- Encrypted contact value or public URL — for actual contact resolution.
- Frontend receives `contactActionId`, not raw phone/username.
- Backend resolves `contactActionId` → redirect/deep-link or returns safe display value.
- `BusinessExternalLink`: provider=`2GIS/INSTAGRAM/TELEGRAM/SITE/WHATSAPP`, publicUrl/deepLink, source, confidence, visibility.

## Product Guardrails

- AskBackend is one backend for Android, iOS, and future web clients.
- Ask is search-first: return known products and services from businesses before creating fallback requests.
- Fallback requests exist only when product/service results are missing or the customer wants business confirmation.
- Do not invent stock, delivery, logistics, schedules, slots, booking, or availability facts without supplier input or trusted integration data.
- Services are not products. Scheduled service logic, booking, and on-demand service logic must stay explicit.
- Concrete product variations are separate `Product` entities in MVP. Do not add product variant tables unless a real business case requires them.
- Current MVP search has only product search and service search. Do not create a separate business search flow unless the product direction changes.
- New task contracts describe product visibility through enabled/disabled/deleted actions and service visibility through active/inactive actions. Do not model separate availability scoring, inventory-count tracking, or freshness tracking in MVP docs.
- Chat is always available from product, service, request, booking, and business-context screens through contextual contact actions.
- Business onboarding is production-facing: registration creates a real branch/store profile and its real products/services must persist in the real database. Do not design it as mock-only onboarding.
- UniqueOffers boost linked products/services in search results (+25 score). DISCOUNT offers compute effective price; non-DISCOUNT offers display as brand signals. UniqueOffers are NOT standalone search results.

## Anti-Marketplace Guardrails (2026-07-01)

Ask is NOT a marketplace. It is an **intent layer** that routes qualified demand to brands without commoditizing them.

- **Default search sort is intent_match, never price_asc.** Price is a filter factor, not the ranking king.
- **No buy-box logic.** Never collapse different brands into one SKU comparison. Always show WHY this brand matches this specific intent.
- **No uniform commodity cards.** Every result card has a standardized decision layer (price, availability, branch, pickup) AND a brand expression layer (style, tone, photos, story).
- **No public "rating" score.** Visible signals are badges: data freshness, confirmation speed, card quality, business activity. Internal ranking signals are separate.
- **Auto-reply does NOT count as confirmation.** Only real business confirmation advances status.
- **Brand profile data model:** BrandProfile (color, logo, cover, tone, links) within Business aggregate. Storefront builder (BrandPageBlock, BusinessCard) was removed in V2 restructuring.
- **UniqueOffers are brand signals and ranking boosters, not standalone cards.** Types: DISCOUNT, NEW_COLLECTION, LIMITED_RELEASE, RESTOCK, CAPSULE, SEASONAL, COLLAB, PREORDER. Statuses: UPCOMING, ACTIVE, ENDED, CANCELLED. Linked to products, services, and branches via M2M join tables.
- **User preference profile is optional and transparent.** Sizes, style, budget, city, favorite brands — editable, not creepy tracking.
- **Chat: open for extension, closed for core chaos.** Brands can add links, quick replies, FAQs, AI assistant. Cannot break user flow, spam, or change system statuses.
- **Standardize decision data, preserve brand identity.** Availability, price, branch, confirmation = standardized. Style, visual, tone, story, UniqueOffers = brand-owned.

## When To Challenge

Flag the risk before editing if a request would:

- turn Ask into only a broadcast app;
- make search secondary to manual request routing;
- hardcode one city, language, supplier type, provider, frontend, or file format;
- make old browser prototype behavior a backend requirement;
- invent unavailable data truth;
- create incompatible frontend/backend contracts;
- bypass the backend API as the product boundary.
