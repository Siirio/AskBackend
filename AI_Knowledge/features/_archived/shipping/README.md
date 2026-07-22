# Shipping & Customer Profile

Business delivery policy (mode + city coverage), optional branch overrides, and customer profile management.

## Key decisions
- Delivery policy is business-wide by default, OWNER-managed, and may later be overridden by a branch.
- shippingMode (VARCHAR): delivery method enum.
- shippingCityIds (JSONB): list of city UUIDs the business ships to.
- Customer profile: auto-created on first access (lazy initialization).
- Profile fields: displayName, email, phone, iconUrl, preferences (sizes, style, budget, city, favorite brands).
- Preferences are optional, transparent, and editable — not creepy tracking.
