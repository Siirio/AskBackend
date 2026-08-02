# Items

Item CRUD, optional branch association, flat ITEM categories, and search-document sync. One concrete sellable variation is one Item entity owned by a Business.

## Key decisions
- One item variation = one Item entity. No variant tables.
- An Item belongs to a Business and can be created before any branch exists.
- An Item has exactly one flat `ITEM` category. The category can be `SYSTEM` or explicitly created by a user.
- Description, labeled purchase destinations, tags, and canonical attributes supplement the category.
- Purchase destinations belong to the Item, not to a branch. An Item may list multiple seller-published customer destinations so the customer can choose where to buy.
- A later branch association owns only location-specific facts such as price and visibility; it is optional.
- No stock tracking, availability confidence, or freshness tracking in MVP.
- Item deletion: disabled Items must not appear in live search.
- Category identity, not a free-form `category_label`, is canonical. Imports may propose a category; user approval selects or explicitly creates it.
- Platform AI may propose attributes only during assigned managed import; approved Item facts remain canonical.
- Item creation publishes immediately: auto-moderation synchronously assigns `APPROVED` unless a prohibited keyword assigns `REJECTED`.
- Business Item lists are ordered newest first so a created Item remains visible after reload.
