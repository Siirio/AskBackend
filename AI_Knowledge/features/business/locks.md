# Business — Feature Locks

LOCKED | Staff never self-register | No public registration form for staff. Created by owner, activated by staff | StaffManagementProcessor, InviteProcessor, AuthController
LOCKED | Membership defaults cover every branch | A default business role applies to all current and future branches; a branch override may change the role or deny access | business membership and branch override model
LOCKED | Multiple owners are valid | OWNER may assign OWNER, MANAGER, or WORKER; MANAGER may create/change WORKER only; WORKER cannot manage staff | staff and invitation authorization
LOCKED | Business registration creates real persisted data | Not throwaway mock. Business, Branch, Member, BusinessProfile must persist | BusinessServiceImpl, AuthProcessor
LOCKED | Registration creates one real initial branch | Additional branches are supported after registration and inherit business membership defaults | Business registration and branch flows
