# UX/UI Backend Contract

This is the backend-facing extraction from the current AskFrontend `EXPECTED_UX_UI_FLOW.md`. Keep this file synchronized before generating backend tasks. When frontend UX and backend docs differ, refresh this backend contract from the frontend flow before changing backend tasks or entities.

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

Backend model: `ProductOffer.enabled` is the live-search toggle. Do not add stock status, stock quantity, availability confidence, or freshness fields for MVP product visibility.

### Services

Service search returns active services from enabled branches.

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

Backend model: `ServiceBranchOffer.active` is the live-search toggle. `ServiceBranchOffer.scheduleText` is display text or conditions, not guaranteed slot truth.

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
- If phone is used, SMS must not pretend to work until a real provider adapter is connected.
- WhatsApp and Telegram can be added later as delivery channels.
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
- Availability confidence is not tracked in MVP.
- If the customer needs confirmation, use chat, clarify action, or fallback request.

## Service Management Rules

- Services can be active or inactive.
- Active services can appear in client service search.
- Inactive services must not appear in live client service search.
- Services may have price, approximate duration, description, schedule text or future schedule pattern, and branch.
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

`SupplierResponseStatus` uses these exact enum values in the current backend model. Do not use old generic `AVAILABLE`, `UNAVAILABLE`, `NEEDS_CONFIRMATION`, or `ALTERNATIVE_OFFERED` statuses.

## Chat And Contact Actions

- Ask chat is always available from concrete context.
- Chat is not a standalone bottom navigation tab.
- Chat is scoped to product, service, request, booking request, business page, or response context.
- If the entity is visible and the user is authenticated, chat can be opened from the concrete context.
- WhatsApp, Telegram, phone, email, map action, and Ask chat are separate contact actions.

## Staff Management Flow

Business owners and managers manage branch-level staff through staff and invite endpoints. Staff are `AppRole.BUSINESS` users whose role is determined by their `BranchMember` record, not by a separate app role.

### Staff Roles

- MANAGER: full branch management access (staff, products, services).
- OPERATOR: limited branch access.

Staff endpoints require OWNER or MANAGER authority on the target branch.

### Staff Creation And Activation

Owner or manager creates staff through `POST /staff`:

- Backend creates `AppUser` with `role=BUSINESS`, `status=PENDING_ACTIVATION`, `mustChangePassword=true`.
- System generates a temporary password, BCrypt-hashes it for login, and AES-encrypts the plain text for owner visibility.
- Backend creates `BranchMember` with the requested role (MANAGER or OPERATOR).
- Response includes `tempPassword` (plain text, one-time visibility).

Owner sees temp password in the staff card while `status = PENDING_ACTIVATION`. After activation the password is hidden and only a "Reset password" button is available.

### Staff Statuses

- `PENDING_ACTIVATION`: created by owner, not yet activated.
- `ACTIVE`: activated and working.
- `PASSWORD_RESET_REQUIRED`: owner reset password, staff must change at next login.
- `DISABLED`: access revoked.

### Staff Password Reset

`POST /staff/{id}/reset-password` (OWNER or MANAGER):

- Generates new temporary password.
- Sets status to `PASSWORD_RESET_REQUIRED`, `mustChangePassword=true`.
- Returns new `tempPassword` in response.

## Unified Login Flow

`POST /api/v1/auth/login` accepts `{ email, password }` and works for all roles (CUSTOMER, BUSINESS owner, BUSINESS manager, BUSINESS operator).

Login logic:

1. Find user by email.
2. Verify BCrypt password.
3. If `mustChangePassword` is false → create normal session with appropriate TTL, return.
4. If `mustChangePassword` is true (status PENDING_ACTIVATION or PASSWORD_RESET_REQUIRED) → create short-TTL activation session (5 minutes) with `activationRequired: true`. Frontend detects this flag and navigates to the password change screen.

`POST /api/v1/auth/change-temporary-password` (authenticated with activation session):

1. Accepts `{ newPassword, passwordConfirmation }`.
2. Validates password confirmation matches.
3. Hashes and stores new password, clears temp password encrypted value.
4. Sets `status=ACTIVE`, `activatedAt=now()`, `mustChangePassword=false`.
5. Creates new full session, revokes activation session.
6. Returns normal session response.

### Temporary Password Rules

- Temp password is BCrypt-hashed in `passwordHash` for login verification.
- Plain temp password is AES-encrypted in `tempPasswordEncrypted` for owner visibility.
- `tempPasswordEncrypted` is set to NULL on activation.
- Activation session TTL is 5 minutes. Staff must complete password change within this window.
- `POST /auth/change-temporary-password` requires an activation session (not a normal session).

## Invite Code Flow

`POST /staff` is the direct creation path. Invite codes provide an alternative self-service path:

- Owner or manager creates invite with role and optional maxUses.
- Invite code is a random opaque string with expiry.
- Invite can be revoked before use.
- Invite list shows code, role, usage count, expiry, and revocation status.

## Retention

- Customer search history and snapshots live for a limited time, for example 10 days.
- Business operational history is separate and longer-lived.
- Business data must not be wiped after testing or demo usage once real onboarding starts.
