# ASK Backend — Code Rules

## All code
- Search before create: find existing patterns before writing new. Reuse.
- Zero unused code: no methods, classes, or imports that aren't called.
- Zero comments: code must be self-documenting through names and structure.
- Zero hardcode: no magic numbers, no inline URLs, no embedded config values.
- Update all relations: when changing X, find and update every dependent.
- Follow existing patterns: mirror the codebase. Don't invent new approaches.

## Package structure
Feature-first: `kz.ask.{shared,identity,business,item,service,search,request,messaging}`
Each feature: `api/` `application/` `domain/` `infrastructure/`
No global `controller/`, `service/`, `repository/`, `dto/`, `mapper/` packages.

## Layer direction
```
Controller → Processor → DomainService → Repository
```
- Controller: validates transport shape, returns `ResponseEntity<T>`, delegates to Processor
- Processor: orchestrates use cases, cross-domain flow. Works ONLY with DTOs via domain service interfaces. Never touches entities or mappers.
- DomainService: owns business logic and entity persistence. Interface + Impl in same package. Never calls Processor. Never returns Entity to another layer.
- Mapper: Entity ↔ DTO only. No DB calls, no API calls, no business logic. Called ONLY by service impls.
- Repository: persistence only.

Forbidden: Controller→Repository, Mapper→Service/Processor/Repository, Service→Processor, Service→Service cycles.

## Naming
- Services: `{Domain}Service` / `{Domain}ServiceImpl` — business logic, @Transactional
- Processors: `{Domain}Processor` — orchestrate multiple services
- Controllers: `{Domain}Controller` — REST endpoints
- Mappers: `{Domain}Mapper` — Entity ↔ DTO, pure field copying. One mapper per domain package. Hand-written only, no MapStruct.
- DTOs: `*Request` (incoming), `*Response` (outgoing), `*Dto` (internal transfer). `@Builder` on all. Standalone files — no nested classes.

## Domain service and DTO identity
- A domain service exists only for exactly one entity and has the exact name `{Entity}Service` / `{Entity}ServiceImpl`. `Item` therefore uses `ItemService`; a differently named service is not a domain service.
- A `*Dto` is the general DTO for exactly one entity and copies that entity's complete domain shape. Its name is `{Entity}Dto`.
- `*Request` is transport input and `*Response` is transport output. `*ListResponse` wrappers are allowed for collection endpoints. Other composite DTOs (`*Result`, `*Item`, `*Batch`, `*Payload`, `*Document`, and ad-hoc context DTOs) remain forbidden.
- A current caller is not a reason to keep a service or DTO. Keep it only when an active product document owns the behavior; then rename it to the allowed component type or move the behavior into its owning entity service.

## Entities
- Every entity extends `BaseUuidV7Entity`. UUIDv7 assigned before first persist.
- Audit: `createdAt`, `updatedAt`. Lombok `@Getter`/`@Setter`, no `@Data`.
- Entities stay inside domain package, never returned from API.
- Use enums for statuses. `FetchType.LAZY` for entity references.
- No primitive types — use wrappers (`Integer`, `Boolean`, `Double`).
- No concrete collection types in declarations — use interfaces (`List`, `Map`).
- No records/classes inside interfaces. No static inner classes in DTOs.
- No anonymous classes (including `new TypeReference<>() {}`).

## Service rules
- Interface must not expose entities. Return DTO copies only. Accept UUIDs and DTOs only.
- Each Service owns one entity type. Even slight naming differences → separate services.
- ServiceImpl: inject only own entity's repository. For other entities in same domain → call that entity's domain service.
- `getReferenceById()` is the ONLY allowed cross-entity repository access (JPA proxy for relationships).
- Services never set entity fields manually → delegate to Mapper. Composite result DTOs use `@Builder` directly in impl.
- `@Transactional` on mutation methods. `@Transactional(readOnly = true)` allowed on read queries where the caller benefits from Hibernate session consistency.
- Use `@RequiredArgsConstructor`. Never throw `RuntimeException` — use `kz.ask.shared.error` hierarchy.
- Never call `findAll()` — always filtered query methods with specific criteria.

## Derived decisions and AI
- A business decision must be represented by one explicit decision result with outcome, reason code, and evidence. Do not encode a growing set of domain examples as sequential `if` branches in a processor or service.
- Contextual cases are inputs to a policy or enrichment decision, not separate execution flows. For example, a possible weapon in a toy context can require review without creating a toy-specific moderation branch.
- AI is advisory. It may return proposed structured data, aliases, or query interpretation, but it never selects businesses, changes the user-selected search scope, invents operational facts, or mutates canonical entities directly.
- Generic search must not hardcode one vertical's vocabulary. Curated vocabulary belongs to an owned policy/data source with documented scope, provenance, and removal criteria.
- Create an AI class only with its real consumer in the same change. Empty role folders, placeholder enrichers, and speculative adapters are unused code.

## Controller rules
- Every method returns `ResponseEntity<T>` from `org.springframework.http.ResponseEntity`.
- Status: `ResponseEntity.ok()`, `.status(HttpStatus.CREATED).body()`, `.noContent().build()`.
- Never `@ResponseStatus` — status comes from ResponseEntity.
- Never return raw entities.

## Error handling
Unified hierarchy in `kz.ask.shared.error`: `BusinessException` base → `NotFoundException`(404), `ValidationException`(400), `ConflictException`(409), `ForbiddenException`(403), `UnauthorizedException`(401), `AuthException`(401), `InternalServerException`(500), `ExternalServiceException`(502).
Error codes in `ErrorCode` enum with Russian message templates. One global handler: `GlobalExceptionHandler`. Never catch + rewrap. Never create feature-specific exceptions.

## Product MVP Rule
One concrete sellable variation = one `Product`. Search grouping via `Product.tags` + `SearchDocument`. No `ProductVariant`, `ProductAttributeDefinition`, or variant tables.

## Service MVP Rule
`ServiceResource` is optional abstract capacity. `ON_DEMAND` offers work without resources/schedules. `SCHEDULED` offers may use resources/schedules/booking. `Booking` ≠ `CustomerRequest`.

## Verification
Do not create tests. Do not run Maven unless explicitly asked. Use file review, git diff, dependency/FK review.
