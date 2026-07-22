# Business — REST API Contracts

## Branches
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /api/v1/businesses/{businessId}/branches | OWNER/MANAGER | List branches |
| POST | /api/v1/businesses/{businessId}/branches | OWNER | Create branch |
| PATCH | /api/v1/businesses/{businessId}/branches/{branchId} | OWNER | Update branch |

## Staff Management
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /api/v1/businesses/{bId}/branches/{brId}/staff | OWNER/MANAGER | Create staff |
| GET | /api/v1/businesses/{bId}/branches/{brId}/staff | OWNER/MANAGER | List staff |
| POST | /api/v1/businesses/{bId}/branches/{brId}/staff/{id}/update | OWNER/MANAGER | Update staff (role, disable) |
| POST | /api/v1/businesses/{bId}/branches/{brId}/staff/{id}/reset-password | OWNER | Reset staff password |

## Invites
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /api/v1/businesses/{bId}/branches/{brId}/invites | OWNER/MANAGER | Create invite |
| GET | /api/v1/businesses/{bId}/branches/{brId}/invites | OWNER/MANAGER | List invites |
| DELETE | /api/v1/businesses/{bId}/branches/{brId}/invites/{id} | OWNER/MANAGER | Revoke invite |

## Members Management
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /api/v1/businesses/{businessId}/members | OWNER/MANAGER | List members (email, displayName, role, status) |
| PATCH | /api/v1/businesses/{businessId}/members/{membershipId} | OWNER | Change role (never to/from OWNER) |
| POST | /api/v1/businesses/{businessId}/members/{membershipId}/deactivate | OWNER, or MANAGER for WORKER | Deactivate member (OWNER protected) |

## Invitations
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /api/v1/businesses/{businessId}/invitations | OWNER (MANAGER/WORKER), MANAGER (WORKER only) | Create invitation by email |
| GET | /api/v1/businesses/{businessId}/invitations | OWNER/MANAGER | List invitations |
| DELETE | /api/v1/businesses/{businessId}/invitations/{invitationId} | OWNER/MANAGER | Revoke pending invitation |
| GET | /api/v1/me/invitations | Bearer | My pending invitations |
| POST | /api/v1/me/invitations/{invitationId}/accept | Bearer | Accept → creates ACTIVE membership |
| POST | /api/v1/me/invitations/{invitationId}/decline | Bearer | Decline |

## Seller Onboarding
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /api/v1/seller/onboarding | Bearer | Create business + OWNER membership. Request includes `catalogScope` = PRODUCTS, SERVICES, or BOTH and response includes managed-import conversationId when applicable |

## Public Reference
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /api/v1/cities | No | List ACTIVE cities |
| GET | /api/v1/cities/resolve | No | Resolve city from coordinates or name |
| GET | /api/v1/categories | No | List root categories |
| GET | /api/v1/categories/{parentId}/subcategories | No | List subcategories |

## Key DTOs
- CreateStaffRequest: name, role (default WORKER), login (email)
- UpdateStaffRequest: role, status
- StaffResponse: id, displayName, email, role, status, branchName, tempPassword (only while pending), activatedAt
- BranchResponse: id, businessId, cityId, cityName, name, address, addressDetails, onlineOnly, status, latitude, longitude

## Catalog Setup
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /api/v1/businesses/{businessId}/catalog-setup | Business member or assigned platform importer | Read IN_PROGRESS, REVIEW_REQUIRED, COMPLETED, or RESTRICTED |

There is no manual completion endpoint. `GET /api/v1/platform/catalog-reviews` lists partial catalogs and `PATCH /api/v1/platform/catalog-reviews/{businessId}` accepts `{ approved }`.
