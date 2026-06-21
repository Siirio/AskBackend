# Architecture Narrative

Ask is a search-first product for finding products and services from real local businesses. The current MVP is not a broad business search engine and not a request-only broadcast app.

The target customer flow is:

```text
Customer chooses product or service search
  -> enters raw query
  -> sees enabled products or enabled services from registered branches
  -> opens product/service/business context, chat, or creates fallback request
```

## Current Product Direction

- Customer search has two primary scopes: products and services.
- Business pages open from context, not from a separate business search.
- Categories narrow product/service search.
- Raw query is preserved everywhere.
- Dynamic filters are based on actual result attributes.
- Fallback requests exist when no suitable result exists or when the customer wants businesses to confirm manually.

## Business Onboarding Direction

Business onboarding is production-facing. It is not a mock-only flow.

The immediate goal is to register real stores/branches before the client app launch so customer search has real data to show. A business registration currently means one concrete branch/store/establishment joins Ask.

After registration:

- the branch profile persists in the real database;
- the branch can add products;
- the branch can add services;
- enabled products and services appear in customer search;
- disabled or deleted products and services stop appearing in live search.

A future account can manage multiple branches, but each branch must have its own contacts and its own products/services. Do not collapse all future branch contacts into one company-wide registration contact.

## One Backend, Many Clients

AskBackend is the single backend for Android, iOS, web, and desktop/PWA clients.

Frontend owns presentation, navigation, and local UI state. Backend owns:

- identity and real contact verification;
- persisted business onboarding data;
- product and service records;
- search indexing;
- branch ownership and contacts;
- request routing;
- chat context;
- data retention policy.

## Product Catalog MVP

Products are real business-entered or imported items.

Current MVP rules:

- one concrete sellable item is one `Product`;
- a product belongs to a business and is offered by a concrete branch through `ProductOffer`;
- product visibility is controlled by enabled/disabled/deleted behavior;
- inventory counting is outside the MVP;
- separate data-freshness tracking is outside the MVP;
- product visibility is driven by business enable/disable/delete actions;
- if the customer needs confirmation, use clarify action, Ask chat, or fallback request.

## Services MVP

Services are separate from products.

Current MVP rules:

- a service belongs to a business and is offered by a concrete branch;
- service visibility is controlled by active/inactive behavior;
- service requests are request-to-book, not guaranteed slot reservations;
- the customer can choose desired time;
- business confirms final time, declines, or proposes another time;
- MVP does not model full staff/calendar slot blocking.

## Search And Distance

Search ranking should be smart and practical. It can use query matching, category, result attributes, enabled state, price, and distance when distance is known. The public contract should stay focused on the visible product/service result and its branch context.

`distanceMeters` is calculated only when:

- customer geolocation is provided;
- branch coordinates exist;
- backend calculates distance from customer coordinates to branch coordinates.

If coordinates are missing, return `distanceMeters=null`.

## Data Truth

Ask must not invent facts.

Allowed MVP facts:

- enabled product exists in branch catalog;
- active service exists in branch service list;
- business-provided price when present;
- business-provided branch address and contacts;
- calculated distance when coordinates exist;
- business response status when business answered.

Not tracked in MVP:

- inventory counting;
- separate data-freshness tracking;
- separate scoring fields for availability;
- automatic delivery SLA;
- courier availability;
- guaranteed service slot availability.

## Persistence And Deployment Direction

Once the site is given to real businesses for onboarding:

- database must be persistent;
- data must not be dropped casually;
- frontend, backend, and database should be deployable together;
- seed/demo data must not replace real business registrations.

Render or another deployment target can host the app, but provider-specific setup belongs to deployment tasks, not product task DTOs.
