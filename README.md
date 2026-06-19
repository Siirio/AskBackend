# Ask

Ask is a local search platform for products and services across city businesses.

The primary user flow is search-first: a customer searches for a product or service, Ask shows matching catalog/service results when the data exists, and the customer immediately understands which business can help. If exact data is missing or confidence is too low, Ask can send a fallback request to suitable sellers or service providers for confirmation.

The product is broader than request routing. Ask should become a reliable local business search and availability layer between customers and stores or service providers. That requires business onboarding, product/service catalogs, Excel/CSV import, data normalization, Smart Search, search indexing, clear response rules, provider outreach, and integration boundaries for systems that suppliers already use.

## The Problem

Customers often need something now but do not know where it is actually available. They may search several marketplaces, call shops, write to messengers, compare incomplete listings, and still discover that the product is unavailable or the service has no free time.

Suppliers have the opposite problem. They want real demand, but they may not have a clean public catalog, a modern booking system, or time to maintain another heavy admin panel. Many early suppliers may already use Excel, MoySklad, POS tools, e-commerce exports, messengers, CRM systems, or manual workflows.

Ask should connect those two sides without pretending that perfect data exists on day one.

## Why Ask Is Not Just Another Marketplace

A classic marketplace usually starts with a tightly controlled catalog. Ask starts from local business search and availability:

- the customer searches in natural language;
- Ask finds products, services, and businesses from known data first;
- if data is insufficient, Ask routes a confirmation request to suitable suppliers;
- catalog and integration quality improves over time;
- automatic availability becomes valid only when real data supports it.

Ask should not force every supplier into perfect catalog migration before the product is useful. It should still build toward catalog/search as the core product and keep manual requests as a fallback for missing, stale, or uncertain data.

## Product Direction

Ask grows in layers:

1. Business/store registration and stable business ownership.
2. Product and service domain models.
3. Product/service CRUD for businesses.
4. Product catalog import from Excel, CSV, MoySklad, POS, e-commerce, or other sources.
5. Searchable entity contracts, search indexing, and Smart Search over rough customer queries, categories, attributes, aliases, and availability signals.
6. Search results that show where a known product or service is available when data supports it.
7. Product request fallback when exact data is missing, stale, or requires supplier confirmation.
8. Supplier replies, request-scoped chat, and contact actions.
9. Integration-backed automatic availability where real provider data exists.
10. Service discovery for appointments, schedules, free windows, specialists, branches, confirmations, and cancellations.

## Target Product Architecture

AskBackend is one backend for all clients. Android, iOS, and any future website must use the same backend API. Do not create a separate backend for each frontend implementation, and do not make backend business logic depend on Android, iOS, or web UI differences.

Client applications should share the same contract model:

```text
Android UI
iOS UI
Web UI
  -> shared client/API abstraction
  -> AskBackend API
```

Frontend implementations may have different visual UI and platform behavior, but they should not duplicate heavy business logic. Backend owns search data truth, catalog processing, service-provider data, request fallback, permissions, and integration boundaries. Client layers adapt backend DTOs into view models and handle platform-specific presentation.

## Backend Architecture Direction

Feature-Sliced Design is a frontend methodology. Do not copy it literally into Spring Boot: `pages`, `widgets`, `features`, `entities`, and `shared` are frontend concepts and would look unnatural as backend packages.

The backend should use the same useful idea of slicing, but in backend terms: package-by-feature, modular monolith, domain modules, DDD-style modules, and clean/hexagonal architecture boundaries.

Avoid a structure that grows into broad technical buckets such as one global `controller`, `service`, `repository`, `dto`, `mapper`, and `entity` package for every product area. That shape becomes hard to navigate as Ask grows across customers, sellers, requests, responses, categories, branches, notifications, moderation, integrations, analytics, catalog import, and services.

Prefer feature/domain-based packaging:

