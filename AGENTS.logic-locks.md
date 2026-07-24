# Approved Domain Foundation

## LOCKED: Requirements authority and conflict handling

**Approved signal:** User clarification on 2026-07-22.

**Protected behavior:** The user's current instructions together with applicable `AI_Knowledge` feature documentation jointly define intended behavior. Applicable feature documentation must be read before behavior is diagnosed or changed. Existing code is not proof of correctness unless the exact behavior is explicitly locked as working or documented as approved.

**Conflict rule:** Stop and ask the user when instructions, documentation, or locks conflict, or when material behavior, data, authorization, or acceptance criteria are missing. Never silently choose existing code or an assumption.

**Allowed changes:** Synchronize documentation and locks after the user resolves the conflict or approves changed behavior.

## LOCKED: Search is catalog-only and user-scoped

**Approved signal:** User clarification on 2026-07-21.

**Rule:** The frontend selects `PRODUCT` or `SERVICE`. Search preserves the raw query, reads only published branch offers, and does not create requests, supplier outreach, notifications, or chats. AI may structure the selected-scope query but may not change its scope, select businesses, or invent facts.

**Scope:** search domain, search API contracts, request integrations, search projection.

## LOCKED: One business-wide customer conversation

**Approved signal:** User clarification on 2026-07-21.

**Rule:** A customer and business share one durable conversation across all branches. Product and service cards open that business conversation; branch and offer are not conversation identity. All authorized business members can see the customer history.

**Scope:** chat domain, chat API, business dashboard inbox, customer contact flows.

## LOCKED: Membership inheritance with branch exceptions

**Approved signal:** User clarification on 2026-07-21.

**Rule:** A business membership has a default role that applies to every present and future branch. Branch-specific overrides may change the role or deny access. Multiple owners are allowed. Owners may assign any role; managers may create and change workers only; workers do not manage staff.

**Scope:** business membership entities, authorization services, staff and invitation flows.

## LOCKED: Canonical entity terminology across layers

**Approved signal:** User correction on 2026-07-22.

**Protected behavior:** API fields, DTOs, backend code, frontend state and documentation use entity terminology unchanged. `BusinessScope` accepts and returns only `ITEM`, `SERVICE`, or `BOTH`; `ManagedImportRequest.selectedSourceTypes` is exposed as `selectedSourceTypes`.

**Scope:** Seller onboarding, managed import, platform access, persistence mappings and related contracts.

## LOCKED: Managed-import activation is the catalog grant

**Approved signal:** User clarification on 2026-07-23.

**Rule:** Activation assigns the platform member immediate catalog access for the request Business and matching `ITEM`, `SERVICE`, or `BOTH` scope for seven days. A global edit permission is not part of this authorization decision.

**Scope:** ManagedImportService, ManagedImportProcessor, Item/Service processors, Excel import access.

## LOCKED: Platform support conversation access

**Approved signal:** User clarification on 2026-07-23.

**Rule:** `MANAGE_SUPPORT_CHATS` permits platform inspection of `GENERAL_SUPPORT` and `PLATFORM_SUPPORT`. `MANAGED_IMPORT` requires `MANAGE_MANAGED_IMPORTS` and the assigned active grant.

**Scope:** PlatformChatProcessor and platform conversation listing.
