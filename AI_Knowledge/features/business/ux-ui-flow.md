# Business — Frontend UX Expectations

## Business cabinet sections (MVP)
- Activity (shared customer conversations)
- Products (manage catalog)
- Services (manage service offerings)
- Company/branch profile
- Future: AI Autodump Import preview

## Seller catalog onboarding
1. Ask whether the business sells products, provides services, or does both.
2. Ask whether the seller will prepare that catalog independently or request managed import.
3. Managed import explains the paid service, expected benefit, selected sources, source links, notes, and contact channel before legal acceptance.
4. Existing business members go to their cabinet instead of seeing another create-business entry.

## Staff management flow
1. Owner opens branch → Staff tab → "Add Staff"
2. Fills name, role (STAFF), login email
3. Confirmation screen with temp password + copy actions (copy login, copy password, copy all, WhatsApp message)
4. Staff receives credentials → opens app → login form → enters email + temp password
5. App detects activationRequired=true → password change screen ("Создайте новый пароль")
6. After password change → enters workspace with appropriate startRoute

## Hierarchy rules
- OWNER: full access to all branches, can create MANAGER + WORKER
- MANAGER: can manage products/services for all branches, can create WORKER only
- WORKER: branch-limited access, cannot manage other staff

## Branch profile
- Branch has: name, city, address, onlineOnly flag, coordinates (optional)
- Online-only branches can skip physical address
- Contacts managed per-branch (registration contact = initial public contact)
- Branch creation/editing has no manual city dropdown or address field: the map selection resolves both values. Only `addressDetails` is entered separately.
- Staff creation endpoints create and return the actual member immediately; invitations are a separate workflow.
