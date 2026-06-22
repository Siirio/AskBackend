# UX/UI Backend Contract

This is the backend-facing extraction from the current AskFrontend `EXPECTED_UX_UI_FLOW.md`. Keep this file synchronized before generating backend tasks. When frontend UX and backend docs differ, refresh this backend contract from the frontend flow before changing backend tasks or entities.

## Product Flow

- Ask is search-first.
- The client searches either products or services. There is no separate business search flow in MVP.
- Submitted search scope is strict and locked. Product search returns only product-context results/checks. Service search returns only service-context results/checks.
- Backend must not return services in product search or products in service search.
- Business pages can open from product cards, service cards, responses, history, chat, or recommendations, but not as a standalone primary search type.
- The customer writes a natural-language query and may narrow it with product or service categories.
- Categories scope search. They are not a marketplace-style catalog picker.
- Raw query must be preserved.
- One submitted search session has one locked scope: `PRODUCT` or `SERVICE`. Scope can be changed before submit, but not inside an already submitted search session.
- Dynamic filters are built from actual found product or service attributes.
- Auto supplier check exists when exact/strong catalog results are insufficient or when relevant business/branch candidates should be asked automatically.
- The customer does not manually create the main fallback request after search. Search submit can automatically create the supplier check/request.
- Auto supplier check is not standalone business search. It is attached to the current product/service search session and raw query.
- Backend must not invent delivery, courier availability, guaranteed stock count, exact service slots, or automatic booking truth.

## Search Result Rules

### Products

Product search returns enabled products/offers from enabled branches.

Product search also may create an automatic supplier check for suitable business/branch candidates. Supplier candidates are selected from branch/business category, product tags, branch profile, city, and other safe search evidence.

Supplier candidates are not proof of product availability. They are recipients of an automatic check. Their responses become supplier response rows inside the current search session.

Product result cards need:

- product image or category placeholder;
- product name;
- short known characteristics;
- price when known;
- business and branch;
- city or distance;
- product display state;
- actions: open, clarify, write, create request.

The customer-facing result screen is split into:

- `FOUND`: exact/similar catalog results;
- `SUPPLIER_CHECK`: automatically selected suppliers and their response status;
- `CHATS`: only real conversations.

Backend must support the frontend distinction between an auto supplier check and a customer-visible chat. Sending an auto supplier check must not create a customer-visible outgoing chat message or customer unread chat notification.

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

## Entry Point Model

There are three distinct entry paths into the system. They are not three equal "registrations":

| Path | Who | How | Endpoint |
|------|-----|-----|----------|
| Customer registration | End-user searching for products/services | Self-registers | `POST /auth/customer/register` |
| Business owner registration | Person creating a business on Ask | Self-registers | `POST /auth/business/register` |
| Staff activation | Staff added by owner | Created by owner, activates via login | `POST /auth/login` → `POST /auth/change-temporary-password` |

Staff members do NOT self-register. There is no `/auth/staff/register`, `/auth/manager/register`, or `/auth/operator/register`.

Business roles are only `OWNER` and `STAFF`.
`MANAGER` and `OPERATOR` must not appear in backend contracts, DTOs, enums, authorities, or frontend behavior.

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

### Core Principle

Staff do not register themselves. There is no public registration form for manager or operator roles. Staff accounts are created inside the business cabinet by owners or managers, then activated by the staff member through the standard login form with a temporary password.

### Staff Roles

Business-facing roles are only `OWNER` and `STAFF`.

Staff endpoints require `OWNER` authority on the target branch. Staff cannot manage other accounts or access branches where they are not assigned.

### Staff Status Lifecycle

```
                   owner creates staff
                   ┌──────────────────┐
                   │ PENDING_ACTIVATION│
                   └────────┬─────────┘
                            │ staff logs in + changes password
                   ┌────────▼─────────┐
                   │     ACTIVE       │
                   └────────┬─────────┘
                            │ owner resets password
                   ┌────────▼──────────┐
                   │ PASSWORD_RESET_   │
                   │ REQUIRED          │─── staff logs in + changes password ──→ ACTIVE
                   └───────────────────┘
                            │ owner disables access
                   ┌────────▼─────────┐
                   │    DISABLED      │
                   └──────────────────┘
```

- `PENDING_ACTIVATION`: owner created the staff member. Staff has not yet logged in. Temporary password is visible to owner.
- `ACTIVE`: staff has activated their account by setting a personal password. Temporary password is destroyed and no longer visible to owner.
- `PASSWORD_RESET_REQUIRED`: owner reset the staff password. Staff must change password at next login. New temporary password is visible to owner until activation.
- `DISABLED`: access revoked. Staff cannot log in.

### Staff Creation (Owner Side)

