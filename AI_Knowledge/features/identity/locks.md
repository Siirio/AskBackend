# Identity — Feature Locks

LOCKED | Email-only auth for MVP | SMS disabled until real provider. Phone removed from AppUser/AuthChallenge V8 | identity domain, AuthChallenge, AppUser
LOCKED | Staff do NOT self-register | No public /auth/staff/register. Staff created by owner, activated via login + password change | StaffManagementProcessor, AuthProcessor
LOCKED | Temp password: BCrypt-hashed for login, AES-encrypted for owner visibility | Never store plain temporary password. Cleared on activation | IdentityServiceImpl, AppUser
LOCKED | Unified login: POST /auth/login works for ALL roles | Single endpoint, not separate per-role login | LoginProcessor, AuthController
LOCKED | Activation session TTL = 5 minutes | Staff must complete password change within this window | auth.* config
