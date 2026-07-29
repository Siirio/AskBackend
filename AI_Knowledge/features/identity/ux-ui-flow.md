# Identity — Frontend UX Expectations

## Entry points (three distinct paths, not three "registrations")
1. Customer: self-registers via email → verify code → enters search
2. Business owner: self-registers via email with business/branch info → verify code → enters cabinet
3. Staff: created by owner → receives temp password → logs in at standard login form → forced password change → enters workspace

## Login flow (unified)
- POST /auth/login with email + password works for ALL roles
- Every successful login returns a signed Bearer JWT. The client stores it in session-scoped storage and sends it through `Authorization`.
- Normal session: activationRequired=false → enter app
- Activation session (5min TTL): activationRequired=true → navigate to password change screen
- Password change: newPassword + confirmation → creates full session, revokes activation session

## Google OAuth bridge
1. Google redirects to the backend callback.
2. Backend creates a revocable session, writes `ASK_SESSION`, and redirects to `/oauth/callback`.
3. Frontend calls `GET /api/v1/auth/session` with credentials.
4. Backend validates the cookie session, returns `access_token`, `token_type=Bearer`, and `expires_in`, then clears `ASK_SESSION`.
5. A first-time OAuth user with no accepted role documents is redirected to the role-choice screen.
6. All later API requests use the Bearer JWT without cookies.

## Staff activation
- Owner creates staff: fills name, role, email → gets a temporary password that remains visible to authorized managers until the staff user changes it
- Owner sees: copy login / copy password / copy all / copy WhatsApp message
- Staff card (pending): shows name, role, branch, status "Ожидает активации", login visible, temp password visible
- Staff card (active): shows name, role, branch, status "Активен", activated timestamp, password hidden
- Owner can reset password → new temp password → status PASSWORD_RESET_REQUIRED

## Registration
- Customer registration form: displayName, email, password, passwordConfirmation
- After email verification, role choice is mandatory. Customer choice requires `USER_TERMS` and `PRIVACY_POLICY`; seller choice requires `SELLER_TERMS` and `PERSONAL_DATA_CONSENT`.
- Seller onboarding is available only after the seller-role documents were accepted.
- Business: email, password, businessName, branchName, branchCityId, branchAddress, onlineOnly, acceptedBusinessRules
- If onlineOnly=true, no physical branch can be created. The onlineOnly field replaces the ambiguous isOnline/isOnlineOnly split.
- Branch creation is rejected while Business.onlineOnly is true

## Profile
- PATCH /api/v1/auth/profile: partial update (only non-null fields changed)
- Can update displayName, email, phone independently
- Profile settings expose account deletion but no account-data export action.
- Password change opens a dedicated two-step dialog: current/new/confirmation → email code → confirmed change.
- Two-factor enable and disable use a separate two-step dialog with an explicit target state and email code.
- Both verification inputs accept exactly 6 digits, support resend, disable duplicate submissions, and clear locally held passwords and codes when closed.
- `AuthSessionResponse.isTwoFactorEnabled` is the source of truth for the current setting. `requiresTwoFactor` remains a login-challenge signal, not the persisted setting.
