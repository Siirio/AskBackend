# AskBackend Project Rules

These rules are the nearest project instructions for AskBackend. Read this file before changing code or project structure.

## Product Direction

- AskBackend is one backend for Android, iOS, and future web clients.
- Ask is search-first: return known products, services, and businesses before creating fallback requests.
- Fallback requests exist only when data is missing, stale, low-confidence, or confirmation-needed.
- Do not invent stock, delivery, logistics, schedules, slots, booking, or availability facts without supplier input or trusted integration data.
- Concrete product variations are separate `Product` entities in MVP. Do not add product variant tables unless a real business case requires them.
- Services are not products. Scheduled service logic, booking, and on-demand service logic must stay explicit.

## Stack

- Java 21.
- Maven.
- Spring Boot.
- Spring MVC.
- Spring Data JPA.
- PostgreSQL.
- Flyway.
- Spring Security.
- OpenAPI.

Do not introduce Gradle, Kotlin, WebFlux, Kafka, RabbitMQ, Redis, microservices, or real external integrations without explicit approval.

## Architecture

- Use a modular monolith with feature packages.
- Feature packages must use backend boundaries: `api`, `application`, `domain`, and `infrastructure`.
- Non-trivial flow direction is `Controller -> Processor -> DomainService -> Repository`.
- Controllers receive request DTOs and return response DTOs.
- Processors orchestrate use cases and work with DTOs.
- Domain services own business logic and map entities internally.
- Repositories are never called from controllers, mappers, assemblers, validators, or other repositories.
- Entities never leave their owning domain boundary.
- Cross-feature communication uses DTOs, processors, or explicit interfaces, not direct repository reads.
- A domain service may use another feature repository only for FK reference attachment.

## DTO Rules

- Use three DTO types only unless a real need is proven:
  - `Request`: incoming API payload.
  - `Response`: outgoing API payload.
  - `Dto`: full internal feature transfer shape.
- Do not use nested DTO classes.
- API DTOs and mobile/web UI models can differ, but backend contracts must stay stable and explicit.

## Entity Rules

- All persistent entities extend the project base UUIDv7 entity.
- Entity IDs are UUIDv7 generated before persist.
- Use explicit enum fields for state machines and data-truth statuses.
- Keep table names snake_case and aligned with `AI_Knowledge/PRODUCT_SERVICE_FOUNDATION_ERD.md`.
- Do not add entity relationships just for navigation convenience if they create cycles or broad loading.
- Prefer FK references through `@ManyToOne(fetch = FetchType.LAZY)` for required ownership links.
- Keep mappers pure. Mappers do not call repositories, services, processors, clients, or validators.

## Folder Creation Rules

Create new backend code under `src/main/java/kz/ask`.

Feature package shape:

```text
kz/ask/<feature>/
  api/
    dto/
  application/
    processor/
  domain/
    entity/
    enums/
    service/
  infrastructure/
    repository/
    mapper/
    client/
```

Shared package shape:

```text
kz/ask/shared/
  domain/
    entity/
    enums/
  infrastructure/
    persistence/
    uuid/
  error/
  security/
```

Do not create broad global `controller`, `service`, `repository`, `dto`, `mapper`, or `entity` packages.

## Verification

- Never create test classes or test files unless the user explicitly reverses this rule.
- Do not run Maven commands unless the user asks for them.
- Use file review, `git diff --check`, schema/FK review, and compile checks only when explicitly requested.

## Documentation

- Keep architecture decisions in `AI_Knowledge`.
- `AI_Knowledge/PRODUCT_SERVICE_FOUNDATION_ERD.md` is the entity and relationship source for MVP.
- Update `AI_Knowledge/CHANGELOG_FOUNDATION.md` when changing product direction, architecture rules, ERD, API contract strategy, catalog strategy, services strategy, or integration assumptions.
