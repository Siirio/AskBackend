# Identity — REST API Contracts

## Customer Auth
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /api/v1/auth/customer/login/start | No | Start customer login (email or phone) |
| POST | /api/v1/auth/customer/register | No | Register customer |
| POST | /api/v1/auth/verify | No | Verify 6-digit code → AuthSessionResponse |
| POST | /api/v1/auth/login | No | Unified login for ALL roles (email + password) |
| GET | /oauth2/authorization/google | No | Start Google OAuth. Reuses the single identity for a verified email or creates a customer identity |
| GET | /login/oauth2/code/google | Google callback | Complete Google OAuth, write the `ASK_SESSION` bridge cookie, and redirect to configured frontend `/oauth/callback` |

## Business Auth
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /api/v1/auth/business/login/start | No | Start business login |
| POST | /api/v1/auth/business/register | No | Register business + branch + owner |
| POST | /api/v1/auth/change-temporary-password | Activation session | Staff first-login password change |

## Session
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /api/v1/auth/session | Bearer or OAuth bridge cookie | Validate the current server session, return a signed JWT, and clear the bridge cookie |
| POST | /api/v1/auth/logout | Bearer | Revoke session |
| POST | /api/v1/auth/profile | Bearer | Update displayName/email/phone |
| POST | /api/v1/legal/registration-acceptances | Bearer | Accept the active legal documents selected for the chosen registration role |

## Key DTOs
- VerificationResponse: verificationId, role, purpose, channel, maskedDestination, expiresAt
- AuthSessionResponse: accessToken, tokenType, expiresIn, expiresAt, isRemembered, isActivationRequired, role, startRoute, user (AuthUserResponse), business (AuthBusinessContextResponse, optional), requiresRoleSelection, availableRoles, allRoles, requiresTwoFactor, isTwoFactorEnabled, verificationId, suggestRoleExpansion
- AuthUserResponse: userId, displayName, email, phone, status
- AuthBusinessContextResponse: businessId, businessName, branchId, branchName, membershipId, memberRole
- `allRoles` contains the deduplicated union of the AppUser role, all active `businessMemberships[].role` values, and `platformMembership.role` when present.
- `startRoute` is `CLIENT_SEARCH` after normal login, including for users with business memberships. `businessMemberships` exposes the separate cabinets they may open explicitly.

## Additional Endpoints
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /api/v1/auth/select-role | No | Select role when account has multiple roles |
| POST | /api/v1/auth/switch-role | Bearer | Switch active role between sessions |
| POST | /api/v1/auth/password-change/request | Bearer | Validate `currentPassword`, `newPassword`, and `passwordConfirmation`; send a purpose-bound email code |
| POST | /api/v1/auth/password-change/confirm | Bearer | Verify `verificationId` + 6-digit `code`, apply the staged password hash, preserve the current session, and revoke all other sessions |
| POST | /api/v1/auth/two-factor/request | Bearer | Request an email challenge for the explicit `enabled` target state |
| POST | /api/v1/auth/two-factor/confirm | Bearer | Verify `verificationId` + 6-digit `code` and apply the challenge-bound target state |
| GET | /api/v1/auth/email-info | No | Get email provider info for login hint |

Password and two-factor challenges are authenticated, owner-bound, and purpose-bound. The public `/api/v1/auth/verify` endpoint accepts only `LOGIN` and `REGISTER`; it cannot consume security-setting challenges. Resending replaces only a pending challenge with the same user and purpose.

## JSON Wire Format
All API responses and requests use **snake_case** property naming. Jackson is configured with `SNAKE_CASE` in `application.yml` (`spring.jackson.property-naming-strategy: SNAKE_CASE`).

Examples:
- `accessToken` on the wire → `access_token`
- `expiresIn` → `expires_in`
- `verificationId` → `verification_id`
- `requiresRoleSelection` → `requires_role_selection`
- `businessId` → `business_id`

DTO field names in this document use camelCase (Java convention). Always map to snake_case when integrating.

## Account Lifecycle
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| DELETE | /api/v1/account | Bearer | Delete account: anonymize profile + user, deactivate memberships, revoke sessions, delete challenges. 409 ACCOUNT_OWNER_TRANSFER_REQUIRED if sole business OWNER. Records ACCOUNT_DELETION_REQUESTED / ACCOUNT_DELETED significant events |

## Config
- auth.challenge.ttl, auth.challenge.code-length (6), auth.challenge.max-attempts
- auth.challenge.retention (P1D) + auth.challenge.retention-interval (PT1H) — expired-challenge purge scheduler
- auth.customer.session.ttl, auth.customer.remembered-session.ttl
- auth.business.session.ttl, auth.business.remembered-session.ttl
- auth.verification.email.enabled (true), auth.verification.sms.enabled (false)
- auth.oauth2.google.client-id / `OAUTH2_GOOGLE_CLIENT_ID`
- auth.oauth2.google.client-secret / `OAUTH2_GOOGLE_CLIENT_SECRET`
- auth.oauth2.frontend-redirect-uri / `OAUTH2_FRONTEND_REDIRECT_URI`
- auth.jwt.secret / `AUTH_JWT_SECRET` — minimum 256-bit secret for HS256 access-token signing
- Google Console redirect URI: `{backendBaseUrl}/login/oauth2/code/google`
- Customer registration request does not accept legal flags. After verification, the client submits `USER_TERMS` + `PRIVACY_POLICY` for a customer or `SELLER_TERMS` + `PERSONAL_DATA_CONSENT` for a seller.
