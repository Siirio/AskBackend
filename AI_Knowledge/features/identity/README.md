# Identity & Auth

Customer and business authentication: email-based login/registration, Google OAuth for customer accounts, 6-digit verification codes, BCrypt passwords, Bearer session tokens, remember-me, and staff activation via temporary passwords.

## Key decisions
- Email is the real MVP verification channel. SMS disabled until real provider connected.
- Customer and business registration/login both use email (phone optional).
- Google OAuth accepts only Google-verified email and creates or reuses the CUSTOMER role. It never creates staff or business roles.
- OAuth uses the backend authorization-code callback and returns the ASK opaque session token to the configured frontend callback in the URL fragment.
- Exactly one primary login identifier required. Password stored only as hash.
- Verification codes: 6 digits, stored hashed.
- Session tokens: random opaque Bearer tokens, token hash stored.
- Remember-me extends session TTL via backend config.
- Successful OTP, password, and Google logins update `lastLoginAt`.
- Business onboarding: creates Business + BusinessBranch + BusinessMember + BusinessContact.
- Staff do NOT self-register. Owner creates staff → staff activates via login + password change.
- Roles: CUSTOMER, BUSINESS_OWNER, BUSINESS_MANAGER, BUSINESS_WORKER, PLATFORM_SUPER_ADMIN, PLATFORM_ADMIN, PLATFORM_MODERATOR.
- BusinessMemberRole: OWNER, MANAGER, WORKER. Hierarchy: OWNER > MANAGER > WORKER.
