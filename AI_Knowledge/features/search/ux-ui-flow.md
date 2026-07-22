# Search — Frontend UX Expectations

## Search flow
1. Customer selects products or services and types a natural-language query (any language, slang, typos)
2. Backend AI structures the selected scope internally — raw query preserved
3. Results returned sorted by intent_match score
4. The frontend-selected scope remains fixed; AI cannot switch it

## Result cards (anti-marketplace)
Each card has two layers:
- Brand layer: brandId, businessName, brandLogoUrl, brandCoverUrl, brandColor, brandDescriptor, badges
- Decision layer: matchReasons, availabilityStatus, pickupOptions, branchContext, distanceText, availableActions

Badges (visible signals, not ratings): data freshness, confirmation speed, card quality, business activity
Internal score NEVER rendered as customer-facing trust.

## Search result sections
Frontend groups results:
- FOUND: exact/similar catalog results
- Customer contact opens or resumes the business conversation from an offer card; it is not created by search itself
- OVER_BUDGET / WRONG_CITY: clearly labeled fallback sections

## Distance
- distanceMeters returned only when calculated from real coordinates (customer lat/lon + branch lat/lon)
- Nullable — missing coordinates → null
- Never calculated from city name or address text
- AI enrichment controls are platform-only and support one item from edit mode or a bulk item selection.
