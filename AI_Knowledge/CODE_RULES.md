# AskBackend Code Rules

This document keeps project-specific code restrictions and implementation rules for fresh AI sessions and developers. Agent workflow rules belong in `AGENTS.md`; Codex infrastructure belongs in `codex/CODEX_INFRASTRUCTURE.md`.

## Core Boundary

AskBackend must stay a single modular monolith. It exposes one backend API for Android, iOS, and web clients.

The backend owns:

- product and service search truth;
- catalog import and normalization;
- branch-level offers;
- booking lifecycle;
- fallback request routing;
- universal conversations;
- integration boundaries.

Clients own presentation, localization text, and UI view models.

## Package Rules

Use feature-first packages:

```text
kz.ask.shared
kz.ask.identity
kz.ask.business
kz.ask.catalog
kz.ask.service
kz.ask.search
kz.ask.request
kz.ask.messaging
```

Each feature follows:

```text
api
application
domain
infrastructure
```

Do not create global technical buckets such as one project-wide `controller`, `service`, `repository`, `dto`, or `mapper` package.

## Type Rules

- Never use primitive types (`int`, `boolean`, `double`) — use wrappers (`Integer`, `Boolean`, `Double`).
- Never use concrete collection types in declarations — use interfaces (`List` not `ArrayList`, `Map` not `HashMap`).
- `void` return type is allowed only for methods that perform side effects (e.g., `logout`, `revokeInvite`).

## Nesting And Anonymous Class Rules

- No records or classes inside interfaces. Extract to standalone files.
- No static inner classes inside DTOs or any other class. Every DTO must be a standalone file.
- No anonymous classes — including `new TypeReference<>() {}`. Use Jackson `TypeFactory.constructMapType()` or `constructCollectionType()` instead.

## Migration Rules

- Modify existing migration files (V1__init.sql) — never create new V2, V3 migration files.
- Add new columns directly to CREATE TABLE statements.
- Add new indexes to the existing index block.

## Entity Foundation

- Every entity extends `BaseUuidV7Entity`.
- UUIDv7 IDs are assigned in the base entity before first persist.
- Audit fields are `createdAt` and `updatedAt`.
- Use Lombok `@Getter` and `@Setter` on entities instead of hand-written accessors.
- Entities stay inside their domain package and must not be returned from API methods.
- Use enums for statuses and data-truth states.
- Avoid bidirectional relationships unless there is a clear domain need.
- Use `FetchType.LAZY` for entity references.

## Layer Direction

```text
Controller -> Processor -> DomainService -> Repository
```

- Controller validates transport shape and returns response DTOs.
- Processor orchestrates use cases and cross-domain flow.
- DomainService owns business logic and entity persistence. Use interface + impl naming: `IdentityService` (interface) + `IdentityServiceImpl`, both in the same domain package. Callers depend on the interface.
- Repository owns persistence only.
- Mapper converts Entity and Dto only.
- Client packages talk to external systems only when explicitly approved.

Forbidden:

- Controller calling Repository.
- Mapper calling Service, Repository, Processor, Client, or Validator.
- DomainService calling Processor.
- DomainService returning Entity to another layer.
- Service-to-service cycles.
- Real external calls without explicit scope, credentials, provider docs, and approval.
- Assembler classes — use Mapper, Processor, or @Builder on Response instead.

## Controller Rules

- Every controller method must return `EntityResponse<T>` wrapping the response DTO.
- `EntityResponse` lives in `kz.ask.shared.api.dto`.
- Controller validates transport shape and delegates to Processor.
- Never return raw DTOs or entities from controller methods.

## Service Interface Rules

- Service interfaces must not expose domain entities in their signatures. Return 1-to-1 DTO copies of entities instead.
- Service interfaces must only reference entities from their own domain package.
- Only domain ServiceImpl classes may hold references to entities; even interfaces in the same domain must not return them.
- Each Service interface owns one entity/aggregate type. `ProductService` owns `Product`, `ProductOfferService` owns `ProductOffer`. Do not put unrelated entity operations into the same service.

## Service Implementation Rules

- ServiceImpl must not use other domain repositories. Only `getReferenceById()` is allowed on foreign repositories — never `findById`, `save`, or query methods.
- ServiceImpl must only inject repositories that match its own entity/aggregate type. For other entities in the same domain, call the corresponding domain service instead of injecting their repositories.
- Services never set entity fields manually (except `entity.setId(uuidV7Generator.generate())`). Entity creation and field mapping lives in Mappers.
- Services validate the request, get references via `getReferenceById()`, call `mapper.toEntity(...)`, set the ID, call `mapper.enrichCreated(entity)`, save, and return `mapper.toDto(saved)`.
- Use `@RequiredArgsConstructor` instead of manual constructors.
- Never use primitive types (`int`, `boolean`) — use wrappers (`Integer`, `Boolean`).
- `void` return type is allowed for methods that perform side effects (e.g., `logout`, `revokeInvite`).
- Never throw `RuntimeException` or `IllegalArgumentException` — use the shared exception hierarchy from `kz.ask.shared.error`.
- Never call `findAll()` — always use filtered query methods with specific criteria.

