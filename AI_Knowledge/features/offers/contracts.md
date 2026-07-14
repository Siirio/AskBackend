# Unique Offers — REST API Contracts

## Business Admin (Drops)
Base: /api/v1/businesses/{businessId}/drops

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /drops | OWNER | List drops |
| POST | /drops | OWNER | Create drop |
| PATCH | /drops/{dropId} | OWNER | Update drop |
| POST | /drops/{dropId}/cancel | OWNER | Cancel drop |
| DELETE | /drops/{dropId} | OWNER | Delete drop |

## Public
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /api/v1/businesses/{businessId}/drops | No | List active and upcoming drops |

## UniqueOffer Model
- id, business_id, name, description, type (UniqueOfferType enum), status (UniqueOfferStatus enum)
- discount_percent (INTEGER), discount_amount (NUMERIC), currency (VARCHAR(3))
- enabled (BOOLEAN), tags (JSONB), startDate, endDate, coverUrl
- M2M: unique_offer_product, unique_offer_service, unique_offer_branch

## Search Integration
- findBestOffer(businessId) → matches active UniqueOffers linked to business's products/services
- DISCOUNT: effectivePrice = price * (1 - percent/100) or price - amount
- offerLabel: "-30%" for percent, "-5000 ₸" for amount, offer name for non-DISCOUNT
- Boost: +25 score for results linked to active offers
