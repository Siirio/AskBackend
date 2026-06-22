# Frontend API Tasks

Endpoint-by-endpoint typed contracts for frontend implementation. Every endpoint lists request fields with exact types, response fields with exact types, possible HTTP status codes, error codes, and frontend behavior notes.

---

## 1. Auth Endpoints

Base path: `/api/v1/auth`

---

### 1.1. Customer Login Start

```
POST /api/v1/auth/customer/login/start
Auth: none
```

**Request: `CustomerLoginStartRequest`**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `email` | `string` | conditional | Required when `phone` is empty. Must be valid email format. |
| `phone` | `string` | conditional | Required when `email` is empty. |
| `rememberMe` | `boolean` | no | Default `false`. Extends session TTL when true. |

Validation: exactly one of `email` or `phone` must be present (non-blank).

**Response `200`: `AuthChallengeResponse`**

| Field | Type | Notes |
|-------|------|-------|
| `authChallengeId` | `uuid` | Challenge ID for verify step. |
| `role` | `string` | `"CUSTOMER"` |
| `purpose` | `string` | `"LOGIN"` |
| `channel` | `string` | `"EMAIL"` or `"SMS"` |
| `maskedDestination` | `string` | Display-safe destination (e.g. `a***@gmail.com`). |
| `expiresAt` | `datetime` | Challenge expiry (ISO 8601). |

**Error responses**

| Status | ErrorCode | When |
|--------|-----------|------|
| `400` | `REGISTRATION_PAYLOAD_ERROR` | Neither email nor phone provided, or both provided. |
| `400` | `EMAIL_ALREADY_REGISTERED` | Email belongs to a registered customer (unexpected on login start). |
| `500` | `DELIVERY_FAILED` | Email/SMS delivery failed. |
| `500` | `INTERNAL_ERROR` | Unexpected server error. |

**Frontend behavior**
- Show code input screen after receiving challenge.
- Display `maskedDestination` so the user knows where the code was sent.
- Start expiry countdown from `expiresAt`.
- Do not auto-retry on delivery failure — show error with retry button.

---

### 1.2. Customer Registration

```
POST /api/v1/auth/customer/register
Auth: none
```

**Request: `CustomerRegisterRequest`**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `displayName` | `string` | no | Customer visible name. |
| `email` | `string` | conditional | Required when `phone` is empty. Must be valid email. |
| `phone` | `string` | conditional | Required when `email` is empty. |
| `password` | `string` | yes | Min 8, max 128 characters. |
| `passwordConfirmation` | `string` | yes | Must equal `password`. |
| `acceptedUserAgreement` | `boolean` | yes | Must be `true`. |
| `rememberMe` | `boolean` | no | Default `false`. |

Validation rules (backend enforces all):
- Exactly one of `email` or `phone` (non-blank).
- `password` equals `passwordConfirmation`.
- `acceptedUserAgreement` is `true`.

**Response `201`: `AuthChallengeResponse`**

| Field | Type | Notes |
|-------|------|-------|
| `authChallengeId` | `uuid` | Challenge ID for verify step. |
| `role` | `string` | `"CUSTOMER"` |
| `purpose` | `string` | `"REGISTER"` |
| `channel` | `string` | `"EMAIL"` or `"SMS"` |
| `maskedDestination` | `string` | Display-safe destination. |
| `expiresAt` | `datetime` | Challenge expiry (ISO 8601). |

**Error responses**

| Status | ErrorCode | When |
|--------|-----------|------|
| `400` | `REGISTRATION_PAYLOAD_ERROR` | Validation failed (missing contact, passwords mismatch, no agreement). |
| `409` | `EMAIL_ALREADY_REGISTERED` | Email already taken. |
| `409` | `PHONE_ALREADY_REGISTERED` | Phone already taken. |
| `500` | `DELIVERY_FAILED` | Delivery failure. |
| `500` | `INTERNAL_ERROR` | Unexpected server error. |

**Frontend behavior**
- Validate passwords match and agreement accepted before sending (client-side + rely on 400).
- On `409` show inline error on email/phone field — "already registered".
- After `201` navigate to code verification screen.

---

### 1.3. Business Login Start

```
POST /api/v1/auth/business/login/start
Auth: none
```

