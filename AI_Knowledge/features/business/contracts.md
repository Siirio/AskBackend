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

## Public Reference
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /api/v1/cities | No | List ACTIVE cities |
| GET | /api/v1/categories | No | List root categories |
| GET | /api/v1/categories/{parentId}/subcategories | No | List subcategories |

## Key DTOs
- CreateStaffRequest: name, role (default WORKER), login (email)
- UpdateStaffRequest: role, status
- StaffResponse: id, displayName, email, role, status, branchName, tempPassword (only while pending)
- BranchDto: id, name, cityId, cityName, address, onlineOnly, status
