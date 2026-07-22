# Product Catalog

Product CRUD, item offers per branch, categories, and search document sync. One concrete sellable variation = one Product entity.

## Key decisions
- One item variation = one Product entity. No variant tables.
- A published item has one curated category and one canonical JSONB attributes map. Description and tags supplement it.
- Product visibility: ProductOffer.enabled toggles live search appearance.
- ProductOffer is the branch relationship: it owns branch-specific price, visibility, publication, and source.
- Duplicating a item to another branch creates a new ProductOffer with the source offer price copied as its initial value.
- No stock tracking, availability confidence, or freshness tracking in MVP.
- Product deletion: disabled/deleted products must not appear in live search.
- Category: category_id nullable; category_label is the primary category field for imports.
- Platform AI may propose attributes only during assigned managed import; approved catalog facts remain canonical.
