# Items — Frontend UX Expectations

## Item management (business cabinet)
- Items tab: list view with name, category, price, status (enabled/disabled)
- Actions per item: edit, enable, disable, delete
- No separate business status (active/needs update/completed) — just enabled/disabled/deleted
- Create/edit form: name, description, category (real UUID from backend), tags, characteristics, price

## Item visibility
- Only enabled Items appear in client search; a branch only contributes optional location facts
- Disabled Items are hidden from search and remain visible in the business cabinet

## Item result cards (customer-facing)
- Item image or category placeholder
- Item name + short characteristics
- Price (when known)
- Business name + branch
- City or distance
- Actions: open, clarify, write, create request
