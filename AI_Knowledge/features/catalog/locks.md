# Catalog — Feature Locks

LOCKED | One concrete variation = one Product entity | No variant tables (ProductVariant, ProductAttributeDefinition) | Product entity, catalog domain
LOCKED | One published item has one curated category and one canonical attributes map | Description and tags supplement attributes; a second characteristics representation is not canonical | Product, catalog mappers, import flow
LOCKED | No stock tracking, availability confidence, or freshness fields | MVP uses enabled/disabled toggle only | ProductOffer, product_offer table
LOCKED | ProductOffer.enabled is the live-search toggle | No separate scoring fields for visibility | SearchDocument sync, item search
LOCKED | ProductOffer is the only branch relationship | Every branch listing owns offer data such as price and visibility; no bare product_branch join is allowed | ProductOffer, catalog schema
