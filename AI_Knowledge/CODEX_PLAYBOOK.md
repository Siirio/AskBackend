# Codex Playbook

This is the task router for AI agents working on Ask. It replaces many small skill files so a new agent can load one focused document.

## First Rule

Read `README.md`, `AGENTS.md`, and the relevant section of this playbook before coding. If the task touches architecture, catalog, services, integrations, or API contracts, also read `ARCHITECTURE_NARRATIVE.md`.

## Backend Work

Use this route for Java, Spring Boot, APIs, persistence, request routing, stores, catalog, services, or integrations.

Expected direction:

- Java 21, Maven, Spring Boot, Spring MVC, PostgreSQL, Flyway, OpenAPI, JUnit 5.
- One backend serves Android, iOS, and web clients through stable shared APIs.
- Core business logic remains provider-agnostic.
- External systems are adapters.
- Controllers are thin and return `ResponseEntity`.
- Non-trivial flows move toward `Controller -> Processor or UseCase -> DomainService -> Repository`.
- DTOs define API boundaries; JPA entities do not leak from controllers.
- Mappers and assemblers are pure.
- Schema changes use Flyway.

Before editing, search for existing local patterns and map dependent DTOs, mappers, services, repositories, tests, and config.

Do not design separate backend behavior for Android, iOS, and web. If clients need different presentation, expose stable API data and let client adapters map it to view models.

## Frontend And Mobile Work

Use this route for UI, client contracts, mobile UX, frontend prototypes, or frontend/backend API alignment.

Expected direction:

- Ask is mobile-first and product-first.
- Native mobile clients may become primary; browser prototypes are tools, not product architecture.
- Android, iOS, and web UI should consume a shared design-independent client/API abstraction where possible.
- Keep API DTOs separate from UI view models.
- Do not invent backend data in the UI.
- Frontend owns normal UI localization; backend returns stable machine-readable statuses and error codes.
- Preserve shared product meaning across frontend and backend.

Do not copy browser-only prototype mechanics into backend requirements.

## Catalog Work

Use this route for product catalog, supplier data import, normalization, attributes, categories, availability, and catalog-backed search.

Do not treat catalog as simple product CRUD. First analyze:

- source format, especially Excel and CSV, plus MoySklad, POS, e-commerce export, or manual entry;
- column mapping;
- product identity and duplicate handling;
- category mapping;
- attribute model;
- branch and city scope;
- price and availability freshness;
- source of truth;
- search behavior;
- supplier correction workflow.

Manual request routing must still work before catalog is mature unless product direction explicitly changes.

Product sellers should not have to recreate existing catalogs manually. Prefer upload, preview, column mapping, validation, correction, and repeated import/update flows for Excel and CSV.

## Services Work

Use this route for service discovery, schedules, appointments, free windows, specialists, branches, bookings, or provider availability.

Services are not products with a different label. Before coding, analyze:

- service provider and branch structure;
- specialist or resource ownership;
- schedule and free-window source;
- duration and price model;
- confirmation and cancellation flow;
- integration options;
- what can be trusted as current availability;
- what must not be hardcoded for one city or provider type.

If availability is not backed by a reliable source, model it as confirmation-needed instead of a guaranteed slot.

Assume a web cabinet is the likely management surface for service providers. Large service datasets, schedules, discounts, conditions, specialists, and free windows are too cumbersome to manage only inside the mobile app.

## Integration Work

Use this route for Telegram, WhatsApp, Paloma, 1C, re:Kassa, Shopify, MoySklad, POS, CRM, fiscal, e-commerce, scheduling, or future provider APIs.

Rules:

- Do not implement real external calls without explicit scope, credentials, and provider docs.
- Keep adapters outside core business logic.
- Do not leak credentials or provider internals to API clients.
- Do not present external facts as true unless the provider response or explicit supplier input supports them.

## System Analysis Gate

Before code, produce a short analysis when the task affects:

- catalog;
- services;
- schedules;
- integrations;
- city/country scaling;
- auth or roles;
- API contract shape;
- data ownership;
- cross-domain architecture.

Use this shape:

```text
Problem:
Actors:
Current evidence:
Proposed model:
MVP shortcut:
Deferred decisions:
Risks:
Verification:
```

## AI Workflow

AI agents should keep the team aligned without becoming a style dictator.

Check whether the task:

- conflicts with Ask vision;
- confuses old prototype behavior with product architecture;
- invents availability, logistics, schedule, or inventory facts;
- hardcodes one city, category, supplier, file, frontend, or provider;
- creates backend/frontend contract drift;
- needs documentation updates.

If there is a conflict, state it concretely, explain the risk, and offer a compatible path.

## MCP And Tool Usage

Use tools selectively:

- local shell/git for simple local state;
- current documentation lookup for changing libraries, SDKs, CLIs, and cloud services;
- browser or Playwright for visible frontend behavior;
- Dashboard or similar lifecycle tools only for meaningful starts, pivots, and completions;
- provider-specific tools only when authenticated and in scope.

Do not copy local MCP configs, tokens, auth files, sqlite state, generated caches, or runtime paths into the repo.
