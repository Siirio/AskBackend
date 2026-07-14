# Identity — REST API Contracts

## Customer Auth
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /api/v1/auth/customer/login/start | No | Start customer login (email or phone) |
| POST | /api/v1/auth/customer/register | No | Register customer |
| POST | /api/v1/auth/verify | No | Verify 6-digit code → AuthSessionResponse |
| POST | /api/v1/auth/login | No | Unified login for ALL roles (email + password) |

## Business Auth
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /api/v1/auth/business/login/start | No | Start business login |
| POST | /api/v1/auth/business/register | No | Register business + branch + owner |
| POST | /api/v1/auth/change-temporary-password | Activation session | Staff first-login password change |

## Session
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /api/v1/auth/session | Bearer | Current session info |
| POST | /api/v1/auth/logout | Bearer | Revoke session |
| POST | /api/v1/auth/profile | Bearer | Update displayName/email/phone |

## Key DTOs
- AuthChallengeResponse: authChallengeId, role, purpose, channel, maskedDestination, expiresAt
- AuthSessionResponse: accessToken, tokenType, expiresAt, remembered, activationRequired, role, startRoute, user (AuthUserResponse), business (AuthBusinessContextResponse, optional), requiresRoleSelection, availableRoles, allRoles, requiresTwoFactor, authChallengeId, suggestRoleExpansion
- AuthUserResponse: userId, displayName, email, status
- AuthBusinessContextResponse: businessId, businessName, branchId, branchName, membershipId, memberRole
- startRoute values: CLIENT_SEARCH, OWNER_BRANCHES, BRANCH_WORKSPACE

## Additional Endpoints
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /api/v1/auth/select-role | No | Select role when account has multiple roles |
| POST | /api/v1/auth/switch-role | Bearer | Switch active role between sessions |
| POST | /api/v1/auth/change-password | Bearer | Change password (authenticated) |
| POST | /api/v1/auth/toggle-2fa | Bearer | Enable/disable two-factor auth |
| GET | /api/v1/auth/email-info | No | Get email provider info for login hint |

## Config
- auth.challenge.ttl, auth.challenge.code-length (6), auth.challenge.max-attempts
- auth.customer.session.ttl, auth.customer.remembered-session.ttl
- auth.business.session.ttl, auth.business.remembered-session.ttl
- auth.verification.email.enabled (true), auth.verification.sms.enabled (false)
