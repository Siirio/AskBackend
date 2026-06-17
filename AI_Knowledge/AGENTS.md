# Ask Agent Rules

These rules guide AI agents and developers working on Ask.

## Core Product Rules

- Ask is a request-routing and availability platform.
- Ask is not only a Telegram or WhatsApp broadcasting tool.
- Ask is not sport-nutrition-only.
- The MVP can be manual: one customer request is routed to relevant suppliers, and suppliers respond manually.
- Long-term architecture must support catalog import, normalization, search, integrations, automatic availability when justified, and service discovery.
- Do not invent inventory, logistics, schedule, delivery, or availability facts without explicit supplier input or real integration data.
- AskBackend is one backend for Android, iOS, and any future website. Never create separate backends per client implementation.

## Development Rules

- Read the nearest project rules before coding.
- Search before creating new files, endpoints, DTOs, services, configs, or docs.
- Do not run `git commit` or `git push` unless explicitly requested in the current turn.
- Do not overwrite existing `AGENTS.md`, skill docs, MCP config, plugin config, or workflow files without reading and preserving their logic.
- Keep changes scoped to the requested work.
- Do not copy secrets, local Codex configs, auth files, sqlite state, generated caches, plugin caches, or runtime paths into the repo.

## Backend Direction

- Java 21, Maven, Spring Boot, Spring MVC, PostgreSQL, Flyway, Spring Data JPA, Spring Security, OpenAPI, JUnit 5.
- Use a feature-sliced backend architecture inspired by Feature-Sliced Design: organize around product capabilities while preserving Spring boundaries inside each slice.
- Keep request routing, catalog import, service scheduling, supplier onboarding, responses, and chat locally understandable instead of scattering each feature across unrelated global folders.
- Do not introduce Gradle, Kotlin, WebFlux, Kafka, RabbitMQ, or microservices without explicit approval and justification.
- Core business logic must stay provider-agnostic.
- External systems are adapters or providers.
- Real external calls require explicit scope, credentials, provider docs, and tests.
- Backend business rules must be client-agnostic. Android, iOS, and web clients consume the same API contracts.

Target chain for non-trivial workflows:

```text
Controller -> Processor or UseCase -> DomainService -> Repository
```

Controllers validate shape, call application boundaries, and return `ResponseEntity`. Repositories are not called from controllers, mappers, assemblers, or validators. DTOs define API boundaries. JPA entities do not leak from REST controllers.

Feature slices may contain their own API contracts, use cases, services, repositories, mappers, tests, and feature-specific config when that improves local reasoning. Shared infrastructure must stay truly shared, and cross-slice communication must be explicit.

## Catalog Rules

- Do not treat catalog as simple CRUD.
- Supplier data may come from Excel, CSV, MoySklad, POS, e-commerce, CRM, messengers, or manual entry.
- Design import, mapping, normalization, categories, attributes, branches, freshness, source of truth, and search before coding.
- Product catalog work must support Excel and CSV import because sellers should not recreate existing catalogs manually.
- Manual request routing must work before catalog is mature unless product direction explicitly changes.

## Services Rules

- Services are not products with a different label.
- Services need schedules, windows, duration, specialist/provider, branch, confirmation, cancellation, and availability-source logic.
- Do system analysis before implementing service search or booking.
- Service-provider management should assume a web cabinet direction for large service datasets, schedules, discounts, and conditions, because mobile-only administration can become overloaded.

## Frontend And Mobile Rules

- Ask is mobile-first and product-first.
- Browser prototypes are tools, not backend architecture.
- Frontend and backend may live in separate repositories.
- API DTOs and UI view models can be different shapes, but contract drift must be explicit.
- Frontend owns normal UI localization; backend returns stable machine-readable statuses and error codes.
- Frontend clients should share a design-independent API/client abstraction instead of duplicating heavy business logic separately in Android, iOS, and web.

## AI Workflow Rules

- Use the smallest relevant tool or skill set.
- Use current docs lookup for changing frameworks, SDKs, CLIs, or cloud services.
- Use browser/mobile verification for visible UI changes.
- Use evidence-first debugging for bugs.
- Verify transaction boundaries before claiming partial-commit problems.
- Use Dashboard/lifecycle tooling only for meaningful work, not every prompt.

## When To Challenge

Politely flag risk before editing if a request would:

- turn Ask into only a broadcast app;
- hardcode one local market;
- make old browser staging a backend requirement;
- invent unavailable data truth;
- create incompatible frontend/backend contracts;
- conflict with foundation rules without explicitly superseding them.
