# Identity — Feature Locks

LOCKED | One identity per normalized email | Password/OTP and Google OAuth resolve the same AppUser. Business, branch, and platform access comes from memberships instead of duplicate role-specific users | identity domain, AuthChallenge, AppUser, Google OAuth
LOCKED | Staff do NOT self-register | No public /auth/staff/register. Staff created by owner, activated via login + password change | StaffManagementProcessor, AuthProcessor
LOCKED | Temp password: BCrypt-hashed for login, AES-encrypted for owner visibility | Never store plain temporary password. Cleared on activation | IdentityServiceImpl, AppUser
LOCKED | Unified login: POST /auth/login works for ALL roles | Single endpoint, not separate per-role login | LoginProcessor, AuthController
LOCKED | Activation session TTL = 5 minutes | Staff must complete password change within this window | auth.* config
