# Service — Frontend UX Expectations

SERVICE create and edit accept up to three real image files through selection, drag-and-drop, or clipboard paste. Owners reorder images by drag-and-drop; the first image is the primary search image. Missing images remain valid and render a neutral placeholder.

## Service result cards (customer-facing)
- Compact row: Business logo, Service name and short relevant information, price when known, and chat action
- Clicking the row opens full Service description plus the public Business profile and optional branch context
- Chat opens/resumes the durable Business conversation; search creates no booking or request
- `Proceed to Purchase` opens a chooser modal containing every purchase or booking destination published for that Service. Destinations are Service-owned and are not grouped or selected by branch.
- If the Service has no purchase destination, `Proceed to Purchase` opens the shared Business chat with an editable draft naming the Service; the customer explicitly sends it.
- Until search returns this destination data, omit `Proceed to Purchase` rather than render an inert control.

## Service contact flow
1. Customer finds a service offer and explicitly opens the business conversation.
2. Every authorized business member sees the shared customer history.
3. The customer and business discuss service details in chat.
4. Search never creates a booking, supplier response, or calendar reservation.

## Three-tier maturity model
- Level 1 (MVP current): Chat-first service discovery and contact.
- Level 2 (deferred): Explicit appointment tracking after a separately approved service workflow.
- Level 3 (deferred): Full calendar system with masters, resources, schedules.

## Service management (business cabinet)
- Services tab: list with name, category, price, schedule, status (active/inactive)
- New active Services publish immediately, appear first, and remain visible after reload.
- Actions: edit, activate, deactivate, delete
- Schedule text: free-form display text (e.g., "Пн-Пт 9:00-18:00"), not slot blocking
