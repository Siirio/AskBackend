# Catalog — Frontend UX Expectations

## Product management (business cabinet)
- Products tab: list view with name, category, price, status (enabled/disabled)
- Actions per product: edit, enable, disable, delete
- No separate business status (active/needs update/completed) — just enabled/disabled/deleted
- Create/edit form: name, description, category (real UUID from backend), tags, SKU, characteristics, price

## Product visibility
- Only enabled products from enabled branches appear in client search
- Disabled products: hidden from search, visible in business cabinet
- Deleted products: hidden from search, may be hidden from cabinet

## Product result cards (customer-facing)
- Product image or category placeholder
- Product name + short characteristics
- Price (when known)
- Business name + branch
- City or distance
- Actions: open, clarify, write, create request
