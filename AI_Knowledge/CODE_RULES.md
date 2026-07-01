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

- Modify existing migration files if this migration isn't in the dev branch yet
- Add new columns directly to CREATE TABLE statements.
- Add new indexes to the existing index block.

## Entity Foundation

- Every entity extends `BaseUuidV7Entity`.
- UUIDv7 IDs are assigned in the base entity before first persist.
- Audit fields are `createdAt` and `updatedAt`.
- Use Lombok `@Getter` and `@Setter` on entities instead of hand-written accessors.
- Entities stay inside their domain package and must not be returned from API.
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
- Processor touching entities or mappers — processors work ONLY with DTOs via domain service interfaces.
- Service-to-service cycles.
- Real external calls without explicit scope, credentials, provider docs, and approval.
- Assembler classes — use Mapper, Processor, or @Builder on Response instead.

## Controller Rules

- Every controller method must return `ResponseEntity<T>` from `org.springframework.http.ResponseEntity` — never a custom wrapper.
- Use `ResponseEntity.ok(body)` for 200, `ResponseEntity.status(HttpStatus.CREATED).body(body)` for 201, and `ResponseEntity.noContent().build()` for 204.
- Controller validates transport shape and delegates to Processor.
- Never return raw DTOs or entities from controller methods.
- Do not use `@ResponseStatus` — status comes from the `ResponseEntity`.

## Service Interface Rules

- Service interfaces must not expose domain entities in their signatures. Return 1-to-1 DTO copies of entities instead.
- Service interfaces must only reference entities from their own domain package.
- Only domain ServiceImpl classes may hold references to entities; even interfaces in the same domain must not return them.
- Each Service interface owns one entity type. `ProductService` owns `Product`, `ProductOfferService` owns `ProductOffer`. Do not put unrelated entity operations into the same service.
- Even the slightest naming difference between entities means separate domain services. `BusinessBranch` and `BranchMember` sound similar but are different entities — each needs its own service interface + impl.
- Service interfaces accept UUIDs and DTOs only. Never accept entities as parameters.
- `void` return is allowed only for side-effect methods (e.g., `logout`, `revoke`, `cancel`).

## Service Implementation Rules

- ServiceImpl must not use other domain repositories. Only `getReferenceById()` is allowed on foreign repositories — never `findById`, `save`, or query methods.
- ServiceImpl must only inject repositories that match its own entity type. For other entities in the same domain, call the corresponding domain service instead of injecting their repositories.
- `getReferenceById()` is the ONLY allowed cross-entity repository access — use it only to obtain JPA proxy references for setting entity relationships. All save/find/delete/query operations on foreign entities must go through that entity's own domain service.
- Services never set entity fields manually. Entity creation and field mapping lives in Mappers.
- Services validate the request, get references via `getReferenceById()`, call `mapper.toEntity(...)`, save, and return `mapper.toDto(saved)`.
- Composite result DTOs that aggregate multiple entities (e.g., `BusinessRegistrationResult`) are built via `@Builder` directly in the service impl — NOT via specialized mapper methods. Mappers only do entity ↔ single DTO conversion.
- Use `@RequiredArgsConstructor` instead of manual constructors.
- Use `@Transactional` on mutation methods only (create, update, delete, status changes). Read-only query methods must NOT be annotated with `@Transactional`.
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
- One mapper per domain layer. Do not create multiple mappers in the same domain package — all entity/DTO conversions for that domain go through one mapper class.
- Hand-written only — no MapStruct.
- Mappers never call Service, Repository, Processor, Client, or Validator.
- Mappers are called ONLY by domain service implementations. Processors and controllers never touch mappers.

## No Unused Code

- Zero unused imports. Every import must be consumed by the file.
- Zero unused methods. If a method is no longer called, delete it — do not leave dead code.
- Zero unused fields or variables.
- Zero unused classes. If a class was created speculatively and is never used, delete it.
- When removing the last caller of a mapper method, delete the mapper method and its now-unused imports in the same commit.
- Do not create "just in case" code, helper methods with no callers, or speculative abstractions.

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
