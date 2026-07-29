# Identity & Auth

Customer and business authentication: email-based login/registration, Google OAuth, 6-digit verification codes, BCrypt passwords, signed JWT access tokens, revocable server-side sessions, remember-me, and staff activation via temporary passwords.

## Key decisions
- Email is the real MVP verification channel. SMS disabled until real provider connected.
- Customer and business registration/login both use email (phone optional).
- Google OAuth accepts only Google-verified email and reuses the single AppUser for that email. A first-time Google login creates a CUSTOMER identity.
- OAuth uses the backend authorization-code callback, writes a short-lived bridge cookie, and redirects to the configured frontend callback. `GET /auth/session` exchanges that cookie for a signed JWT and clears it.
- Local OAuth uses `localhost` for authorization, backend callback, frontend callback, and bridge-cookie exchange; mixing `localhost` with `127.0.0.1` loses the host-scoped bridge cookie.
- Customer registration collects legal acceptance only after email verification and role choice. Customers accept `USER_TERMS` and `PRIVACY_POLICY`; sellers accept `SELLER_TERMS` and `PERSONAL_DATA_CONSENT`.
- Exactly one primary login identifier required. Password stored only as hash.
- Verification codes: 6 digits, stored hashed.
- Authenticated password changes and two-factor setting changes use purpose-bound email challenges. Requesting a replacement invalidates only the previous pending challenge for the same user and purpose.
- A password change keeps the current session active and revokes every other session. Two-factor login applies to every account role and does not create a session or update `lastLoginAt` until its code is confirmed.
- Invalid verification attempts and terminal challenge status changes commit independently so an outer authentication transaction cannot reset the configured attempt limit.
- Access tokens are HS256 JWTs signed with `auth.jwt.secret`. The `sid` claim points to a hashed, revocable server-side session so logout and expiry remain enforceable.
- Remember-me extends session TTL via backend config.
- Successful OTP, password, and Google logins update `lastLoginAt`.
- Business onboarding: creates Business + BusinessProfile + OWNER BusinessMember, with an optional initial BusinessBranch.
- AppUser is the personal account. Business and platform memberships are optional work contexts attached to that account; login opens the personal customer context and the user enters a business cabinet explicitly.
- `allRoles` is the deduplicated union of the personal AppUser role, active business membership roles, and the active platform membership role. Context-specific authorization continues to use `businessMemberships` and `platformMembership`.
- Unified password login authenticates the exact AppUser whose password matched. It never verifies one same-email record and silently starts a session for another record.
- Staff do NOT self-register. Owner creates staff → staff activates via login + password change.
- A staff temporary password is BCrypt-hashed for authentication and separately encrypted for owner/manager display. It remains visible in staff lists across later sessions while password change is required and is cleared when the staff user sets a permanent password.
- Authorization roles are centralized as CUSTOMER, OWNER, MANAGER, WORKER, SUPER_ADMIN, ADMIN, and MODERATOR; role groups distinguish customer, business, and platform access.
- Business membership hierarchy: OWNER > MANAGER > WORKER.
- Account data export is not part of the profile or account lifecycle surface; permanent deletion remains available.
- Email delivery switch is `AUTH_VERIFICATION_TEST_MODE` (true → LoggingEmailCodeSender/LocalBusinessInvitationEmailSender log instead of SMTP; AuthProcessor exposes test branches). The local profile sets `auth.verification.test-mode: true` directly and imports the ignored backend `.env` as a properties file for local secrets such as Google OAuth credentials. The dead `AUTH_VERIFICATION_EMAIL_ENABLED`/`AUTH_VERIFICATION_SMS_ENABLED` keys were removed from application-local.yml and compose.yml. `AUTH_VERIFICATION_STAGING_BYPASS` only skips code-hash comparison, it does NOT stop the send attempt.
