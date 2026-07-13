# Unique Offers

Brand signals and ranking boosters — NOT standalone search results. Boost linked products/services in search (+25 score). DISCOUNT offers compute effective price. Non-DISCOUNT offers display as brand signal labels.

## Key decisions
- Formerly "BrandDrops" — renamed to UniqueOffer in V2 restructuring.
- Linked to products, services, AND branches via M2M join tables.
- One offer can link to all three simultaneously.
- Can be toggled (enabled/disabled) without deletion.
- Offer types: DISCOUNT, NEW_COLLECTION, LIMITED_RELEASE, RESTOCK, CAPSULE, SEASONAL, COLLAB, PREORDER.
- Offer statuses: UPCOMING, ACTIVE, ENDED, CANCELLED.
- DISCOUNT: discount_percent (INTEGER) or discount_amount (NUMERIC) → computes effectivePrice.
- Non-DISCOUNT: shows offer name as label on search result card.
- Active offers linked to product/service's business → +25 score boost.
