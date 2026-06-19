# UX/UI Backend Contract

This is the backend-facing extraction from the Ask UX/UI flow. The full frontend blueprint can live in the frontend repository; AskBackend only needs the product flow, statuses, and API contract expectations.

## Product Flow

- Ask is mobile-first and search-first.
- The customer writes a natural-language query and may scope it with a category.
- Category selection scopes Smart Search; it is not the main product picker.
- The customer must not be forced to manually choose one concrete SKU from a marketplace-style list in the primary flow.
- Backend should return known products, services, and businesses when confidence is good enough.
- Backend should create or support fallback requests when known data is missing, stale, low-confidence, or confirmation-needed.
- Manual supplier replies must not invent stock quantity, courier availability, delivery SLA, service slots, or booking certainty.

## Customer Request Statuses

Backend should expose stable machine-readable statuses. Frontend owns normal localization.

- `DRAFT`: request is not ready for dispatch.
- `CREATED`: request exists but is not being dispatched yet.
- `DISPATCHING`: request is being sent to eligible suppliers.
- `SENT`: request was sent and is waiting for replies.
- `PARTIALLY_RESPONDED`: at least one supplier response exists.
- `COMPLETED`: request lifecycle is complete.
- `EXPIRED`: request TTL ended.
- `CANCELLED`: request was cancelled.
- `FAILED`: dispatch or lifecycle failed.

## Supplier Response Statuses

- `AVAILABLE`: supplier says they can offer the requested item or service.
- `UNAVAILABLE`: supplier explicitly says they cannot.
- `NEED_CLARIFICATION`: supplier needs size, model, flavor, year, article, time, or other details.
- `ALTERNATIVE_OFFERED`: exact match is unavailable, but a similar option exists.

## Request And Response Rules

- ProductRequest or equivalent request aggregate must preserve the raw customer query.
- Request dispatch must be idempotent.
- Dispatch should target active eligible suppliers only.
- The same supplier/channel must not receive duplicate request messages for the same request.
- Supplier response should be upsert/update based on request and supplier identity.
- A supplier may answer once and retry/update only within the approved product rule.
- Updating one supplier response keeps that supplier row as the same response in client feeds.
- New supplier responses appear chronologically after earlier responses.
- Feed data must support dozens or 100+ responses through compact rows, filters, counts, and progressive reveal.

## Client Data Needs

API responses should support:

- customer request history;
- active request status and progress;
- recipient count and response count;
- response feed filters with counts;
- compact response rows with supplier, status, price, distance, and product hint;
- expanded response details with product image, supplier, product/service, price, comment, address, map action, and contacts;
- supplier inbox sorted by unanswered or new requests;
- supplier reply attempts and limit state;
- per-request and per-supplier chat messages;
- notification counts and read states.

## Chat And Contact Actions

- Ask chat is scoped to one request and one supplier.
- Customer chat must have a back path to the supplier response feed.
- Supplier chat opens as its own sub-view, not buried below the reply form.
- WhatsApp, Telegram, map links, and Ask chat are separate per-response contact actions.
- Map action should not appear when branch address is unknown.
- Browser prototypes may use web URLs; native clients may use deep links.

## Integration Behavior

- Telegram and WhatsApp are delivery/contact adapters, not core business logic.
- Paloma, 1C, re:Kassa, Shopify, POS, CRM, and e-commerce systems are inventory, POS, catalog, or scheduling providers.
- Automatic replies are valid only when provider data supports the fact and the source is explicit.
- Integration-backed facts should remain distinguishable from manual supplier replies.
