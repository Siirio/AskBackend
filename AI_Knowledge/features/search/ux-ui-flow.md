# Search — Frontend UX Expectations

## Search flow
1. Customer types natural-language query (any language, slang, typos)
2. Backend AI structures query internally — raw query preserved
3. Results returned sorted by intent_match score
4. No scope toggle — AI determines if this is a product or service search

## Result cards (anti-marketplace)
Each card has two layers:
- Brand layer: brandId, businessName, brandLogoUrl, brandCoverUrl, brandColor, brandDescriptor, badges
- Decision layer: matchReasons, availabilityStatus, confirmationStatus, pickupOptions, branchContext, distanceText, requiresSupplierCheck, availableActions

Badges (visible signals, not ratings): data freshness, confirmation speed, card quality, business activity
Internal score NEVER rendered as customer-facing trust.

## Search result sections
Frontend groups results:
- FOUND: exact/similar catalog results
- SUPPLIER_CHECK: auto-selected suppliers and their response status (NOT customer-visible chat)
- CHATS: only real conversations appear here
- OVER_BUDGET / WRONG_CITY: clearly labeled fallback sections

## Distance
- distanceMeters returned only when calculated from real coordinates (customer lat/lon + branch lat/lon)
- Nullable — missing coordinates → null
- Never calculated from city name or address text
