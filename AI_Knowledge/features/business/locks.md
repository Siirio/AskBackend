# Business — Feature Locks

LOCKED | Staff never self-register | No public registration form for staff. Created by owner, activated by staff | StaffManagementProcessor, InviteProcessor, AuthController
LOCKED | Hierarchy: OWNER > MANAGER > WORKER | MANAGER can create WORKER only. OWNER can create MANAGER + WORKER | StaffManagementProcessor, InviteProcessor
LOCKED | Business registration creates real persisted data | Not throwaway mock. Business, Branch, Member, Contact must persist | BusinessServiceImpl, AuthProcessor
LOCKED | One branch per registration MVP | Multi-branch management deferred | Business registration flow
