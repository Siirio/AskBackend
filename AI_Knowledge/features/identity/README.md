# Identity & Auth

Customer and business authentication: email-based login/registration, 6-digit verification codes, BCrypt passwords, Bearer session tokens, remember-me, and staff activation via temporary passwords.

## Key decisions
- Email is the real MVP verification channel. SMS disabled until real provider connected.
- Customer and business registration/login both use email (phone optional).
- Exactly one primary login identifier required. Password stored only as hash.
- Verification codes: 6 digits, stored hashed.
- Session tokens: random opaque Bearer tokens, token hash stored.
- Remember-me extends session TTL via backend config.
- Business onboarding: creates Business + BusinessBranch + BusinessMember + BusinessContact.
- Staff do NOT self-register. Owner creates staff → staff activates via login + password change.
- Roles: CUSTOMER, BUSINESS_OWNER, BUSINESS_MANAGER, BUSINESS_WORKER, PLATFORM_SUPER_ADMIN, PLATFORM_ADMIN, PLATFORM_MODERATOR.
- BusinessMemberRole: OWNER, MANAGER, WORKER. Hierarchy: OWNER > MANAGER > WORKER.
