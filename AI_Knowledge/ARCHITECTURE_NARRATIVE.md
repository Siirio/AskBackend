# Architecture Narrative

Ask began as a practical request-routing idea: a customer asks for a product, Ask sends the request to relevant stores, and stores reply manually. That remains a valid MVP because it proves whether customers and suppliers get value before expensive integrations exist.

The deeper product insight is that request broadcasting alone is not enough. If Ask never learns how to work with supplier data, it stays a messenger-like tool. The stronger product is an availability platform: a system that can route manually at first, then gradually understand catalogs, branches, attributes, availability, services, schedules, and integrations.

## From Manual Routing To Availability Platform

The early flow is:

```text
Customer request -> relevant suppliers -> manual supplier replies -> customer compares replies
```

That flow is useful because it reduces customer effort and gives suppliers demand. But it does not scale by itself. Suppliers should not manually answer every obvious availability question forever if they already have data somewhere else.

The next technical goal is not merely "add product CRUD." The goal is supplier data ingestion and search quality.

## Catalog Is A System, Not A Table

Many suppliers, especially early local suppliers, may have data in Excel, MoySklad, POS systems, e-commerce exports, CRM tools, or custom spreadsheets. The backend must treat catalog work as a data pipeline:

- importing raw files or provider data;
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

Manual request routing must still work before catalog is mature. Catalog should improve routing and availability confidence over time, not block supplier onboarding.

## Smart Search

Smart Search is the primary customer discovery path. It should not collapse into a rigid product picker. A customer may describe a need imprecisely. The system should gradually learn to connect the request with categories, products, attributes, aliases, supplier catalogs, and fallback manual outreach.

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

## Backend Architecture Direction

The backend should stay a monolith until there is evidence that splitting services is worth the cost.

Default backend direction:

- Java 21;
- Maven;
- Spring Boot;
- Spring MVC;
- PostgreSQL;
- Flyway;
- Spring Data JPA;
- Spring Security;
- OpenAPI;
- JUnit 5.

Non-trivial workflows should move toward:

```text
Controller -> Processor or UseCase -> DomainService -> Repository
```

Controllers should validate request shape, call application boundaries, and return `ResponseEntity`. Domain services own business logic. Repositories own persistence. DTOs define API contracts. Mappers and assemblers stay pure.

## Integration Boundaries

Core business logic must not depend directly on Telegram, WhatsApp, Paloma, 1C, re:Kassa, Shopify, MoySklad, POS, CRM, fiscal systems, e-commerce systems, or scheduling systems. These are adapters or providers.

Real provider calls require explicit scope, credentials, documentation, and tests. Placeholders may exist, but they must not pretend to have real provider behavior.

## Mobile And Frontend Direction

The future product is mobile-first. Native mobile apps may become the primary clients. Browser tools and prototypes can exist, but they should not define backend architecture.

Frontend and backend can be separate repositories owned by different developers. The shared contract is product meaning plus stable APIs, not one old prototype implementation.

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

Manual replies can contain status, price, comment, branch address, contact actions, and explicit supplier notes. Exact stock quantity, delivery SLA, courier availability, automatic availability, service slots, and booking promises require supplier input or real integration data.

If the source is weak, Ask should say confirmation is needed.
