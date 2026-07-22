# Domain Foundation Design

**Status:** Approved in conversation on 2026-07-21; implementation review pending.

## Goal

Make one canonical owner responsible for every business, catalog, membership, chat, and search fact. Remove request-routing behavior from the catalog search path.

## Product flow

```text
customer selects GOODS or SERVICES
-> enters raw query
-> backend preserves raw query
-> AI structures only that selected scope
-> bounded search returns published branch offers
-> customer explicitly opens the seller's business conversation
-> every authorized business member sees the shared customer history
```

Search never creates a request, recipient list, supplier outreach, notification, or chat. AI never changes the user-selected scope, selects sellers, or invents catalog facts.

## Bounded contexts and canonical ownership

### Business

`Business` is the legal and lifecycle root: country code, legal name and identifier, business state, moderation, block, and soft-delete lifecycle. Public presentation belongs to the business public profile. Delivery policy is business-wide by default and may be overridden by a branch later; onboarding must disclose that rule.

`BusinessBranch` owns a concrete location and its local operating state. It does not own customer conversations.

### Membership

`BusinessMembership` owns a user's default role for a business. The default applies to every current and future branch. A separate branch-access override may either assign a different role or deny access for one branch. Multiple `OWNER` memberships are valid. Owners may manage every role, managers may manage workers only, and workers cannot manage staff.

### Catalog

`Product` and `ServiceOffering` are business-owned canonical definitions. Each published item has one curated category and one canonical JSONB `attributes` map. Free-text description and tags supplement attributes; a second characteristics representation is not canonical.

`ProductOffer` and `ServiceBranchOffer` own branch-specific price, publication, visibility, and source. Duplicating a catalog item to another branch creates another offer and copies the source offer's price as its initial value. A bare item-to-branch or service-to-branch join without offer data is not part of the target model.

Platform-managed AI import may propose and populate item or service attributes during the managed seven-day import window. It never changes approved human-entered canonical facts without an explicit business/platform edit.

### Chat

One `ChatConversation` is identified by `businessId` and `customerId`, not by branch, item, service, request, or booking. All active members with business access share its customer history. A card's branch or offer is an entry point into that conversation, not a second conversation key. Date separators are presentation derived from message timestamps.

The initial inbox state is business-wide: when one authorized business member reads a conversation, the business-side unread count is cleared. Per-member read state is deferred until a real requirement exists.

### Search

`SearchDocument` remains a derived, rebuildable search projection. Its denormalized lexical and card fields are deliberate. PostgreSQL canonical catalog records remain the source of truth. Search receives a frontend-selected scope, returns only matching published offers, and preserves the visible raw query unchanged.

### Retired request-routing model

`CustomerRequest`, `RequestTarget`, and `SupplierResponse` do not participate in the approved search or chat flow. Their endpoints, tables, docs, and dependencies are retirement candidates. They must not be repurposed as chat or inbox records.

## Fresh-baseline schema direction

The V1 baseline is a fresh-deploy schema and may be rewritten. The target baseline removes retired request-routing tables and obsolete bare catalog-to-branch joins, models business-wide conversations with a unique business/customer key, separates business concerns, and represents branch access as explicit overrides over a business-level default role. No production data operation is authorized by this design.

## Non-goals

- Automatic supplier outreach or request broadcasts.
- AI selection of sellers, scope overrides, stock claims, or availability claims.
- Dynamic attribute-definition tables or item variants.
- Per-staff chat read receipts.
- Payment, order, purchase, or loyalty analytics.

