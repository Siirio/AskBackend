# Items — Frontend UX Expectations

## Item management (business cabinet)
- Items tab: list view with name, category, price, status (enabled/disabled)
- New Items appear first and remain visible after page reload.
- Actions per item: edit, enable, disable, delete
- No separate business status (active/needs update/completed) — just enabled/disabled/deleted
- Create/edit form: name, description, category (real UUID from backend), labeled purchase destinations, tags, characteristics, price, enabled state

## Item visibility
- Normal enabled Items publish immediately after creation without manual moderation.
- Autobanned Items remain saved in the business cabinet but do not appear in client search.
- Only enabled and auto-approved Items appear in client search; a branch only contributes optional location facts.
- Disabled Items are hidden from search and remain visible in the business cabinet

## Item result cards (customer-facing)
- Compact row: Business logo, Item name and short relevant information, price when known, and chat action
- Clicking the row opens full Item description plus the public Business profile and optional branch context
- Chat opens/resumes the durable Business conversation; search never creates a request
- `Proceed to Purchase` opens a chooser modal containing every purchase destination published for that Item. Destinations are Item-owned and are not grouped or selected by branch.
- If the Item has no purchase destination, `Proceed to Purchase` opens the shared Business chat with an editable draft naming the Item; the customer explicitly sends it.
- Until search returns this destination data, omit `Proceed to Purchase` rather than render an inert control.
