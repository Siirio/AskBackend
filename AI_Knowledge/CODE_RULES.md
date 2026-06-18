# AskBackend Code Rules

This document keeps project-specific code restrictions and implementation rules for fresh AI sessions and developers.

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
- DomainService owns business logic and entity persistence.
- Repository owns persistence only.
- Mapper converts Entity and Dto only.
- Assembler converts Dto to Response only.
- Client packages talk to external systems only when explicitly approved.

Forbidden:

- Controller calling Repository.
- Mapper calling Service, Repository, Processor, Client, or Validator.
- DomainService calling Processor.
- DomainService returning Entity to another layer.
- Service-to-service cycles.
- Real external calls without explicit scope, credentials, provider docs, and approval.

## DTO Types

Use only these DTO families by default:

- `*Request` for incoming API payload.
- `*Response` for outgoing API payload.
- `*Dto` for internal feature transfer.

No nested DTO classes. Create standalone DTO files when DTOs exist.

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
