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
8. Seller onboarding has four steps: business details, catalog setup, delivery and pickup, and final confirmation. Delivery is the penultimate screen.
9. The owner selects no delivery, selected cities, all Kazakhstan, or worldwide coverage and separately enables or disables pickup. Selected-city coverage requires at least one preset city. Pickup requires at least one map-selected branch before onboarding can finish.
10. Delivery coverage, selected cities, and pickup availability are editable later in the Business profile by OWNER or MANAGER.
11. Pickup branches drafted during onboarding are submitted with onboarding and appear immediately in Business cabinet → Organization after the transaction succeeds.

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
- Branch has: name, city, address, addressDetails, internal coordinates, timeZoneId (IANA), weeklyHours (DayOfWeek + opensAt/closesAt), and specialHours (LocalDate overrides)
- Physical branch requires at minimum name and coordinates; schedule, address, and timezone are optional but required for open-now display
- Business.onlineOnly=true means no physical branch exists; branch creation is rejected while onlineOnly is true
- Contacts managed per-branch (registration contact = initial public contact)
- Branch creation/editing has no manual city dropdown or address field: the map selection resolves both values. Only `addressDetails` is entered separately.
- OWNER and MANAGER may create or edit branches and their opening hours. Numeric latitude and longitude are never exposed as form fields.
- Staff creation endpoints create and return the actual member immediately; invitations are a separate workflow.
- Chat is always available regardless of branch opening state. The frontend shows a neutral message when the branch is closed.
