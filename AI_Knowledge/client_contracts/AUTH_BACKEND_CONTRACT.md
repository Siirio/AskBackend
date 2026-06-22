# Auth Backend Contract

This document defines the desired production-facing auth and onboarding logic for Ask customers and businesses.

## Auth Model Summary

|   |   |
|---|---|
| Description | Login, registration, contact verification, session restore, logout, and business branch onboarding. |
| Auth required | No for auth start/register/verify. Yes for current session, logout, client APIs, and business cabinet APIs. |
| System module | identity |
| App roles | `CUSTOMER`, `BUSINESS` |
| Business authorities | `ROLE_BUSINESS_OWNER`, `ROLE_BUSINESS_STAFF` |
| Verification code | 6 digits |
| Real channel requirement | At least email verification must work for production-facing onboarding; phone/SMS can be added next. |

## 1. Functional Requirements

| Number | Requirement | Status | Source | Comment |
|---|---|---|---|---|
| AUTH-001 | Customer can register and login by phone or email. | Required | Product correction | If email is used, phone is not required. |
| AUTH-002 | Business can register and login by phone or email. | Required | Product correction | Same identifier rules as customer. |
| AUTH-003 | Every auth flow verifies the selected contact with a 6-digit code. | Required | Frontend UX | Verification truth belongs to backend. |
| AUTH-004 | Email verification must be real for production-facing MVP. | Required | Product correction | Mock-only onboarding is not acceptable. |
| AUTH-005 | SMS verification can be the first phone channel; WhatsApp and Telegram can be later channels. | Required | Product correction | They must not pretend to work until a real provider adapter exists. Current backend default keeps SMS disabled. |
| AUTH-006 | Remember-me creates a longer-lived session by backend config. | Required | Frontend UX | Client storage is not auth truth. |
| AUTH-007 | Business registration creates a real branch/store profile. | Required | Product correction | Current registration is for one concrete establishment/branch. |
| AUTH-008 | Registration contact is the initial public branch contact. | Required | Product correction | It can later be edited in branch profile. |
| AUTH-009 | Branches can have separate contacts later. | Required | Product correction | Do not model one company contact as all branch contacts forever. |
| AUTH-010 | Registered business products and services persist in the real database. | Required | Product correction | Onboarding is pre-sales data collection. |

## 2. Identity Rules

- A customer account can use either `email` or `phone` as login identifier.
- A business account can use either `email` or `phone` as login identifier.
- Exactly one primary login identifier is required during registration.
- If email is used, phone is optional and must not block registration.
- If phone is used, email is optional and must not block registration.
- Password is stored only as hash.
- Verification code is stored only as hash.
- Frontend must not decide that a contact is verified without backend `verify` success response.

## 3. Business Onboarding Model

Current MVP registration means: one concrete store/branch/establishment joins Ask.

The registration creates or prepares:

- `AppUser` for owner login;
- `Business` as the owning account/container;
- `BusinessMember` with owner role;
- `BusinessBranch` as the concrete registered establishment;
- `BusinessContact` for the selected registration contact as initial public contact;
- optional additional branch contact fields only when explicitly provided.

Do not require a "main office address" in the auth DTO. Use branch/establishment address:

- `branchName`;
- `branchCityId`;
- `branchAddress`;
- `onlineOnly`.

If `onlineOnly=true`, physical address is optional.

A later multi-branch cabinet can allow one business account to manage several branches. Current auth task must not assume that all branches share one contact.

## 4. System Settings

| Name | Code | Type |
|---|---|---|
| Challenge TTL | `auth.challenge.ttl` | duration |
| Challenge code length | `auth.challenge.code-length` | integer |
| Challenge max attempts | `auth.challenge.max-attempts` | integer |
| Customer session TTL | `auth.customer.session.ttl` | duration |
| Customer remembered session TTL | `auth.customer.remembered-session.ttl` | duration |
| Business session TTL | `auth.business.session.ttl` | duration |
| Business remembered session TTL | `auth.business.remembered-session.ttl` | duration |
| Email verification enabled | `auth.verification.email.enabled` | boolean |
| SMS verification enabled | `auth.verification.sms.enabled` | boolean |

Current MVP defaults:

- email verification is enabled and must use SMTP-backed real delivery;
- SMS verification is disabled by default until a real SMS provider adapter is connected;
- logging/noop sender implementations are development aids only and must not be described as production verification.

## 5. API Methods

### Start Customer Login

`POST /api/v1/auth/customer/login/start`

| # | Field | Type | Required | Comment |
|---|---|---|---|---|
| 1 | `email` | string | - | Required when phone is empty. |
| 2 | `phone` | string | - | Required when email is empty. |
| 3 | `rememberMe` | boolean | - | Default false. |

Rules:

- Exactly one of `email` or `phone` is required.
- Email uses `EMAIL`.
- Phone uses `SMS` while other phone channels are not implemented.

Response: `AuthChallengeResponse`.

### Register Customer

`POST /api/v1/auth/customer/register`

