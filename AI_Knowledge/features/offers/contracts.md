# Unique Offers — REST API Contracts

## Business Admin
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /api/v1/business-admin/offers | OWNER | Create offer |
| GET | /api/v1/business-admin/offers | OWNER | List offers |
| PATCH | /api/v1/business-admin/offers/{id} | OWNER | Update offer |
| DELETE | /api/v1/business-admin/offers/{id} | OWNER | Delete offer |

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
