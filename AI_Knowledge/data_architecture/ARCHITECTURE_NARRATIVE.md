# Architecture Narrative

Ask is a search-first product for finding products and services from real local businesses. The current MVP is not a broad business search engine and not a request-only broadcast app.

The target customer flow is:

```text
Customer chooses product or service search
  -> enters raw query
  -> sees enabled products or enabled services from registered branches
  -> opens product/service/business context, chat, or creates fallback request
```

## Current Product Direction

- Customer search has two primary scopes: products and services.
- Business pages open from context, not from a separate business search.
- Categories narrow product/service search.
- Raw query is preserved everywhere.
- Dynamic filters are based on actual result attributes.
- Automatic supplier check exists when no suitable result exists, when catalog results are weak, or when suitable suppliers should confirm manually.
- The customer does not manually create the main fallback request after product search. Search submit can automatically trigger supplier check for suitable branches.
- Supplier check is attached to the search session and does not create a standalone business search scope.

## Business Onboarding Direction

Business onboarding is production-facing. It is not a mock-only flow.

The immediate goal is to register real stores/branches before the client app launch so customer search has real data to show. A business registration currently means one concrete branch/store/establishment joins Ask.

After registration:

- the branch profile persists in the real database;
- the branch can add products;
- the branch can add services;
- enabled products and services appear in customer search;
- disabled or deleted products and services stop appearing in live search.

A future account can manage multiple branches, but each branch must have its own contacts and its own products/services. Do not collapse all future branch contacts into one company-wide registration contact.

## One Backend, Many Clients

AskBackend is the single backend for Android, iOS, web, and desktop/PWA clients.

Frontend owns presentation, navigation, and local UI state. Backend owns:

- identity and real contact verification;
- persisted business onboarding data;
- product and service records;
- search indexing;
- branch ownership and contacts;
- request routing;
- chat context;
- data retention policy.

## Product Catalog MVP

Products are real business-entered or imported items.

Current MVP rules:

- one concrete sellable item is one `Product`;
- a product belongs to a business and is offered by a concrete branch through `ProductOffer`;
- `ProductOffer.enabled` controls live search visibility for a branch-level product offer;
- `Product.status` remains the product record lifecycle;
- inventory counting is outside the MVP;
- separate data-freshness tracking is outside the MVP;
- product visibility is driven by business enable/disable/delete actions;
- if the customer needs confirmation, use clarify action, Ask chat, or fallback request.
- if exact/strong catalog results are insufficient, Ask can automatically check suitable suppliers selected by category, tags, branch profile, city, and similar enabled product offers.
- automatic supplier check does not prove availability; only supplier response can confirm availability, analog, rejection, or clarification need.

## Services MVP

Services are separate from products.

The service booking model has three maturity levels. Only Levels 1 and 2 are in current MVP scope. Level 3 is explicitly deferred.

### Core Philosophy: Chat-First, Button-for-Fixation

Ask Services MVP is **not a booking calendar** — it is **chat + structured fixation of a final agreement**.

- The primary communication channel between business and customer is the regular Ask chat. Buttons/actions within chat exist not to replace communication, but to **record the result of an agreement already reached in chat**.
- The customer selects a desired time when creating a request → this is **requested/desired time**, not a guaranteed booking.
- Business and customer communicate in chat. During communication they may agree on a different time.
- When agreement is reached, the business fixes the final **confirmedStartAt / confirmedEndAt** within the chat.
- Confirmation/change/cancellation of time creates a **system event in conversation** — visible to both customer and business.

### Three-Tier Service Maturity Model

#### Level 1: MVP Request-to-Book (current Task 04)

- Customer sends a request with desired time.
- Time is **desired** — not a guaranteed slot.
- Business confirms, declines, or continues discussion in chat.
- No automatic guarantee of a free slot.

#### Level 2: Minimal Confirmed Appointment Tracking (current Task 04)

- After chat, business fixes the final **confirmedStartAt / confirmedEndAt**.
- This creates a confirmed appointment record in the `booking` table.
- The confirmed interval **blocks future suggested time options** for this service/branch (minimal overlap check).
- This is NOT full CRM — no resources, masters, shifts, automatic slot availability.

#### Level 3: Future Calendar System (NOT in Task 04)

