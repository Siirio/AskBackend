# Product Catalog

Product CRUD, product offers per branch, categories, and search document sync. One concrete sellable variation = one Product entity.

## Key decisions
- One product variation = one Product entity. No variant tables.
- Search grouping via Product.tags + SearchDocument, not variant attributes.
- Product visibility: ProductOffer.enabled toggles live search appearance.
- Products linked to branches via M2M product_branch join table.
- No stock tracking, availability confidence, or freshness tracking in MVP.
- Product deletion: disabled/deleted products must not appear in live search.
- Category: category_id nullable; category_label is the primary category field for imports.
- Product characteristics stored as JSONB (characteristics_json).