```text
src/main/java/kz/ask/
  request/
    api/
      RequestController.java
      dto/
    application/
      CreateRequestService.java
      RespondToRequestService.java
    domain/
      Request.java
      RequestStatus.java
      RequestPolicy.java
    infrastructure/
      RequestRepository.java
      JpaRequestRepository.java

  store/
    api/
      StoreController.java
      dto/
    application/
      RegisterStoreService.java
      UpdateStoreAvailabilityService.java
    domain/
      Store.java
      StoreBranch.java
    infrastructure/
      StoreRepository.java

  product/
    api/
    application/
    domain/
    infrastructure/

  shared/
    security/
    errors/
    persistence/
    clock/
    events/
```

Inside each module, keep clean boundaries: `api` receives HTTP/API input, `application` orchestrates use cases, `domain` owns business rules, and `infrastructure` talks to persistence or technical adapters. Shared infrastructure must stay truly shared, and cross-module calls should go through explicit interfaces or application use cases, not hidden direct coupling.

## Catalog And Services Direction

Product sellers usually already have product data in files or systems. The backend must support practical import paths, especially Excel and CSV, so sellers do not have to manually recreate catalogs from scratch. Catalog work should include file upload, column mapping, validation, normalization, duplicate handling, category/attribute mapping, branch-level data, import history, and data-quality feedback.

Catalog is now part of the core search strategy, not a distant optional add-on. If a product or service already exists in Ask data, the backend should be able to return it through search before creating a manual request.

Services have a different operational shape. Service providers need to manage offerings, schedules, free windows, discounts, conditions, specialists, branches, confirmations, and cancellations. Doing that only inside a mobile app can become overloaded and inconvenient. The planned direction is a web cabinet for service providers, so they can manage larger service datasets and availability more comfortably.

Current product surface direction:

- mobile application has two sides: customer and seller/supplier;
- website is primarily for establishments that provide services and need a larger workspace for managing service offerings, schedules, windows, discounts, and conditions;
- product-catalog bulk import must support Excel and CSV workflows;
- customer search should show known products/services first and use requests only when data is insufficient;
- future web surfaces may also support bulk product catalog operations if that becomes the most usable supplier workflow, but this should remain API-backed and not become a separate backend.

## Current Foundation Scope

This repository is a foundation for future Ask backend and AI-assisted development. It preserves the product vision, backend code rules, data architecture, client contract expectations, and first-session Codex setup notes that new developers and Codex agents should load before coding.

It is not an old prototype dump and not a local-machine-specific Codex export.

## Important Documents

- `AGENTS.md`: short agent workflow, first-session pointer, task routing, and product guardrails.
- `AI_Knowledge/first_steps/FIRST_READ_THIS.md`: start here when a new person or Codex agent opens the repo.
- `AI_Knowledge/first_steps/IMPLEMENTATION_PIPELINE.md`: how to keep extending this foundation safely.
- `AI_Knowledge/CODE_RULES.md`: project-specific backend code architecture, folder creation, DTO, entity, Lombok, and verification rules.
- `AI_Knowledge/data_architecture/ARCHITECTURE_NARRATIVE.md`: technical story and architecture direction.
- `AI_Knowledge/data_architecture/PRODUCT_SERVICE_FOUNDATION_ERD.md`: MVP entity strategy, ERD, table connections, booking, messaging, and product/service search boundaries.
- `AI_Knowledge/client_contracts/UX_UI_BACKEND_CONTRACT.md`: backend-facing contract extracted from the UX/UI flow.
- `codex/CODEX_INFRASTRUCTURE.md`: expected Codex plugins, MCP servers, and routing behavior without local config.
- `AI_Knowledge/CHANGELOG_FOUNDATION.md`: what changed in this foundation.

## Non-Goals

- Do not hardcode Ask around one city, one store, one Excel file, one frontend, one old prototype, or one provider.
- Do not claim inventory, service slots, delivery, logistics, or guaranteed availability facts unless a supplier or real integration provides them.
- Do not copy local Codex configs, tokens, auth files, sqlite state, generated caches, plugin caches, or runtime paths into this repo.
- Do not make browser web staging the architecture of the future backend.

## Working Principle

Ask should stay simple enough to launch, but structured enough that launch decisions do not block future catalog, integration, services, mobile, and multi-city growth.