### Service Flow

```
Service                              Mapper
------                              ------
validate(request)
refs = repo.getReferenceById()
entity = mapper.toEntity(...)  →    Entity e = new Entity();
                                     e.setField1(ref1);
                                     e.setField2(request.getX());
                                     return e;
entity.setId(uuidGenerator())        ← only non-mapper field set
mapper.enrichCreated(entity)     →    sets createdBy, audit fields
saved = repo.save(entity)
return mapper.toDto(saved)       →    Dto dto = new Dto();
                                     dto.setId(saved.getId());
                                     return dto;
```

Concrete example from `registerBusiness()` — the forbidden anti-pattern:

```java
// WRONG: manual entity building inside service
Business business = new Business();
business.setName(businessName);
business.setStatus(RecordStatus.ACTIVE);
business = businessRepository.save(business);
// ... repeats for branch, member, contact
```

Correct pattern: delegate to `BusinessMapper.toEntity(request, refs)`.

## DTO Types

Use only these DTO families by default:

- `*Request` for incoming API payload.
- `*Response` for outgoing API payload.
- `*Dto` for internal feature transfer.

Use `@Builder` on all Requests and Responses. Use `@Getter` and `@Setter` instead of hand-written accessors. Never use `@Data`.

No nested DTO classes. Create standalone DTO files when DTOs exist.

## Mapper Rules

- Mappers own Entity ↔ Dto mapping. They do `new Entity()` + `.setX().setX().setX()`. No business logic, just field copying.
- Mappers live in `infrastructure/mapper/` per feature.
- Hand-written only — no MapStruct.
- Mappers never call Service, Repository, Processor, Client, or Validator.
- ID assignment lives in Services (not Mappers), always via `uuidV7Generator.generate()`.

## Error Handling Rules

All exceptions use a unified hierarchy in `kz.ask.shared.error`:

- `BusinessException` — abstract base with `ErrorCode`, `HttpStatus`, `Object[] args`.
- `NotFoundException` (404), `ValidationException` (400), `ConflictException` (409), `ForbiddenException` (403), `UnauthorizedException` (401), `AuthException` (401), `InternalServerException` (500), `ExternalServiceException` (502).

Error codes live in `ErrorCode` enum with Russian message templates and `format(Object... args)` method.

One handler: `kz.ask.shared.api.GlobalExceptionHandler` replaces feature-specific handlers.

Error response: `ErrorResponse` (timestamp, errorCode, message, errors). Validation details go into `ErrorDetail` (field, message).

Rules:

- Never catch and rewrap — let exceptions propagate to the handler.
- Never create feature-specific exception classes — use the shared hierarchy with the right `ErrorCode`.
- Always pass format args to `ErrorCode` when the template contains `%s`.
- Never throw generic `RuntimeException` — use the appropriate subclass from `kz.ask.shared.error`.

## Product MVP Rule

One concrete sellable variation is one `Product`.

Examples:

- `Mammut protein 5kg Chocolate`
- `Mammut protein 1kg Strawberry`
- `Mammut protein 5kg Vanilla`

Search grouping is handled by `Product.tags` and `SearchDocument`, not by product variant tables.

Do not create:

- `ProductVariant`
- `ProductAttributeDefinition`
- `ProductVariantAttributeValue`
- `ProductAlias`
- product availability history snapshots

until a real business case requires them.

## Service MVP Rule

`ServiceResource` is an optional abstract capacity resource for scheduled services. It must not imply specialist accounts, payroll, login, or specialist UI.

`ON_DEMAND` service branch offers must work without resources, schedules, and windows.

`SCHEDULED` service branch offers may use resources, schedules, windows, and booking.

`Booking` is separate from `CustomerRequest`. A request asks for confirmation. A booking is a confirmed or pending service commitment.

## Messaging Rule

Use universal conversation entities. Do not permanently bind chat only to customer request flow.

Conversations can be linked to:

- customer request;
- booking;
- product offer;
- service branch offer.

## Verification Rule

Do not create tests.

Do not run Maven commands unless explicitly asked.

For normal project changes, use:

- file review;
- `git diff --check`;
- dependency and FK review;
- compile only if explicitly requested.
