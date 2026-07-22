# Shipping — REST API Contracts

## Shipping Settings
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /api/v1/businesses/{businessId}/shipping | OWNER | Get shipping settings |
| PATCH | /api/v1/businesses/{businessId}/shipping | OWNER | Update the business default shipping mode + city coverage |

Branch-level overrides are a planned explicit extension. They must fall back to the business policy when no override exists.

## Customer Profile
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /api/v1/profile | CUSTOMER | Get profile |
| PATCH | /api/v1/profile | CUSTOMER | Update profile fields |
| POST | /api/v1/profile/icon?iconUrl=... | CUSTOMER | Update profile icon |

## Key DTOs
- ShippingResponse: shippingMode, shippingCityIds
- UpdateShippingRequest: shippingMode, shippingCityIds
- CustomerProfileResponse: displayName, email, iconUrl, preferences
- UpdateCustomerProfileRequest: displayName, email, preferences (partial update)
