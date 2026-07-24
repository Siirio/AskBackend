# Business — REST API Contracts

## Branches
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /api/v1/businesses/{businessId}/branches | OWNER/MANAGER | List branches (wrapped in `BranchListResponse`) |
| POST | /api/v1/businesses/{businessId}/branches | OWNER/MANAGER | Create branch |
| PATCH | /api/v1/branches/{branchId} | OWNER/MANAGER | Update branch |

## Registration
`POST /api/v1/auth/business/register` accepts a business name, `businessScope`, and either
`businessCategoryId` or `businessCategoryName`. Branch fields are optional. Registration creates
the Business, its profile, and OWNER membership in one transaction; a first branch is created only
when branch input is present.

## Authenticated seller onboarding
`POST /api/v1/business/onboarding` is Bearer-authenticated and creates a business for the current
customer. It requires `businessName`, a selected or free-text `categoryId` or `categoryName`,
`countryCode`, `legalForm`, `catalogSetupMode`, and `businessScope` (`ITEM`, `SERVICE`, or `BOTH`).

- For `KZ_IP`, `legalIdentifier` (IIN) and `legalName` are required; the IIN is persisted as `iin`.
- For `KZ_TOO`, `legalIdentifier` (BIN) and `legalName` are required; the BIN is persisted as `bin`.
- For `NONE`, at least one valid `http://` or `https://` verification link is required. The client
  progressively asks for a source type before rendering its link field and blocks continuation
  while every supplied link is invalid or no link is present.
- If `catalogSetupMode` is `ASK_MANAGED_IMPORT`, the cabinet opens the managed-import request
  dialog. The dialog renders only `preferredContactChannel` and `preferredContactValue`; supplied
  verification links are forwarded without rendering source fields again.

## Managed import request
`POST /api/v1/businesses/{businessId}/managed-imports` uses the persisted
`ManagedImportRequest` shape. It requires `businessScope` (`ITEM`, `SERVICE`, or `BOTH`),
`preferredContactChannel`, and `preferredContactValue`. `selectedSourceTypes` and source links are optional;
onboarding forwards already collected verification links without asking the owner to enter them
again. Contact format must match the selected channel: valid email, Telegram username beginning
with `@`, or an international WhatsApp phone number.

## Staff Management
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /api/v1/businesses/{bId}/branches/{brId}/staff | OWNER/MANAGER | Create staff |
| GET | /api/v1/businesses/{bId}/branches/{brId}/staff | OWNER/MANAGER | List staff |
| POST | /api/v1/businesses/{bId}/branches/{brId}/staff/{id}/update | OWNER/MANAGER | Update staff (role, disable) |
| POST | /api/v1/businesses/{bId}/branches/{brId}/staff/{id}/reset-password | OWNER | Reset staff password |
| POST | /api/v1/businesses/{businessId}/staff | OWNER/MANAGER | Create a business member; managers may create workers only |
| GET | /api/v1/businesses/{businessId}/staff | OWNER/MANAGER | List business members, including the encrypted temporary password while password change is required |
| DELETE | /api/v1/businesses/{businessId}/staff/{membershipId} | OWNER/MANAGER | Delete a still-unactivated member request; managers may delete workers only |

## Invites
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /api/v1/businesses/{bId}/branches/{brId}/invites | OWNER/MANAGER | Create invite |
| GET | /api/v1/businesses/{bId}/branches/{brId}/invites | OWNER/MANAGER | List invites |
| DELETE | /api/v1/businesses/{bId}/branches/{brId}/invites/{id} | OWNER/MANAGER | Revoke invite |

## Members Management
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /api/v1/businesses/{businessId}/members | OWNER/MANAGER | List members (wrapped in `BusinessMemberListResponse`) |
| PATCH | /api/v1/members/{membershipId} | OWNER | Change role (never to/from OWNER) |
| POST | /api/v1/members/{membershipId}/deactivate | OWNER, or MANAGER for WORKER | Deactivate member (OWNER protected) |

## Invitations
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /api/v1/businesses/{businessId}/invitations | OWNER (MANAGER/WORKER), MANAGER (WORKER only) | Create invitation by email |
| GET | /api/v1/businesses/{businessId}/invitations | OWNER/MANAGER | List invitations (wrapped in `BusinessInvitationListResponse`) |
| DELETE | /api/v1/invitations/{invitationId} | OWNER/MANAGER | Revoke pending invitation |
| GET | /api/v1/me/invitations | Bearer | My pending invitations (wrapped in `BusinessInvitationListResponse`) |
| POST | /api/v1/me/invitations/{invitationId}/accept | Bearer | Accept → creates ACTIVE membership |
| POST | /api/v1/me/invitations/{invitationId}/decline | Bearer | Decline |

## Public Reference
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /api/v1/categories | No | Suggest flat categories by query and `type` (`BUSINESS`, `ITEM`, or `SERVICE`) |
| POST | /api/v1/categories | Bearer | Create a USER category with `name` and `type` |

## Key DTOs
- CreateStaffRequest: name, role (default WORKER), login (email)
- UpdateStaffRequest: role, status
- StaffResponse: id, displayName, email, role, status, branchName, tempPassword (only while pending), activatedAt
- BranchResponse: id, businessId, cityId, cityName, name, address, addressDetails, latitude, longitude, timeZoneId, weeklyHours, specialHours, openingSummary (computed OPEN/CLOSED/UNKNOWN)
- BranchListResponse: branches (List&lt;BranchResponse&gt;)
- BusinessInvitationResponse: id, businessId, businessName, invitedEmail, invitedRole, invitedByDisplayName, status, expiresAt, branchIds
- BusinessInvitationListResponse: invitations (List&lt;BusinessInvitationResponse&gt;)
- BusinessMemberDto: id, businessId, businessName, userId, email, displayName, role
- BusinessMemberListResponse: members (List&lt;BusinessMemberDto&gt;)
- Branch create/update accepts `timeZoneId` (IANA), `weeklyHours` (DayOfWeek + LocalTime opensAt/closesAt), and `specialHours` (LocalDate + closed/opensAt/closesAt overrides). OWNER or MANAGER may set schedule.
- Branch create/update receives latitude and longitude internally from map selection or a supported map-link resolver. The frontend never asks the user to enter or displays the numeric coordinates. Persistence stays provider-independent.

## Categories

Categories are flat. Each category has exactly one type: `BUSINESS`, `ITEM`, or `SERVICE`, and one source: `SYSTEM` or `USER`.

- The client sends text while the user types; the API returns matching system and user-created suggestions for the requested type.
- Selecting a suggestion stores its category identity.
- If no suggestion fits, the explicit create action creates a `USER` category and stores that identity.
- There are no parent IDs, subcategories, or fallback category trees.
