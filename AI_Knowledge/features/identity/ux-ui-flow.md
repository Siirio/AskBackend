# Identity — Frontend UX Expectations

## Entry points (three distinct paths, not three "registrations")
1. Customer: self-registers via email → verify code → enters search
2. Business owner: self-registers via email with business/branch info → verify code → enters cabinet
3. Staff: created by owner → receives temp password → logs in at standard login form → forced password change → enters workspace

## Login flow (unified)
- POST /auth/login with email + password works for ALL roles
- Normal session: activationRequired=false → enter app
- Activation session (5min TTL): activationRequired=true → navigate to password change screen
- Password change: newPassword + confirmation → creates full session, revokes activation session

## Staff activation
- Owner creates staff: fills name, role (STAFF), email → gets temp password visible once
- Owner sees: copy login / copy password / copy all / copy WhatsApp message
- Staff card (pending): shows name, role, branch, status "Ожидает активации", login visible, temp password visible
- Staff card (active): shows name, role, branch, status "Активен", activated timestamp, password hidden
- Owner can reset password → new temp password → status PASSWORD_RESET_REQUIRED

## Registration
- Customer: displayName, email, password, passwordConfirmation, acceptedUserAgreement
- Business: email, password, businessName, branchName, branchCityId, branchAddress, onlineOnly, acceptedBusinessRules
- If onlineOnly=true, physical address optional

## Profile
- PATCH /api/v1/auth/profile: partial update (only non-null fields changed)
- Can update displayName, email, phone independently
- Profile settings expose account deletion but no account-data export action.
