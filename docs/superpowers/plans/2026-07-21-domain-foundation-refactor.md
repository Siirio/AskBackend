# Domain Foundation Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Align the backend entity model and fresh V1 baseline with the approved catalog-only search, business-wide chat, inherited membership, and normalized catalog rules.

**Architecture:** Canonical business entities own legal lifecycle, public presentation, default delivery policy, memberships, and branches. Branch offers remain the only catalog-to-branch relationship. Search stays a rebuildable projection, while shared customer conversations are keyed by business and customer. Request-routing is removed instead of being repurposed as inbox state.

**Tech Stack:** Java 17, Spring Boot 3, Spring Data JPA, PostgreSQL/Flyway V1 fresh baseline.

## Global Constraints

- Do not commit or push.
- Preserve all existing uncommitted work and integrate with it.
- V1 is a fresh-deploy baseline and may be rewritten; do not execute schema changes against production data.
- Search scope comes only from the frontend and search creates no requests, outreach, notifications, or chats.
- AI structures only the selected scope and never selects businesses or invents facts.
- One item variation remains one `Product`; no variants or dynamic attribute-definition tables.
- Maven is run only after the user explicitly authorizes a build or test command.

---

### Task 1: Establish canonical entity ownership and V1 tables

**Files:**
- Modify: `src/main/java/kz/ask/business/domain/entity/Business.java`
- Modify: `src/main/java/kz/ask/business/domain/entity/BusinessDeliveryProfile.java`
- Modify: `src/main/java/kz/ask/business/domain/entity/BusinessMember.java`
- Replace: `src/main/java/kz/ask/business/domain/entity/BranchMember.java`
- Modify: `src/main/java/kz/ask/business/domain/entity/BusinessMemberBranch.java`
- Modify: `src/main/java/kz/ask/chat/domain/entity/ChatConversation.java`
- Modify: `src/main/java/kz/ask/catalog/domain/entity/Product.java`
- Modify: `src/main/java/kz/ask/service/domain/entity/ServiceOffering.java`
- Modify: `src/main/resources/db/migration/V1__init.sql`

**Produces:** Legal/lifecycle-only business root, business delivery default plus branch override table, inherited memberships with branch override/deny semantics, unique business/customer conversations, offer-only catalog branch links, and one canonical attributes map per published catalog item.

- [ ] Add the new enums required for branch access and delivery override behavior.
- [ ] Replace duplicate branch-member role storage with an explicit branch override linked to a business membership.
- [ ] Move business shipping state out of `Business` and make delivery policy entities own it.
- [ ] Remove `Product.price` and `Product.characteristicsJson`; retain canonical `attributes`.
- [ ] Require one category for published products and services through entity constraints and mapper/service validation.
- [ ] Add a unique business/customer conversation constraint and model branch/item as non-identity entry points.
- [ ] Rewrite V1 definitions so table definitions directly express the target model, without legacy create/alter/drop history for removed entities.

### Task 2: Update persistence and domain services

**Files:**
- Modify: business, catalog, service, chat, search, and import repositories/mappers/services discovered from Task 1.
- Remove: branch-member repositories/services/DTOs and their request-routing equivalents once no callers remain.

**Produces:** Services use default membership plus branch overrides, delivery services use the delivery profile, catalog duplication creates offers, and search projection reads only canonical fields.

- [ ] Replace branch-member authorization checks with effective business-membership access checks.
- [ ] Make shipping processors read and write the business delivery profile with branch fallback semantics.
- [ ] Update catalog/import mappers to use one category and `attributes` only.
- [ ] Update chat start logic to get-or-create by business/customer and share the business inbox.
- [ ] Remove request-routing dependencies from search, dashboard, platform, and service flows.

### Task 3: Update API/processors and retire request-routing surface

**Files:**
- Modify: business, catalog, service, chat, search, managed-import, identity, and platform processors/controllers/DTOs.
- Remove: request controllers, processors, repositories, entities, DTOs, and enums after all references are removed.

**Produces:** The public contract exposes explicit business chat from a catalog offer, business dashboards list shared conversations, and no obsolete request endpoint remains.

- [ ] Make catalog-card chat actions resolve business identity, not branch or offer conversation identity.
- [ ] Enforce owner/manager/worker staff-management boundaries at processors and domain services.
- [ ] Delete request-routing API surfaces and direct their former entry points to no-op-free explicit chat behavior.
- [ ] Update managed import so platform AI drafting supports both item and service catalog scopes.

### Task 4: Reconcile documentation and verify

**Files:**
- Modify: affected `AI_Knowledge` contracts/flows/locks, `AI_Knowledge/Changelog.md`, and `docs/current-system-audit.md`.

**Produces:** Documentation and schema describe the same model, with no references to automatic supplier checks or duplicated branch relationships.

- [ ] Search for retired request-routing, duplicate characteristics, bare item/service branch joins, and per-branch conversation identity.
- [ ] Run `git diff --check` and inspect Flyway SQL structure.
- [ ] On explicit user authorization, run the relevant Maven unit-test/build command and report its exact result.

