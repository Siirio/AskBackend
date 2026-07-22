# Business — Frontend UX Expectations

## Business cabinet sections (MVP)
- Activity (shared customer conversations)
- Items
- Services
- Company/branch profile
- Future: AI Autodump Import preview

## Registration and categories

1. The owner registers a Business, chooses its scope (`ITEM`, `SERVICE`, or `BOTH`), and chooses or creates one `BUSINESS` category.
2. A branch may be added during registration or later; it is not required for Item or Service creation.
3. Item forms use only `ITEM` category suggestions; service forms use only `SERVICE` category suggestions.
4. Typing shows matching system and user-created categories. The user may explicitly create a category when no suggestion fits.
5. Existing business members go to their cabinet instead of seeing another create-business entry.
6. Choosing no specified legal form requires at least one valid verification link before the owner can continue.
7. The managed-import request dialog asks only how the Ask team should make contact and validates the contact against the selected channel. It does not ask for onboarding sources or service acceptance again.

## Staff management flow
1. Owner opens branch → Staff tab → "Add Staff"
2. Fills name, role (STAFF), login email
3. Confirmation screen with temp password + copy actions (copy login, copy password, copy all, WhatsApp message)
4. Staff receives credentials → opens app → login form → enters email + temp password
5. App detects activationRequired=true → password change screen ("Создайте новый пароль")
6. After password change → enters workspace with appropriate startRoute

## Hierarchy rules
- OWNER: full access to all branches, can create MANAGER + WORKER
- MANAGER: can manage Items/Services for the business and all branches, can create WORKER only
- WORKER: branch-limited access, cannot manage other staff

## Branch profile
- Branch has: name, city, address, onlineOnly flag, coordinates (optional)
- Online-only branches can skip physical address
- Contacts managed per-branch (registration contact = initial public contact)
- Branch creation/editing has no manual city dropdown or address field: the map selection resolves both values. Only `addressDetails` is entered separately.
- Staff creation endpoints create and return the actual member immediately; invitations are a separate workflow.
