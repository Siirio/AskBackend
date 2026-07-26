# Items — Frontend UX Expectations

## Item management (business cabinet)
- Items tab: list view with name, category, price, status (enabled/disabled)
- Actions per item: edit, enable, disable, delete
- No separate business status (active/needs update/completed) — just enabled/disabled/deleted
- Create/edit form: name, description, category (real UUID from backend), deep link, tags, characteristics, price, enabled state

## Item visibility
- Only enabled Items appear in client search; a branch only contributes optional location facts
- Disabled Items are hidden from search and remain visible in the business cabinet

## Item result cards (customer-facing)
- Compact row: Business logo, Item name and short relevant information, price when known, and chat action
- Clicking the row opens full Item description plus the public Business profile and optional branch context
- Chat opens/resumes the durable Business conversation; search never creates a request