**Request: `BusinessLoginStartRequest`**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `email` | `string` | conditional | Required when `phone` is empty. Must be valid email. |
| `phone` | `string` | conditional | Required when `email` is empty. |
| `rememberMe` | `boolean` | no | Default `false`. |

Validation: exactly one of `email` or `phone` must be present (non-blank).

**Response `200`: `AuthChallengeResponse`**

| Field | Type | Notes |
|-------|------|-------|
| `authChallengeId` | `uuid` | Challenge ID. |
| `role` | `string` | `"BUSINESS"` |
| `purpose` | `string` | `"LOGIN"` |
| `channel` | `string` | `"EMAIL"` or `"SMS"` |
| `maskedDestination` | `string` | Display-safe destination. |
| `expiresAt` | `datetime` | Challenge expiry (ISO 8601). |

**Error responses**

| Status | ErrorCode | When |
|--------|-----------|------|
| `400` | `REGISTRATION_PAYLOAD_ERROR` | Neither email nor phone, or both. |
| `404` | `USER_NOT_FOUND` | No business account with this email/phone. |
| `500` | `DELIVERY_FAILED` | Delivery failure. |
| `500` | `INTERNAL_ERROR` | Unexpected server error. |

**Frontend behavior**
- Same flow as customer login start.
- On `404` show "account not found" — suggest registration.

---

### 1.4. Business Registration

```
POST /api/v1/auth/business/register
Auth: none
```

**Request: `BusinessRegisterRequest`**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `email` | `string` | conditional | Login and initial public contact. |
| `phone` | `string` | conditional | Login and initial public contact. |
| `password` | `string` | yes | Min 8, max 128. |
| `passwordConfirmation` | `string` | yes | Must equal `password`. |
| `businessName` | `string` | yes | Owning business/container name. |
| `branchName` | `string` | yes | Concrete store/establishment name. |
| `branchCityId` | `uuid` | no | City ID for local search. |
| `branchAddress` | `string` | conditional | Required unless `onlineOnly` is `true`. |
| `onlineOnly` | `boolean` | no | Default `false`. When `true`, physical address optional. |
| `acceptedBusinessRules` | `boolean` | yes | Must be `true`. |
| `rememberMe` | `boolean` | no | Default `false`. |

Validation rules:
- Exactly one of `email` or `phone` (non-blank).
- `password` equals `passwordConfirmation`.
- `acceptedBusinessRules` is `true`.
- If `onlineOnly` is `false` (or not set), `branchCityId` and `branchAddress` required.

**Response `201`: `AuthChallengeResponse`**

| Field | Type | Notes |
|-------|------|-------|
| `authChallengeId` | `uuid` | Challenge ID. |
| `role` | `string` | `"BUSINESS"` |
| `purpose` | `string` | `"REGISTER"` |
| `channel` | `string` | `"EMAIL"` or `"SMS"` |
| `maskedDestination` | `string` | Display-safe destination. |
| `expiresAt` | `datetime` | Challenge expiry (ISO 8601). |

**Error responses**

| Status | ErrorCode | When |
|--------|-----------|------|
| `400` | `REGISTRATION_PAYLOAD_ERROR` | Validation failed. |
| `404` | `CITY_NOT_FOUND` | `branchCityId` not found. |
| `409` | `EMAIL_ALREADY_REGISTERED` | Email already taken by any role. |
| `409` | `PHONE_ALREADY_REGISTERED` | Phone already taken. |
| `500` | `DELIVERY_FAILED` | Delivery failure. |
| `500` | `INTERNAL_ERROR` | Unexpected server error. |

**Frontend behavior**
- Multi-step form recommended: credentials → business info → confirmation.
- Show city picker that resolves to `branchCityId`.
- On `404 CITY_NOT_FOUND` show "city not found" on city picker.
- After `201` navigate to code verification.

---

### 1.5. Verify Code

```
POST /api/v1/auth/verify
Auth: none
```

**Request: `VerifyCodeRequest`**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `authChallengeId` | `uuid` | yes | Challenge ID from login/register start. |
| `code` | `string` | yes | Exactly 6 digits (`\d{6}`). |

**Response `200`: `AuthSessionResponse`**