`POST /api/v1/businesses/{businessId}/branches/{branchId}/staff`

Owner or manager fills in:

- Staff name
- Role: Manager / Operator
- Login: email (phone in future)

Backend:

1. Verifies caller is OWNER of the business/branch.
2. Creates `AppUser` with `role=BUSINESS`, `status=PENDING_ACTIVATION`, `mustChangePassword=true`.
3. Generates temporary password, BCrypt-hashes it for login verification, AES-encrypts the plain text for owner visibility.
4. Creates `BranchMember` with the requested role.
5. Returns `StaffResponse` with `tempPassword` in plain text (one-time visibility at creation, plus visible in staff card while pending).

After creation, owner sees a confirmation screen with copy actions:

```
Сотрудник создан
Имя: Манас
Роль: Manager
Филиал: Mega Silk Way
Логин: manager@example.com
Временный пароль: Q7K9-M2PA

[Скопировать логин]
[Скопировать временный пароль]
[Скопировать всё сообщение]
[Скопировать сообщение для WhatsApp]
[Готово]
```

### Staff Card — Before Activation

In the staff list, a pending staff member shows:

- Name, role, branch
- Status: "Ожидает активации"
- Login visible
- Temporary password visible
- Actions: copy credentials, generate new temporary password, delete invitation

Temporary password is displayed only while `status = PENDING_ACTIVATION`.

### Staff Card — After Activation

After the staff member logs in and sets their own password:

- Name, role, branch
- Status: "Активен"
- Activated at: timestamp
- Login visible
- Password: hidden — "Сотрудник уже активировал аккаунт. Временный пароль больше недоступен."
- Actions: change role, reset password, disable access

### Staff Password Reset

`POST /api/v1/businesses/{businessId}/branches/{branchId}/staff/{id}/reset-password`

Owner or manager resets a staff password:

1. Generates new temporary password.
2. Sets status to `PASSWORD_RESET_REQUIRED`, `mustChangePassword=true`.
3. Returns `StaffResponse` with new `tempPassword` in plain text.

After reset, the temporary password is visible again until the staff member activates.

### Staff Self-View

A staff member logged in with `PASSWORD_RESET_REQUIRED` sees the same password change screen as a new staff member. After setting a new password, they become `ACTIVE` again.

## Unified Login Flow

`POST /api/v1/auth/login` accepts `{ email, password }` and works for ALL roles:

- Customer
- Business owner
- Business manager
- Business operator

Login logic:

1. Find user by email.
2. Verify BCrypt password.
3. If `mustChangePassword` is `false` → create normal session, return `AuthSessionResponse` with `activationRequired: false`.
4. If `mustChangePassword` is `true` (status `PENDING_ACTIVATION` or `PASSWORD_RESET_REQUIRED`) → create short-TTL activation session (5 minutes) with `activationRequired: true`. Frontend detects this and navigates to the password change screen.

### Password Change Screen

`POST /api/v1/auth/change-temporary-password` (authenticated with activation session):

1. Accepts `{ newPassword, passwordConfirmation }`.
2. Validates passwords match.
3. Hashes and stores new password.
4. Clears `tempPasswordEncrypted` (owner can no longer see it).
5. Sets `status=ACTIVE`, `activatedAt=now()`, `mustChangePassword=false`.
6. Creates new full session, revokes activation session.
7. Returns `AuthSessionResponse` with `activationRequired: false`.

Frontend password change screen:

```
Создайте новый пароль

Вы входите впервые. Для безопасности задайте свой постоянный пароль.

Новый пароль
Повторите пароль

[Сохранить и продолжить]
```

### Temporary Password Rules

- Temp password is BCrypt-hashed in `passwordHash` for login verification.
- Plain temp password is AES-encrypted in `tempPasswordEncrypted` for owner visibility.
- `tempPasswordEncrypted` is set to NULL on activation.
- Never store plain temporary password in the database.
- Activation session TTL is 5 minutes. Staff must complete password change within this window.
- `POST /auth/change-temporary-password` requires an activation session, not a normal session.

## Invite Code Flow

`POST /api/v1/businesses/{businessId}/branches/{branchId}/invites` creates shareable invite codes. This is an alternative self-service path:

- Owner or manager creates invite with role and optional `maxUses`.
- Invite code is a random opaque string with configurable expiry.
- Invite can be revoked before use.
- Invite list shows code, role, usage count, expiry, and revocation status.

Note: for MVP, direct staff creation (`POST /staff`) is the primary path. Invite codes are secondary.

## Retention

- Customer search history and snapshots live for a limited time, for example 10 days.
- Business operational history is separate and longer-lived.
- Business data must not be wiped after testing or demo usage once real onboarding starts.
