# Catalog — Feature Locks

LOCKED | One concrete variation = one Product entity | No variant tables (ProductVariant, ProductAttributeDefinition) | Product entity, catalog domain
LOCKED | No stock tracking, availability confidence, or freshness fields | MVP uses enabled/disabled toggle only | ProductOffer, product_offer table
LOCKED | ProductOffer.enabled is the live-search toggle | No separate scoring fields for visibility | SearchDocument sync, product search
LOCKED | Products can link to multiple branches via M2M | product_branch join table, not single branch_id FK | Product entity, ProductOffer
