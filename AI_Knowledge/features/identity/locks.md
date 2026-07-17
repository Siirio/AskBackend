# Identity — Feature Locks

LOCKED | Email-based auth for MVP | Password/OTP and Google OAuth use verified email. Google first login may create CUSTOMER only; staff and business roles keep their dedicated onboarding | identity domain, AuthChallenge, AppUser, Google OAuth
LOCKED | Staff do NOT self-register | No public /auth/staff/register. Staff created by owner, activated via login + password change | StaffManagementProcessor, AuthProcessor
LOCKED | Temp password: BCrypt-hashed for login, AES-encrypted for owner visibility | Never store plain temporary password. Cleared on activation | IdentityServiceImpl, AppUser
LOCKED | Unified login: POST /auth/login works for ALL roles | Single endpoint, not separate per-role login | LoginProcessor, AuthController
LOCKED | Activation session TTL = 5 minutes | Staff must complete password change within this window | auth.* config
