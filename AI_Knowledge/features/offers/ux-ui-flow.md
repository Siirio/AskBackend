# Unique Offers — Frontend UX Expectations

## Business cabinet
- Offers tab: create and manage promotional offers
- Create form: name, description, type (DISCOUNT/NEW_COLLECTION/etc.), dates, uploaded cover image, linked items/services/branches. Discount percent and amount are optional and appear only for `DISCOUNT`.
- Toggle enabled/disabled independently from edit
- Offer card shows: status (UPCOMING/ACTIVE/ENDED/CANCELLED), linked item count, dates

## Customer-facing
- DISCOUNT offers: price shown with strikethrough original + "-30%" label
- Non-DISCOUNT offers: offer name shown as badge on result card (e.g., "NEW COLLECTION")
- Offers boost ranking (internal signal, not customer-facing score)
