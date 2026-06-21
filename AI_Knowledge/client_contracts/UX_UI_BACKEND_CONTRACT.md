# UX/UI Backend Contract

This is the backend-facing extraction from the current AskFrontend `EXPECTED_UX_UI_FLOW.md`. Keep this file synchronized before generating backend tasks.

## Product Flow

- Ask is search-first.
- The client searches either products or services. There is no separate business search flow in MVP.
- Business pages can open from product cards, service cards, responses, history, chat, or recommendations, but not as a standalone primary search type.
- The customer writes a natural-language query and may narrow it with product or service categories.
- Categories scope search. They are not a marketplace-style catalog picker.
- Raw query must be preserved.
- Dynamic filters are built from actual found product or service attributes.
- Fallback request exists when search results are missing, too weak, or the customer wants to ask businesses directly.
- Backend must not invent delivery, courier availability, guaranteed stock count, exact service slots, or automatic booking truth.

## Search Result Rules

### Products

Product search returns enabled products/offers from enabled branches.

Product result cards need:

- product image or category placeholder;
- product name;
- short known characteristics;
- price when known;
- business and branch;
- city or distance;
- product display state;
- actions: open, clarify, write, create request.

Product result DTOs use the current MVP model only: business/branch context, product display data, price when known, display state, calculated distance when possible, and contact actions.

Product visibility is controlled by enabled/disabled/deleted business actions. If a business keeps a product enabled, it is treated as current for search display. If the customer needs confirmation, use clarify/request/chat flow.

### Services

Service search returns enabled services from enabled branches.

Service result cards need:

- service image or business image;
- service name;
- business and branch;
- approximate price or price-from;
- approximate duration when known;
- district or distance;
- desired-time request action;
- actions: request booking, write.

Services on MVP are request-to-book, not guaranteed slot booking. The customer chooses desired time, Ask sends a structured service request, and the business confirms, declines, or proposes another time.

Service result DTOs use the current MVP model only: business/branch context, service display data, price when known, approximate duration, display state, calculated distance when possible, and contact actions.

Service visibility is controlled by active/inactive service toggles and branch ownership.

## Distance Logic

`distanceMeters` is allowed only when it is calculated from real coordinates.

Backend calculation:

- client sends current geolocation as `customerLatitude` and `customerLongitude`;
- branch has stored `latitude` and `longitude`;
- backend calculates straight-line distance between customer and branch coordinates using a geodesic formula such as Haversine;
- backend returns `distanceMeters` as nullable integer;
- if customer coordinates or branch coordinates are missing, return `distanceMeters=null`;
- do not calculate distance from city name, district text, address text, or mocked coordinates;
- sorting by distance is allowed only for rows where distance is known; unknown distance must not be presented as nearby.

Frontend wording can show city, district, address, or distance depending on available data.

## Auth And Onboarding Flow

Backend auth must follow `AI_Knowledge/client_contracts/AUTH_BACKEND_CONTRACT.md`.

- Auth is required before entering customer app or business cabinet.
- Customer and business registration/login can use phone or email.
- If phone is used, verification can be SMS first; WhatsApp and Telegram can be added later as delivery channels.
- If email is used, verification must work by real email code for the production-facing MVP.
- At least one real verification channel must work before real onboarding. Do not design business onboarding as mock-only.
- Successful customer auth routes to Search.
- Successful business auth routes to Activity.
- Business onboarding creates a real branch/store profile whose data persists.
- The registration contact is also the initial public contact for that branch unless the business later edits branch contacts.

## Business Cabinet Flow

Business cabinet MVP sections:

- Activity;
- Products;
- Services;
- Company or branch profile.

The cabinet is production-facing onboarding, not a throwaway mock:

- registered businesses must persist;
- products added by businesses must persist;
- services added by businesses must persist;
- enabled products and services become searchable for customers;
- disabled or deleted products and services must not appear in live client search.

Each registration currently creates one concrete branch/store profile. A higher-level multi-branch business management model can be added later, but current registration must be treated as onboarding a specific establishment/branch.

Branch contacts are managed by the branch. A future business account may manage several branches, each with its own contacts.

## Product Management Rules

- Products do not have a separate business status like active/needs update/completed.
- Products have actions: edit, enable, disable, delete.
- Enabled products can appear in client product search.
- Disabled or deleted products must not appear in live client product search.
- Inventory counting is not part of the MVP.
- Data freshness is not tracked in MVP.
- If the customer needs confirmation, use chat, clarify action, or fallback request.

## Service Management Rules

- Services can be active or inactive.
- Active services can appear in client service search.
- Inactive services must not appear in live client service search.
- Services may have price, approximate duration, description, availability schedule text or pattern, and branch.
- MVP service requests are not guaranteed bookings.
- Business confirms final date/time/conditions.

## Request And Response Statuses

Product request business responses:

- `HAS_ITEM`: business has the requested product or a sufficiently matching product.
- `NO_ITEM`: business says it does not have it.
- `NEED_CLARIFICATION`: business needs details.
- `HAS_ANALOG`: business offers an analog.

Service request business responses:

- `CAN_PROVIDE`: business can provide the service.
- `CANNOT_PROVIDE`: business cannot provide it.
- `NEED_CLARIFICATION`: business needs date, time, address, service type, or other details.
- `SUGGEST_OTHER_TIME`: business proposes another time.

Updating a business response updates the same row. It must not create duplicates.

## Chat And Contact Actions

- Ask chat is always available from concrete context.
- Chat is not a standalone bottom navigation tab.
- Chat is scoped to product, service, request, booking request, business page, or response context.
- If the entity is visible and the user is authenticated, chat can be opened from the concrete context.
- WhatsApp, Telegram, phone, email, map action, and Ask chat are separate contact actions.

## Retention

- Customer search history and snapshots live for a limited time, for example 10 days.
- Business operational history is separate and longer-lived.
- Business data must not be wiped after testing or demo usage once real onboarding starts.
