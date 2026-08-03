# Unique Offers — REST API Contracts

## Business Admin (Drops)

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /api/v1/businesses/{businessId}/drops | No (public) | List active and upcoming drops |
| POST | /api/v1/businesses/{businessId}/drops | OWNER | Create drop |
| PATCH | /api/v1/drops/{dropId} | OWNER | Update drop (derives businessId from entity) |
| POST | /api/v1/drops/{dropId}/cover | OWNER | Upload and replace the ASK-managed cover image |
| POST | /api/v1/drops/{dropId}/cancel | OWNER | Toggle isActive (derives businessId from entity) |
| DELETE | /api/v1/drops/{dropId} | OWNER | Delete drop (derives businessId from entity) |

## UniqueOffer Model
- id, business_id, name, description, type (UniqueOfferType enum), status (UniqueOfferStatus enum)
- discount_percent (INTEGER), discount_amount (NUMERIC), currency (VARCHAR(3))
- is_active (BOOLEAN), tags (JSONB), startDate, endDate, server-generated coverUrl
- M2M: unique_offer_product, unique_offer_service, unique_offer_branch

Create and update JSON requests do not accept `coverUrl`. The cover is sent as a validated PNG, JPEG, or WebP multipart file after the Unique Offer exists.

## Search Integration
- Active, date-valid Unique Offers boost only their linked Items or Services.
- A branch-restricted offer applies only when the result uses one of its linked branches.
- Search exposes `hasActiveOffer`; active-offer-first sorting is available through `sort=unique_offers`. Offer names and discounts are not mixed into the stable badge-token list.
- The offer never becomes a standalone result or search scope.
