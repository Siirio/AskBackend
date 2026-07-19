# Identity & Auth

Customer and business authentication: email-based login/registration, Google OAuth, 6-digit verification codes, BCrypt passwords, signed JWT access tokens, revocable server-side sessions, remember-me, and staff activation via temporary passwords.

## Key decisions
- Email is the real MVP verification channel. SMS disabled until real provider connected.
- Customer and business registration/login both use email (phone optional).
- Google OAuth accepts only Google-verified email and reuses the single AppUser for that email. A first-time Google login creates a CUSTOMER identity.
- OAuth uses the backend authorization-code callback, writes a short-lived bridge cookie, and redirects to the configured frontend callback. `GET /auth/session` exchanges that cookie for a signed JWT and clears it.
- Exactly one primary login identifier required. Password stored only as hash.
- Verification codes: 6 digits, stored hashed.
- Access tokens are HS256 JWTs signed with `auth.jwt.secret`. The `sid` claim points to a hashed, revocable server-side session so logout and expiry remain enforceable.
- Remember-me extends session TTL via backend config.
- Successful OTP, password, and Google logins update `lastLoginAt`.
- Business onboarding: creates Business + BusinessBranch + BusinessMember + BusinessContact.
- Staff do NOT self-register. Owner creates staff → staff activates via login + password change.
- Roles: CUSTOMER, BUSINESS_OWNER, BUSINESS_MANAGER, BUSINESS_WORKER, PLATFORM_SUPER_ADMIN, PLATFORM_ADMIN, PLATFORM_MODERATOR.
- BusinessMemberRole: OWNER, MANAGER, WORKER. Hierarchy: OWNER > MANAGER > WORKER.
- Account data export is not part of the profile or account lifecycle surface; permanent deletion remains available.
- Email delivery switch is `AUTH_VERIFICATION_TEST_MODE` (true → LoggingEmailCodeSender/LocalBusinessInvitationEmailSender log instead of SMTP; AuthProcessor exposes test branches). Local profile sets `auth.verification.test-mode: true` directly — Spring Boot does NOT read `.env` (no dotenv dependency). The dead `AUTH_VERIFICATION_EMAIL_ENABLED`/`AUTH_VERIFICATION_SMS_ENABLED` keys were removed from application-local.yml and compose.yml. `AUTH_VERIFICATION_STAGING_BYPASS` only skips code-hash comparison, it does NOT stop the send attempt.