| # | Field | Type | Required | Comment |
|---|---|---|---|---|
| 1 | `displayName` | string | - | Customer visible name. |
| 2 | `email` | string | - | Required when phone is empty. |
| 3 | `phone` | string | - | Required when email is empty. |
| 4 | `password` | string | + | Store only hash. |
| 5 | `passwordConfirmation` | string | + | Must match. |
| 6 | `acceptedUserAgreement` | boolean | + | Must be true. |
| 7 | `rememberMe` | boolean | - | Default false. |

Response: `AuthChallengeResponse`.

### Start Business Login

`POST /api/v1/auth/business/login/start`

| # | Field | Type | Required | Comment |
|---|---|---|---|---|
| 1 | `email` | string | - | Required when phone is empty. |
| 2 | `phone` | string | - | Required when email is empty. |
| 3 | `rememberMe` | boolean | - | Default false. |

Response: `AuthChallengeResponse`.

### Register Business Branch

`POST /api/v1/auth/business/register`

| # | Field | Type | Required | Comment |
|---|---|---|---|---|
| 1 | `email` | string | - | Login and initial public contact when phone is empty. |
| 2 | `phone` | string | - | Login and initial public contact when email is empty. |
| 3 | `password` | string | + | Store only hash. |
| 4 | `passwordConfirmation` | string | + | Must match. |
| 5 | `businessName` | string | + | Owning business/container name. |
| 6 | `branchName` | string | + | Concrete store/establishment name. |
| 7 | `branchCityId` | uuid | - | Recommended for local search. |
| 8 | `branchAddress` | string | - | Required unless onlineOnly is true. |
| 9 | `onlineOnly` | boolean | - | Default false. |
| 10 | `acceptedBusinessRules` | boolean | + | Must be true. |
| 11 | `rememberMe` | boolean | - | Default false. |

Response: `AuthChallengeResponse`.

### Verify Code

`POST /api/v1/auth/verify`

| # | Field | Type | Required | Comment |
|---|---|---|---|---|
| 1 | `authChallengeId` | uuid | + | Challenge id. |
| 2 | `code` | string | + | Exactly 6 digits. |

Response: `AuthSessionResponse`.

### Current Session

`GET /api/v1/auth/session`

Authorization: Bearer token required.

Response: `AuthSessionResponse`.

### Logout

`POST /api/v1/auth/logout`

Authorization: Bearer token required.

Response: `LogoutResponse`.

## 6. DTO Contracts

### AuthChallengeResponse

| # | Field | Type | Required | Comment |
|---|---|---|---|---|
| 1 | `authChallengeId` | uuid | + | Challenge id. |
| 2 | `role` | string | + | `CUSTOMER` or `BUSINESS`. |
| 3 | `purpose` | string | + | `LOGIN` or `REGISTER`. |
| 4 | `channel` | string | + | `EMAIL` or `SMS`. |
| 5 | `maskedDestination` | string | + | Display-safe destination. |
| 6 | `expiresAt` | datetime | + | Challenge expiry. |

### AuthSessionResponse

| # | Field | Type | Required | Comment |
|---|---|---|---|---|
| 1 | `accessToken` | string | + | Bearer token or opaque session token. |
| 2 | `tokenType` | string | + | `Bearer`. |
| 3 | `expiresAt` | datetime | + | Session expiry. |
| 4 | `remembered` | boolean | + | Whether remember-me was selected. |
| 5 | `role` | string | + | `CUSTOMER` or `BUSINESS`. |
| 6 | `user` | object | + | `AuthUserResponse`. |
| 7 | `business` | object | - | Present for business sessions. |
| 8 | `startRoute` | string | + | `CLIENT_SEARCH`, `OWNER_BRANCHES`, or `BRANCH_WORKSPACE`. |

### AuthUserResponse

| # | Field | Type | Required | Comment |
|---|---|---|---|---|
| 1 | `userId` | uuid | + | User id. |
| 2 | `displayName` | string | - | Customer or owner display name. |
| 3 | `email` | string | - | Present when account uses or has email. |
| 4 | `phone` | string | - | Present when account uses or has phone. |
| 5 | `status` | string | + | User status. |

### AuthBusinessContextResponse

| # | Field | Type | Required | Comment |
|---|---|---|---|---|
| 1 | `businessId` | uuid | + | Owning business id. |
| 2 | `businessName` | string | + | Business name. |
| 3 | `branchId` | uuid | + | Registered branch id. |
| 4 | `branchName` | string | + | Registered branch name. |
| 5 | `membershipId` | uuid | + | Owner membership id. |
| 6 | `memberRole` | string | + | `OWNER` or `STAFF`. |

## 7. Implementation Boundaries

- Keep auth in `kz.ask.identity`.
- Use `kz.ask.business` boundaries for business, branch, member, and contact creation.
- Do not return mock codes in production-facing task contracts.
- Do not require both phone and email.
- Do not model registration as a throwaway mock.
- Do not create tests or run Maven unless explicitly requested.