- Masters, resources, employee schedules, overlaps, integrations, automatic slot availability.
- **Nothing from this level is implemented now.**

### Three-Level Time Model

Service requests track three distinct time levels:

- `requestedStartAt` — the time the customer specified when creating the request (desired time). Never changed by backend.
- `proposedStartAt` — the time the business counter-offered via `SUGGEST_OTHER_TIME`. Can be updated on repeated proposals.
- `confirmedStartAt` / `confirmedEndAt` — the finally agreed time, fixed by the business via `CAN_PROVIDE`. Only this time creates a confirmed appointment.

### Current MVP Rules

- a service belongs to a business and is offered by a concrete branch;
- `ServiceBranchOffer.active` controls live search visibility for a branch-level service offer;
- active services appear in live client search;
- inactive services do NOT appear in live client search;
- `ServiceBranchOffer.scheduleText` is display/conditions text, not guaranteed slot truth;
- service requests are request-to-book, not guaranteed slot reservations;
- the customer can choose desired time (`requestedStartAt`);
- business confirms final time (`confirmedStartAt`/`confirmedEndAt`), declines (`CANNOT_PROVIDE`), or proposes another time (`SUGGEST_OTHER_TIME` with `proposedStartAt`);
- MVP does not model full staff/calendar slot blocking;
- confirmed appointments at Level 2 perform minimal overlap checks for the same service/branch/time interval.

### ActivityDisplayStatus

`ActivityDisplayStatus` is the **only** status visible in the Activity UI. It is **never stored** in the database — it is derived at runtime from the request lifecycle status and the supplier response status.

| ActivityDisplayStatus | Condition | Meaning |
|---|---|---|
| `DISCUSSING` | All cases except the two below | Request is in discussion. Business can respond, confirm, decline, suggest other time. |
| `CONFIRMED` | `customerRequestStatus ∈ {COMPLETED, PARTIALLY_RESPONDED}` AND `supplierResponseStatus = CAN_PROVIDE` AND `confirmedStartAt != null` | Time is agreed. `booking` record created. Actions: open chat. |
| `CONFIRMATION_DECLINED` | `supplierResponseStatus = CANNOT_PROVIDE` | Business declined. Actions: open chat (chat remains accessible). |

Key rules:
- `CAN_PROVIDE` without `confirmedStartAt` = `DISCUSSING` (business said "can do" but time not yet fixed).
- `SUGGEST_OTHER_TIME` = always `DISCUSSING` (business proposed another time — waiting for customer response in chat).
- `NEED_CLARIFICATION` = always `DISCUSSING` (business asked a clarifying question in chat).
- Every confirmation, time change, or cancellation creates a **system event in conversation** — visible to both customer and business.

## Membership And Authority Model

Business membership has two levels: business-level ownership and branch-level staff roles.

### Business-Level Membership

`BusinessMember` links an `AppUser` to a `Business` with the OWNER role. The user who registers the business becomes its owner. There is exactly one owner per business in MVP.

### Branch-Level Membership

`BranchMember` links an `AppUser` to a `BusinessBranch` with STAFF role. These are staff accounts created by the owner.

- STAFF: works inside the assigned branch. Cannot manage other staff, branches, or business settings.

### Authority Resolution

Authority is computed at session creation and stored as a string in `auth_session.authority`:

| Condition | Authority |
|---|---|
| `AppRole.CUSTOMER` | `ROLE_CUSTOMER` |
| `AppRole.BUSINESS` + `BusinessMember(OWNER)` | `ROLE_BUSINESS_OWNER` |
| `AppRole.BUSINESS` + `BranchMember(STAFF)` | `ROLE_BUSINESS_STAFF` |

Authority already includes the `ROLE_` prefix for Spring Security. All staff are `AppRole.BUSINESS` users distinguished by their branch membership record.

### Branch Access Rules

- Business owner has access to all branches of their business (no BranchMember record needed).
- Staff has access to their specific branch through BranchMember.

## Current Data Model Alignment

The current code and `V1__init.sql` intentionally do not contain:

- product stock status or stock quantity fields;
- availability confidence fields;
- data freshness timestamps;
- standalone business search documents or business result counts;
- service confirmation policy fields that imply automatic booking truth.

Search documents index only product offers and service branch offers. Search result snapshots may store `business_id` and `branch_id` only as context for a product/service row.

