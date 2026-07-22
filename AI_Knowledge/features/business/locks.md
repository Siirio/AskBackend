# Business — Feature Locks

LOCKED | Staff never self-register | No public registration form for staff. Created by owner, activated by staff | StaffManagementProcessor, InviteProcessor, AuthController
LOCKED | Membership defaults cover every branch | A default business role applies to all current and future branches; a branch override may change the role or deny access | business membership and branch override model
LOCKED | Multiple owners are valid | OWNER may assign OWNER, MANAGER, or WORKER; MANAGER may create/change WORKER only; WORKER cannot manage staff | staff and invitation authorization
LOCKED | Business registration creates real persisted data | Not throwaway mock. Business, Member, and BusinessProfile must persist; a branch is optional | BusinessServiceImpl, AuthProcessor
LOCKED | Item and Service creation never requires a branch | Branch association may be added later for location-specific delivery, price, or visibility | item/service creation flows, branch offer flows
LOCKED | Categories are flat and typed | Every category is exactly BUSINESS, ITEM, or SERVICE and comes from SYSTEM or USER; no parent/child hierarchy exists | Category, category API, item/service/business forms, search interpretation