| Field | Type | Notes |
|-------|------|-------|
| `accessToken` | `string` | Bearer token. |
| `tokenType` | `string` | `"Bearer"` |
| `expiresAt` | `datetime` | Session expiry (ISO 8601). |
| `remembered` | `boolean` | Whether remember-me was selected. |
| `activationRequired` | `boolean` | `true` when user must change temporary password. Frontend must navigate to password change screen. |
| `role` | `string` | `"ROLE_CUSTOMER"`, `"ROLE_BUSINESS_OWNER"`, or `"ROLE_BUSINESS_STAFF"`. |
| `startRoute` | `string` | `"CLIENT_SEARCH"` for customers, `"OWNER_BRANCHES"` for owners, `"BRANCH_WORKSPACE"` for staff. |
| `user` | `object` | `AuthUserResponse` (see below). |
| `business` | `object` | `AuthBusinessContextResponse`. Present for business roles, `null` for customers. |

**`AuthUserResponse` (user object)**

| Field | Type | Notes |
|-------|------|-------|
| `userId` | `uuid` | User ID. |
| `displayName` | `string` | Display name (nullable). |
| `email` | `string` | Email if account has one (nullable). |
| `phone` | `string` | Phone if account has one (nullable). |
| `status` | `string` | User status — see [User Statuses](#user-statuses). |

**`AuthBusinessContextResponse` (business object)**

| Field | Type | Notes |
|-------|------|-------|
| `businessId` | `uuid` | Business ID. |
| `businessName` | `string` | Business name. |
| `branchId` | `uuid` | Branch ID. |
| `branchName` | `string` | Branch name. |
| `membershipId` | `uuid` | Membership record ID. |
| `memberRole` | `string` | `"OWNER"` or `"STAFF"`. |
| `startRoute` | `string` | `OWNER_BRANCHES` for owner, `BRANCH_WORKSPACE` for staff. |
| `selectedBranchId` | `uuid` | Present for Staff. Nullable for Owner before branch selection. |

**Error responses**

| Status | ErrorCode | When |
|--------|-----------|------|
| `400` | `CHALLENGE_EXPIRED` | Challenge TTL exceeded. Message includes challenge ID. |
| `400` | `CHALLENGE_MAX_ATTEMPTS` | Too many wrong code attempts. Message includes challenge ID. |
| `400` | `CHALLENGE_INVALID_CODE` | Wrong code. Message includes challenge ID. |
| `404` | `CHALLENGE_NOT_FOUND` | Challenge ID doesn't exist. |
| `500` | `INTERNAL_ERROR` | Unexpected server error. |

**Frontend behavior**
- Store `accessToken` securely (httpOnly cookie preferred, Authorization header fallback).
- If `activationRequired === true` → navigate to `/change-password` screen (see 1.7).
- If `activationRequired === false` → navigate to `startRoute`.
- On wrong code: show inline error, allow retry. Track attempts locally.
- On expired: show "code expired" with option to request new code.

---

### 1.6. Unified Login

```
POST /api/v1/auth/login
Auth: none
```

Used by ALL roles: customer, business owner, business staff.

**Request: `LoginRequest`**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `email` | `string` | yes | Login identifier. |
| `password` | `string` | yes | Account password. |

**Response `200`: `AuthSessionResponse`**

Same shape as verify response (see 1.5). Key difference: `activationRequired` signals whether the user must change password immediately.

**Error responses**

| Status | ErrorCode | When |
|--------|-----------|------|
| `401` | `INVALID_CREDENTIALS` | Wrong email or password. |
| `403` | `ACCOUNT_NOT_ACTIVE` | Account blocked or deleted. |
| `500` | `INTERNAL_ERROR` | Unexpected server error. |

**Frontend behavior**
- If `activationRequired === true`: user is staff with PENDING_ACTIVATION or PASSWORD_RESET_REQUIRED status. Navigate to `/change-password`. The returned `accessToken` is a short-lived activation session (5 min TTL). It can only be used to call `/auth/change-temporary-password`.
- If `activationRequired === false`: normal session. Navigate to `startRoute`.
- On `401`: show "wrong email or password" — do NOT reveal which is wrong.
- On `403`: show "account blocked" — no retry.

---

### 1.7. Change Temporary Password

```
POST /api/v1/auth/change-temporary-password
Auth: Bearer token (activation session)
```

Used by staff on first login or after password reset by owner.

**Request: `ChangeTemporaryPasswordRequest`**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `newPassword` | `string` | yes | New permanent password. |
| `passwordConfirmation` | `string` | yes | Must equal `newPassword`. |

**Response `200`: `AuthSessionResponse`**

Full session with `activationRequired: false`. `startRoute` will be `"BRANCH_WORKSPACE"`. `user.status` will be `"ACTIVE"`.

**Error responses**

| Status | ErrorCode | When |
|--------|-----------|------|
| `400` | `PASSWORDS_DO_NOT_MATCH` | Confirmation doesn't match new password. |
| `400` | `PASSWORD_CHANGE_NOT_REQUIRED` | User is already active (no temp password to change). |
| `401` | `SESSION_INVALID` | Activation session expired or invalid. |
| `500` | `INTERNAL_ERROR` | Unexpected server error. |

**Frontend behavior**
- This screen appears when `activationRequired === true` after login.
- Show two password fields + submit button.
- Validate match client-side before sending.
- The activation session TTL is 5 minutes. If it expires, show message: "Session expired. Please log in again." and redirect to `/login`.
- After success: store the new full session token, navigate to `BUSINESS_ACTIVITY`.

---

### 1.8. Current Session

```
GET /api/v1/auth/session
Auth: Bearer token
```

Restore session from stored token. Used on app launch.

**Request body:** none.

**Response `200`: `AuthSessionResponse`**

Same as verify/login response.

**Error responses**

| Status | ErrorCode | When |
|--------|-----------|------|
| `401` | `SESSION_INVALID` | Token expired or invalid. |
| `500` | `INTERNAL_ERROR` | Unexpected server error. |

**Frontend behavior**
- Call on app startup if a stored token exists.
- On `401`: clear stored token, redirect to auth screen.
- On `200`: restore session state, navigate to `startRoute`.

---

### 1.9. Logout

```
POST /api/v1/auth/logout
Auth: Bearer token
```

**Request body:** none.

**Response `200`: `LogoutResponse`**

| Field | Type | Notes |
|-------|------|-------|
| `success` | `boolean` | Always `true`. |

**Error responses**

| Status | ErrorCode | When |
|--------|-----------|------|
| `401` | `SESSION_INVALID` | Token already expired (still succeeds client-side). |
| `500` | `INTERNAL_ERROR` | Unexpected server error. |

**Frontend behavior**
- Always clear stored token on client side regardless of response.
- Navigate to auth screen.

---

## 2. Staff Management Endpoints

Base path: `/api/v1/businesses/{businessId}/branches/{branchId}/staff`

All endpoints require `ROLE_BUSINESS_OWNER` authority.

---

### 2.1. Create Staff

```
POST /api/v1/businesses/{businessId}/branches/{branchId}/staff
Auth: Bearer token (OWNER)
```

Owner creates a staff member.

**Request: `CreateStaffRequest`**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `email` | `string` | yes | Staff login email. |
| `displayName` | `string` | yes | Staff display name. |
| `role` | `string` | - | Must be omitted or fixed as `"STAFF"`. Frontend should not show role picker. |

**Response `201`: `StaffResponse`**

| Field | Type | Notes |
|-------|------|-------|
| `id` | `uuid` | Staff user ID. |
| `email` | `string` | Staff email. |
| `displayName` | `string` | Staff name. |
| `role` | `string` | `"STAFF"`. |
| `status` | `string` | `"PENDING_ACTIVATION"` (initial state). |
| `tempPassword` | `string` | One-time temporary password. Visible ONLY in this response and in staff list while status is `PENDING_ACTIVATION` or `PASSWORD_RESET_REQUIRED`. |
| `activatedAt` | `datetime` | `null` until staff activates. |

**Error responses**

| Status | ErrorCode | When |
|--------|-----------|------|
| `400` | `REGISTRATION_PAYLOAD_ERROR` | Validation failed (missing fields). |
| `403` | `ACCESS_DENIED` | Caller is not owner of this branch. |
| `404` | `BRANCH_NOT_FOUND` | Branch doesn't exist. |
| `409` | `EMAIL_ALREADY_EXISTS` | A user with this email already exists (any role). |
| `500` | `INTERNAL_ERROR` | Unexpected server error. |

**Frontend behavior**
- After `201`: show confirmation screen with copy actions:
  - "Сотрудник создан"
  - Show: name, branch, login email, temporary password
  - Buttons: copy login, copy password, copy all, copy for WhatsApp, done
- Share temporary password via WhatsApp format:
  ```
  Вас пригласили в Ask как сотрудник филиала "Mega Silk Way"
  Логин: staff@example.com
  Временный пароль: Q7K9-M2PA
  Скачайте Ask: [link]
  ```
- Warn owner: "Сохраните пароль сейчас — после активации он станет недоступен."

---

### 2.2. List Staff

```
GET /api/v1/businesses/{businessId}/branches/{branchId}/staff
Auth: Bearer token (OWNER or MANAGER)
```

**Request body:** none.

**Response `200`: `List<StaffResponse>`**

Array of `StaffResponse` objects. See fields in 2.1.

Key frontend logic for each staff card:

| Staff status | Password visibility | Card shows |
|--------------|---------------------|------------|
| `PENDING_ACTIVATION` | Visible | tempPassword, "Ожидает активации", copy actions, delete invite button |
| `ACTIVE` | Hidden | activatedAt timestamp, "Активен", change role button, reset password button, disable button |
| `PASSWORD_RESET_REQUIRED` | Visible | new tempPassword, "Требуется смена пароля", copy actions |
| `DISABLED` | Hidden | "Заблокирован", enable button |

**Error responses**

| Status | ErrorCode | When |
|--------|-----------|------|
| `403` | `ACCESS_DENIED` | Caller is not owner. |
| `404` | `BRANCH_NOT_FOUND` | Branch doesn't exist. |
| `500` | `INTERNAL_ERROR` | Unexpected server error. |

**Frontend behavior**
- Show staff list with role badge (Сотрудник).
- Show status chip with color: yellow for pending, green for active, orange for reset required, red for disabled.
- Show temp password inline only for `PENDING_ACTIVATION` and `PASSWORD_RESET_REQUIRED`.
- For `ACTIVE`: show "Сотрудник уже активировал аккаунт. Временный пароль больше недоступен."

---

### 2.3. Update Staff

```
POST /api/v1/businesses/{businessId}/branches/{branchId}/staff/{staffId}/update
Auth: Bearer token (OWNER or MANAGER)
```

Update staff role or status (disable/enable).

**Request: `UpdateStaffRequest`**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `role` | `string` | - | Removed — no role picker. Staff has only one role. |
| `status` | `string` | no | `"DISABLED"` to disable. May support `"ACTIVE"` for re-enable. |

At least one field must be provided.

**Response `200`: `StaffResponse`**

Updated staff record. Same shape as create response.

**Error responses**

| Status | ErrorCode | When |
|--------|-----------|------|
| `400` | `REGISTRATION_PAYLOAD_ERROR` | No fields provided or invalid values. |
| `403` | `ACCESS_DENIED` | Caller is not owner. |
| `404` | `BRANCH_NOT_FOUND` | Branch doesn't exist. |
| `404` | `STAFF_NOT_FOUND` | Staff member doesn't exist. |
| `500` | `INTERNAL_ERROR` | Unexpected server error. |

**Frontend behavior**
- Staff role is always STAFF — no role change action needed.
- Disable: show confirmation dialog "Заблокировать сотрудника? Он не сможет войти в аккаунт."
- After update: refresh staff list.

---

### 2.4. Reset Staff Password

```
POST /api/v1/businesses/{businessId}/branches/{branchId}/staff/{staffId}/reset-password
Auth: Bearer token (OWNER or MANAGER)
```

Owner/manager generates a new temporary password for a staff member.

**Request body:** none.

**Response `200`: `StaffResponse`**

Staff record with:
- `status`: `"PASSWORD_RESET_REQUIRED"`
- `tempPassword`: new temporary password (visible again)
- `activatedAt`: unchanged from before

**Error responses**

| Status | ErrorCode | When |
|--------|-----------|------|
| `403` | `ACCESS_DENIED` | Caller is not owner. |
| `404` | `BRANCH_NOT_FOUND` | Branch doesn't exist. |
| `404` | `STAFF_NOT_FOUND` | Staff member doesn't exist. |
| `500` | `INTERNAL_ERROR` | Unexpected server error. |

**Frontend behavior**
- Show confirmation: "Сгенерировать новый временный пароль для [name]? Текущий пароль перестанет работать."
- After success: show same copy-actions screen as staff creation.
- The staff member will be forced to change password on next login.

---

## 3. Invite Management Endpoints

Base path: `/api/v1/businesses/{businessId}/branches/{branchId}/invites`

All endpoints require `ROLE_BUSINESS_OWNER` authority.

Note: for MVP, direct staff creation (section 2) is the primary path. Invite codes are secondary.

---

### 3.1. Create Invite

```
POST /api/v1/businesses/{businessId}/branches/{branchId}/invites
Auth: Bearer token (OWNER or MANAGER)
```

Create a shareable invite code.

**Request: `CreateInviteRequest`**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `role` | `string` | yes | Always `"STAFF"`. |
| `maxUses` | `integer` | no | Max activations before code expires. Unlimited if `null`. |

**Response `201`: `InviteResponse`**

| Field | Type | Notes |
|-------|------|-------|
| `id` | `uuid` | Invite ID. |
| `code` | `string` | Opaque invite code string. |
| `role` | `string` | `"STAFF"`. |
| `maxUses` | `integer` | Max uses (`null` = unlimited). |
| `useCount` | `integer` | How many times used so far. |
| `expiresAt` | `datetime` | Expiry timestamp. |
| `revokedAt` | `datetime` | `null` unless revoked. |

**Error responses**

| Status | ErrorCode | When |
|--------|-----------|------|
| `400` | `REGISTRATION_PAYLOAD_ERROR` | Invalid request. |
| `403` | `ACCESS_DENIED` | Caller is not owner. |
| `404` | `BRANCH_NOT_FOUND` | Branch doesn't exist. |
| `500` | `INTERNAL_ERROR` | Unexpected server error. |

**Frontend behavior**
- After create: show invite code with copy action.
- Share format similar to staff creation but with invite code:
  ```
  Приглашение в Ask как сотрудник филиала "Mega Silk Way"
  Код приглашения: ABC123XYZ
  Скачайте Ask: [link]
  ```

---

### 3.2. List Invites

```
GET /api/v1/businesses/{businessId}/branches/{branchId}/invites
Auth: Bearer token (OWNER or MANAGER)
```

**Request body:** none.

**Response `200`: `List<InviteResponse>`**

Array of invite objects. See fields in 3.1.

**Error responses**

| Status | ErrorCode | When |
|--------|-----------|------|
| `403` | `ACCESS_DENIED` | Caller is not owner. |
| `404` | `BRANCH_NOT_FOUND` | Branch doesn't exist. |
| `500` | `INTERNAL_ERROR` | Unexpected server error. |

**Frontend behavior**
- Show invite list with: code, usage (e.g. "2/5"), expiry, status chip (active/revoked/expired).
- Active codes show "Revoke" action.
- Revoked codes: strikethrough, no actions.

---

### 3.3. Revoke Invite

```
DELETE /api/v1/businesses/{businessId}/branches/{branchId}/invites/{inviteId}
Auth: Bearer token (OWNER or MANAGER)
```

Revoke an invite code. Already-activated users are unaffected.

**Request body:** none.

**Response `204`: No Content**

Empty body. Frontend removes the invite from the list.

**Error responses**

| Status | ErrorCode | When |
|--------|-----------|------|
| `403` | `ACCESS_DENIED` | Caller is not owner. |
| `404` | `BRANCH_NOT_FOUND` | Branch doesn't exist. |
| `404` | `STAFF_NOT_FOUND` | Invite doesn't exist (reuses error — invite not found). |
| `500` | `INTERNAL_ERROR` | Unexpected server error. |

**Frontend behavior**
- Show confirmation: "Отозвать приглашение? Новые сотрудники не смогут его использовать."
- On `204`: remove from list. Show toast "Приглашение отозвано."

---

## 4. Error Response Format

All error responses follow this structure:

```json
{
  "timestamp": "2026-06-21T10:30:00Z",
  "errorCode": "INVALID_CREDENTIALS",
  "message": "Неверный email или пароль",
  "errors": null
}
```

**`ErrorResponse`**

| Field | Type | Notes |
|-------|------|-------|
| `timestamp` | `datetime` | Error timestamp (ISO 8601). |
| `errorCode` | `string` | Machine-readable code (see 4.1). |
| `message` | `string` | Human-readable Russian message. |
| `errors` | `array` of `ErrorDetail` | Field-level errors (nullable). Present for validation errors. |

**`ErrorDetail`**

| Field | Type | Notes |
|-------|------|-------|
| `field` | `string` | Field name with error. |
| `message` | `string` | Per-field error message. |

### 4.1. Error Code Reference

| ErrorCode | HTTP Status | Default Message |
|-----------|-------------|-----------------|
| `USER_NOT_FOUND` | `404` | Пользователь не найден |
| `CHALLENGE_NOT_FOUND` | `404` | Код подтверждения не найден |
| `EMAIL_ALREADY_REGISTERED` | `409` | Email уже зарегистрирован |
| `PHONE_ALREADY_REGISTERED` | `409` | Телефон уже зарегистрирован |
| `EMAIL_ALREADY_EXISTS` | `409` | Пользователь с таким email уже существует |
| `INVALID_CREDENTIALS` | `401` | Неверный email или пароль |
| `ACCOUNT_NOT_ACTIVE` | `403` | Аккаунт не активен |
| `ROLE_MISMATCH` | `403` | Неверная роль аккаунта |
| `SESSION_INVALID` | `401` | Сессия недействительна |
| `PASSWORD_CHANGE_NOT_REQUIRED` | `400` | Смена пароля не требуется |
| `PASSWORDS_DO_NOT_MATCH` | `400` | Пароли не совпадают |
| `CHALLENGE_EXPIRED` | `400` | Код подтверждения истек: {challengeId} |
| `CHALLENGE_MAX_ATTEMPTS` | `400` | Превышено количество попыток: {challengeId} |
| `CHALLENGE_INVALID_CODE` | `400` | Неверный код подтверждения: {challengeId} |
| `REGISTRATION_PAYLOAD_ERROR` | `400` | Ошибка данных регистрации |
| `CITY_NOT_FOUND` | `404` | Город не найден |
| `BRANCH_NOT_FOUND` | `404` | Филиал не найден |
| `STAFF_NOT_FOUND` | `404` | Сотрудник не найден |
| `ACCESS_DENIED` | `403` | Доступ запрещен |
| `DELIVERY_FAILED` | `502` | Ошибка отправки кода подтверждения |
| `INTERNAL_ERROR` | `500` | Внутренняя ошибка сервера |

### 4.2. HTTP Status Summary

| Status | Meaning | Frontend action |
|--------|---------|-----------------|
| `200` | Success | Process response body. |
| `201` | Created | Process response body, update local list. |
| `204` | No content | Remove item from list. |
| `400` | Bad request | Show error message, allow correction. |
| `401` | Unauthorized | Clear token, redirect to login. |
| `403` | Forbidden | Show "access denied", do not retry. |
| `404` | Not found | Show "not found", suggest navigation back. |
| `409` | Conflict | Show inline field error (e.g. "email taken"). |
| `500` | Server error | Show "something went wrong", offer retry. |
| `502` | Bad gateway | Show "delivery failed", offer retry. |

---

## 5. Auth State Reference

### 5.1. Session TTLs

| Session type | TTL | Config key |
|--------------|-----|------------|
| Customer session | 24h (86400s) | `auth.customer.session.ttl` |
| Customer remembered | 30d (2592000s) | `auth.customer.remembered-session.ttl` |
| Business session | 24h (86400s) | `auth.business.session.ttl` |
| Business remembered | 30d (2592000s) | `auth.business.remembered-session.ttl` |
| Staff session | 24h (86400s) | `auth.staff.session.ttl` |
| Staff remembered | 30d (2592000s) | `auth.staff.remembered-session.ttl` |
| Staff activation | 5 min (300s) | `auth.staff.activation-session.ttl` |

### 5.2. Authority Strings

| Role | Authority string | Notes |
|------|-----------------|-------|
| Customer | `ROLE_CUSTOMER` | End-user searching. |
| Business owner | `ROLE_BUSINESS_OWNER` | Owns business, manages branches and Staff, can enter branch workspace. |
| Business staff | `ROLE_BUSINESS_STAFF` | Works inside assigned branch workspace only. |

`ROLE_BUSINESS_MANAGER` and `ROLE_BUSINESS_OPERATOR` are removed and must not be used.

### 5.3. User Statuses

| Status | Display (Russian) | Can log in? | Notes |
|--------|-------------------|-------------|-------|
| `PENDING` | Ожидает | No | Legacy — pre-verification. |
| `ACTIVE` | Активен | Yes | Normal active account. |
| `BLOCKED` | Заблокирован | No | Access revoked. |
| `DELETED` | Удален | No | Soft-deleted. |
| `PENDING_ACTIVATION` | Ожидает активации | Limited | Staff created by owner, not yet activated. Can log in with temp password, must change it. |
| `PASSWORD_RESET_REQUIRED` | Требуется смена пароля | Limited | Owner reset staff password. Can log in with new temp password, must change it. |
| `DISABLED` | Заблокирован | No | Staff access manually disabled by owner. |

### 5.4. Staff Status Lifecycle

```
PENDING_ACTIVATION ──(staff logs in + changes password)──→ ACTIVE
ACTIVE ──(owner resets password)──→ PASSWORD_RESET_REQUIRED
PASSWORD_RESET_REQUIRED ──(staff logs in + changes password)──→ ACTIVE
ACTIVE ──(owner disables)──→ DISABLED
DISABLED ──(owner re-enables)──→ ACTIVE (or PASSWORD_RESET_REQUIRED)
```

### 5.5. Start Routes

| Role | `startRoute` |
|------|-------------|
| `ROLE_CUSTOMER` | `CLIENT_SEARCH` |
| `ROLE_BUSINESS_OWNER` | `OWNER_BRANCHES` |
| `ROLE_BUSINESS_STAFF` | `BRANCH_WORKSPACE` |

### 5.6. Challenge Config

| Setting | Default | Notes |
|---------|---------|-------|
| Code length | 6 digits | `auth.challenge.code-length` |
| Max attempts | 5 | `auth.challenge.max-attempts` |
| TTL | 300s (5 min) | `auth.challenge.ttl` |

---

## 6. Frontend Routing Map

| Route | Screen | Auth required | Notes |
|-------|--------|---------------|-------|
| `/auth` | Auth choice (customer/business) | No | Entry point. |
| `/auth/customer/login` | Customer login form | No | Email/phone + remember me. |
| `/auth/customer/register` | Customer registration form | No | Name + email/phone + password + agreement. |
| `/auth/business/login` | Business login form | No | Email/phone + remember me. |
| `/auth/business/register` | Business registration form | No | Multi-step: credentials → business → confirm. |
| `/auth/verify` | Code verification | No | 6-digit input + countdown. |
| `/auth/login` | Unified password login | No | Email + password. Fallback/legacy login. |
| `/change-password` | Set permanent password | Activation session | Shown when `activationRequired: true`. |
| `/search` | Customer search | Customer | `CLIENT_SEARCH` |
| `/business/branches` | Branch management | Owner | `OWNER_BRANCHES` |
| `/business/activity` | Branch workspace activity | Owner, Staff | `BRANCH_WORKSPACE` |
| `/business/products` | Product management | Owner, Staff | |
| `/business/services` | Service management | Owner, Staff | |
| `/business/staff` | Staff list | Owner | |
| `/business/invites` | Invite codes | Owner | |
| `/business/profile` | Branch profile | Owner, Staff | |

---

## 7. Auth Header Convention

All authenticated requests must include:

```
Authorization: Bearer <accessToken>
```

The `accessToken` comes from `AuthSessionResponse.accessToken` (verify, login, change-password, session endpoints).

Token type is always `"Bearer"` as indicated by `AuthSessionResponse.tokenType`.
