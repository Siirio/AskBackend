# Shipping & Customer Profile

Business shipping settings (mode + city coverage) and customer profile management.

## Key decisions
- Shipping settings: per-business, OWNER-only PATCH.
- shippingMode (VARCHAR): delivery method enum.
- shippingCityIds (JSONB): list of city UUIDs the business ships to.
- Customer profile: auto-created on first access (lazy initialization).
- Profile fields: displayName, email, phone, iconUrl, preferences (sizes, style, budget, city, favorite brands).
- Preferences are optional, transparent, and editable — not creepy tracking.
