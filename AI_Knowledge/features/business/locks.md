# Business — Feature Locks

LOCKED | Staff never self-register | No public registration form for staff. Created by owner, activated by staff | StaffManagementProcessor, InviteProcessor, AuthController
LOCKED | Membership defaults cover every branch | A default business role applies to all current and future branches; a branch override may change the role or deny access | business membership and branch override model
LOCKED | Multiple owners are valid | OWNER may assign OWNER, MANAGER, or WORKER; MANAGER may create/change WORKER only; WORKER cannot manage staff | staff and invitation authorization
LOCKED | Business registration creates real persisted data | Not throwaway mock. Business, Member, and BusinessProfile must persist; a branch is optional | BusinessServiceImpl, AuthProcessor
LOCKED | Item and Service creation never requires a branch | Branch association may be added later for location-specific delivery, price, or visibility | item/service creation flows, branch offer flows
LOCKED | Categories are flat and typed | Every category is exactly BUSINESS, ITEM, or SERVICE and comes from SYSTEM or USER; no parent/child hierarchy exists | Category, category API, item/service/business forms, search interpretation
LOCKED | Branch coordinates are internal map-derived data | Users select a place or provide a supported map link; frontend forms never ask for or display numeric latitude/longitude | branch forms, map resolver, branch persistence
LOCKED | Branch opening hours are owned by the branch aggregate | Schedule is @ElementCollection inside BusinessBranch; no standalone schedule CRUD or artificial domain service | BusinessBranch entity, BranchOpeningHoursPolicy
LOCKED | Business logo and cover are ASK-managed files | Clients upload validated image files and cannot submit media URLs; URL inputs are reserved for external destinations | BusinessProfile request contract, business media storage, business cabinet
