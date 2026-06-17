# Ask

Ask is a request-routing, availability, catalog, and service discovery platform.

The first useful version is simple: a customer describes what they need, Ask routes the request to relevant suppliers, and suppliers answer manually with availability, price, address, contact options, or clarification. That manual MVP is valuable because it reduces the customer's search effort before deep integrations exist.

The long-term product is broader. Ask should become a reliable availability layer between customers and stores or service providers. That requires supplier onboarding, catalog import, data normalization, Smart Search, clear response rules, provider outreach, and integration boundaries for systems that suppliers already use.

## The Problem

Customers often need something now but do not know where it is actually available. They may search several marketplaces, call shops, write to messengers, compare incomplete listings, and still discover that the product is unavailable or the service has no free time.

Suppliers have the opposite problem. They want real demand, but they may not have a clean public catalog, a modern booking system, or time to maintain another heavy admin panel. Many early suppliers may already use Excel, MoySklad, POS tools, e-commerce exports, messengers, CRM systems, or manual workflows.

Ask should connect those two sides without pretending that perfect data exists on day one.

## Why Ask Is Not Just Another Marketplace

A classic marketplace usually starts with a controlled catalog. Ask starts from availability and demand:

- the customer asks in natural language;
- Ask finds the right supplier or provider path;
- the supplier can answer manually at first;
- catalog and integration quality improves over time;
- automatic replies become valid only when real data supports them.

Ask should not force every supplier into complete catalog migration before the product is useful. It should let the MVP work manually while building toward better data.

## Product Direction

Ask grows in layers:

1. Manual request routing.
2. Stable supplier, branch, contact, category, and response models.
3. Product catalog import from Excel, CSV, MoySklad, POS, e-commerce, or other sources.
4. Data normalization for product names, categories, attributes, prices, branches, and freshness.
5. Smart Search over rough customer queries, categories, attributes, aliases, and availability signals.
6. Integration-backed automatic availability where real provider data exists.
7. Service discovery for appointments, schedules, free windows, specialists, branches, confirmations, and cancellations.

## Target Product Architecture

AskBackend is one backend for all clients. Android, iOS, and any future website must use the same backend API. Do not create a separate backend for each frontend implementation, and do not make backend business logic depend on Android, iOS, or web UI differences.

Client applications should share the same contract model:

```text
Android UI
iOS UI
Web UI
  -> shared client/API abstraction
  -> AskBackend API
```

Frontend implementations may have different visual UI and platform behavior, but they should not duplicate heavy business logic. Backend owns request routing, data truth, catalog processing, service-provider data, permissions, and integration boundaries. Client layers adapt backend DTOs into view models and handle platform-specific presentation.

## Catalog And Services Direction

Product sellers usually already have product data in files or systems. The backend must support practical import paths, especially Excel and CSV, so sellers do not have to manually recreate catalogs from scratch. Catalog work should include file upload, column mapping, validation, normalization, duplicate handling, category/attribute mapping, branch-level data, import history, and data-quality feedback.

Services have a different operational shape. Service providers need to manage offerings, schedules, free windows, discounts, conditions, specialists, branches, confirmations, and cancellations. Doing that only inside a mobile app can become overloaded and inconvenient. The planned direction is a web cabinet for service providers, so they can manage larger service datasets and availability more comfortably.

Current product surface direction:

- mobile application has two sides: customer and seller/supplier;
- website is primarily for establishments that provide services and need a larger workspace for managing service offerings, schedules, windows, discounts, and conditions;
- product-catalog bulk import must support Excel and CSV workflows;
- future web surfaces may also support bulk product catalog operations if that becomes the most usable supplier workflow, but this should remain API-backed and not become a separate backend.

## Current Foundation Scope

This repository is a foundation for future Ask backend and AI-assisted development. It preserves the product vision, architecture rules, workflow rules, and implementation pipeline that new developers and Codex agents should load before coding.

It is not an old prototype dump and not a local-machine-specific Codex export.

## Important Documents

- `FIRST_READ_THIS.md`: start here when a new person or Codex agent opens the repo.
- `ARCHITECTURE_NARRATIVE.md`: technical story and architecture direction.
- `AGENTS.md`: rules for AI agents and developers.
- `CODEX_PLAYBOOK.md`: compact task router for backend, frontend, catalog, services, integrations, and MCP usage.
- `IMPLEMENTATION_PIPELINE.md`: how to keep extending this foundation safely.
- `SELF_AWARE_ORIGIN.md`: how to reason about foundation decisions without over-citing old context.
- `FOUNDATION_AUDIT.md`: what kind of material belongs in the foundation and what should be excluded.
- `skills/`: focused skill docs that can become installable Codex skills if needed.
- `mcp/README.md` and `plugins/README.md`: tool guidance without secrets or machine-specific config.
- `DEPRECATED_WEB_STAGING_NOTES.md`: archive-only notes about old browser-staging lessons.
- `CHANGELOG_FOUNDATION.md`: what changed in this foundation.

## Non-Goals

- Do not hardcode Ask around one city, one store, one Excel file, one frontend, one old prototype, or one provider.
- Do not claim catalog, inventory, service slots, delivery, or logistics facts unless a supplier or real integration provides them.
- Do not copy local Codex configs, tokens, auth files, sqlite state, generated caches, plugin caches, or runtime paths into this repo.
- Do not make browser web staging the architecture of the future backend.

## Working Principle

Ask should stay simple enough to launch, but structured enough that launch decisions do not block future catalog, integration, services, mobile, and multi-city growth.
