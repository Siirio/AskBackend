# Architecture Narrative

Ask began as a practical request-routing idea, but the current product strategy is search-first. Ask is a local search platform where a customer first sees found products or services across city businesses.

The deeper product insight is that request broadcasting alone is not enough. If Ask never learns how to work with supplier data, it stays a messenger-like tool. The stronger product is a local search and availability platform: a system that understands businesses, products, services, catalogs, branches, attributes, schedules, and integrations, while using manual requests as confirmation fallback when data is missing or uncertain.

## From Request Routing To Search-First Platform

The target flow is:

```text
Customer search -> product/service/business results -> clear source and availability confidence
  -> fallback request to suitable suppliers when exact data is missing
```

Manual request routing remains useful because it handles missing catalogs, stale availability, uncertain services, and supplier confirmation. But it is no longer the only core flow. Suppliers should not manually answer every obvious availability question forever if they already have data somewhere else.

The next technical goal is not merely "add product CRUD." The goal is business data ingestion, search quality, and a fallback request path that activates only when known data is insufficient.

## One Backend, Many Clients

AskBackend must be the single backend for all official clients. Android, iOS, and a future website must use the same backend API. The backend should not fork into separate implementations per client, and business rules must not depend on whether the request came from mobile or web.

The intended client shape is:

```text
Android UI
iOS UI
Web UI
  -> shared client/API abstraction
  -> AskBackend API
```

Each frontend can have platform-specific UI, navigation, and local state, but heavy product logic must stay out of separate UI implementations. The shared client/API abstraction should isolate communication with AskBackend from design and presentation. Backend owns search data truth, catalog processing, availability confidence, request fallback, service-provider data, permissions, and integration boundaries.

## Catalog Is A System, Not A Table

Many suppliers, especially early local suppliers, may have data in Excel, CSV, MoySklad, POS systems, e-commerce exports, CRM tools, or custom spreadsheets. The backend must treat catalog work as a data pipeline:

- importing raw files, especially Excel and CSV, or provider data;
- mapping columns;
- preserving raw source rows;
- normalizing names;
- detecting duplicates;
- mapping categories;
- storing flexible attributes;
- tracking branch-level price or availability;
- measuring freshness;
- allowing supplier corrections;
- searching rough, incomplete, multilingual, or misspelled queries.

Catalog-backed search is a core product path. Manual request routing must still work for missing, stale, or uncertain data, but catalog should not be treated as a distant optional improvement.

Catalog import must be seller-friendly. Shops should not have to fill the same catalog manually inside Ask if they already maintain Excel or CSV exports. The system should support upload, preview, column mapping, validation, correction, import history, and repeated updates.

## Smart Search

Smart Search is the primary customer discovery path. It should not collapse into a rigid product picker. A customer may describe a need imprecisely. The system should connect the search intent with categories, products, services, attributes, aliases, supplier catalogs, and fallback manual outreach.

When catalog confidence is low, Ask should route the request for confirmation rather than invent availability.

## Services Are A Separate Direction

Ask should support services in the future. A user may choose whether they are looking for a product or a service.

Products are physical items. Services involve time and capacity. A service model may need:

- service providers;
- branches;
- specialists or resources;
- schedules;
- free windows;
- durations;
- price rules;
- confirmation;
- cancellation;
- source of availability;
- provider integrations.

Services cannot be modeled as products with a different label. Before coding service search or booking, the team should write a system analysis covering source of truth, availability updates, integration options, MVP shortcuts, and scaling risks.

For service providers, a mobile-only management flow is likely too heavy. Managing service offerings, schedules, free windows, discounts, conditions, specialists, and branches is better suited to a web cabinet. The website direction is therefore not a general replacement for mobile apps; it is primarily a provider workspace for establishments that need comfortable service administration.

## Backend Architecture Direction

The backend should stay a monolith until there is evidence that splitting services is worth the cost.

Feature-Sliced Design is a frontend methodology. Do not copy it literally into Spring Boot: frontend layers such as `app`, `pages`, `widgets`, `features`, `entities`, and `shared` are not backend package names.

AskBackend should use the backend equivalent of the same slicing idea: package-by-feature, modular monolith, domain modules, DDD-style modules, and clean/hexagonal architecture boundaries. The system should be grouped by product capability and bounded domain area, not only by technical layer.

Default backend direction:

- Java 21;
- Maven;
- Spring Boot;
- Spring MVC;
- PostgreSQL;
- Flyway;
- Spring Data JPA;
- Spring Security;
- OpenAPI.

Non-trivial workflows should move toward:

```text
Controller -> Processor or UseCase -> DomainService -> Repository
```

Controllers should validate request shape, call application boundaries, and return `ResponseEntity`. Domain services own business logic. Repositories own persistence. DTOs define API contracts. Mappers and assemblers stay pure.

Feature/domain modules should make related code easy to find. Search, catalog import, service scheduling, request fallback, supplier onboarding, response handling, and chat should each have clear local ownership. Avoid a structure where every feature is split across broad global buckets so that one change requires jumping through many unrelated files.

Recommended module shape:

```text
request/
  api/
  application/
  domain/
  infrastructure/

store/
  api/
  application/
  domain/
  infrastructure/

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

Backend modules may contain API DTOs/controllers, use cases/processors, domain services, repositories, mappers, and feature-specific configuration when needed. Shared primitives, cross-cutting infrastructure, security, common errors, and integration abstractions belong in shared or infrastructure areas. Cross-module dependencies must be explicit and interface-based.

## Integration Boundaries

Core business logic must not depend directly on Telegram, WhatsApp, Paloma, 1C, re:Kassa, Shopify, MoySklad, POS, CRM, fiscal systems, e-commerce systems, or scheduling systems. These are adapters or providers.

Real provider calls require explicit scope, credentials, documentation, and explicit approval. Placeholders may exist, but they must not pretend to have real provider behavior.

## Mobile And Frontend Direction

The future product is mobile-first. Native mobile apps may become the primary clients. Browser tools and prototypes can exist, but they should not define backend architecture.

Frontend and backend can be separate repositories owned by different developers. The shared contract is product meaning plus stable APIs, not one old prototype implementation.

The mobile application has two product sides: customer and seller/supplier. The website is planned mainly for service-providing establishments that need to manage larger service data and scheduling. All of these clients still go through the same backend.

## Scaling Direction

Ask may start in one city and expand to more cities, Kazakhstan, CIS, or other markets. Avoid hardcoding:

- one city;
- one language;
- one category;
- one supplier type;
- one spreadsheet shape;
- one provider;
- one frontend;
- one deployment vendor.

The architecture should be practical, not enterprise theater. But it must avoid decisions that make growth impossible.

## Data Truth

Ask must not invent facts.

Search results can show known products, services, businesses, prices, branches, and availability confidence only when the backend has a trustworthy source. Manual replies can contain status, price, comment, branch address, contact actions, and explicit supplier notes. Exact stock quantity, delivery SLA, courier availability, automatic availability, service slots, and booking promises require supplier input or real integration data.

If the source is weak, Ask should say confirmation is needed.