## Search And Distance

Search ranking should be smart and practical. It can use query matching, category, result attributes, enabled state, price, and distance when distance is known. The public contract should stay focused on the visible product/service result and its branch context.

### Anti-Marketplace Ranking (2026-07-01)

Default sort is **intent_match**, never price_asc. The intent_match score combines:
- Query relevance (category + tag + description match)
- Style vector proximity (when style preferences are known)
- Availability signal (in stock > needs confirmation > unknown)
- Distance (when coordinates available)
- Data freshness (recently updated > stale)
- Business activity level (active > dormant)
- Card completeness (well-filled > minimal)

Price is a filter factor, not the default sort king. Available sort options: intent_match, availability, distance, price_asc, price_desc, business_activity, card_quality.

**Critical:** Backend computes raw scores internally. Frontend receives human-readable match reasons ("В бюджете, oversized fit, самовывоз сегодня"), NEVER raw confidence percentages.

### Brand-Aware Result Cards

Search results carry two layers:
1. **Standardized decision layer:** price/range, availability, branch/city, pickup, timelines, confirmation status, match_reason (human-readable string from backend).
2. **Brand expression reference:** brandId, brandProfile snapshot (color, logo, cover) — enough for frontend to render brand identity without extra round-trips.

Full brand expression (storefront blocks, collections, drops) lives on the Brand Profile endpoint, not in search result DTOs.

### Supplier Quality Signals (Internal)

Backend tracks per-business quality signals for ranking, NOT for public display:
- `data_freshness_score` — how recently data was updated
- `card_completeness_score` — how well-filled the business profile and product cards are
- `confirmation_accuracy` — ratio of HAS_ITEM responses that were actually available
- `response_time_trend` — median time to first response (not used as primary rank)
- `activity_level` — frequency of logins, updates, responses

These produce visible badges: "Данные обновлены сегодня", "Быстро подтверждает наличие", "Хорошо заполненная карточка", "Активный бизнес".

Auto-reply (AI bot response) does NOT advance confirmation status. Statuses: AUTO_REPLY, BUSINESS_CONFIRMED, DATA_RECENTLY_UPDATED, NEEDS_CONFIRMATION.

### Brand Profile & Storefront (New Data Model)

`BrandProfile` (part of Business aggregate):
- brandColor, logoUrl, coverUrl
- toneOfVoice, description
- links: instagram, telegram, website

`BrandPageBlock` (ordered, toggleable):
- blockType: HERO, COLLECTION, PRODUCTS, ABOUT, DROP, CONTACTS, BRANCHES
- displayOrder, configJson (block-specific configuration)

`Drop` (time-limited brand event):
- name, description, startDate, endDate
- type: NEW_COLLECTION, LIMITED_RELEASE, RESTOCK, CAPSULE, SEASONAL, COLLAB, PREORDER
- status: UPCOMING, ACTIVE, ENDED

### User Preference Profile (New Data Model)

`UserPreferenceProfile`:
- sizes (string array), preferredCategories (UUID array)
- budgetRange (min/max), cityId, district
- styleTags (string array), favoriteBrands (UUID array)
- excludeMassMarket (boolean), preferPickup (boolean), showOnlyInStock (boolean)

Optional, transparent, user-editable. Used to boost intent_match scoring, not as hard filter.

`distanceMeters` is calculated only when:

- customer geolocation is provided;
- branch coordinates exist;
- backend calculates distance from customer coordinates to branch coordinates.

If coordinates are missing, return `distanceMeters=null`.

## Data Truth

Ask must not invent facts.

Allowed MVP facts:

- enabled product exists in branch catalog;
- active service exists in branch service list;
- business-provided price when present;
- business-provided branch address and contacts;
- calculated distance when coordinates exist;
- business response status when business answered.

Not tracked in MVP:

- inventory counting;
- separate data-freshness tracking;
- separate scoring fields for availability;
- automatic delivery SLA;
- courier availability;
- guaranteed service slot availability.

## Persistence And Deployment Direction

Once the site is given to real businesses for onboarding:

- database must be persistent;
- data must not be dropped casually;
- frontend, backend, and database should be deployable together;
- seed/demo data must not replace real business registrations.

Render or another deployment target can host the app, but provider-specific setup belongs to deployment tasks, not product task DTOs.
