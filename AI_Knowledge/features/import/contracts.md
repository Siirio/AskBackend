# Import — REST API Contracts

## Excel Import
Base: /api/v1/business-admin/branches/{branchId}/product-imports

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /product-imports | OWNER/WORKER | Upload .xlsx → parse → auto-map → MAPPING_REQUIRED |
| POST | /product-imports/{importId}/mapping | OWNER/WORKER | Save mapping → normalize → PREVIEW_READY |
| GET | /product-imports/{importId}/preview | OWNER/WORKER | Get preview state |
| POST | /product-imports/{importId}/approve | OWNER/WORKER | Create Product + Offer + SearchDocument → IMPORTED |
| POST | /product-imports/{importId}/cancel | OWNER/WORKER | Cancel → CANCELLED |

## AI Autodump Import
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /api/v1/business-admin/branches/{branchId}/autodump-sessions/files | Assigned platform importer | Upload .txt/.md/.pdf for AI processing |

## Managed Import
- `POST /api/v1/businesses/{businessId}/managed-imports` creates a PENDING request without a grant or chat. Request requires `catalog_scope` = PRODUCTS, SERVICES, or BOTH, plus source types, source details, contact channel/value, and legal acceptance.
- `POST /api/v1/platform/managed-imports/{requestId}/activate` assigns the request, creates the seven-day grant for the request's catalog scope, and starts the chat.
- `GET /api/v1/platform/managed-imports/businesses/{businessId}/catalog-access` returns `allowed` and the active `catalogScope`; the assigned importer can edit products and/or services inside that scope.
- There is no manual completion endpoint. Expiry records product count, revokes the grant, and deletes chat attachments.

## Import Statuses
CatalogImportStatus: UPLOADED → MAPPING_REQUIRED → PREVIEW_READY → IMPORTED / FAILED / CANCELLED
RawRowStatus: PENDING, VALID, WARNING, INVALID
TargetField: NAME, CATEGORY_LABEL, DESCRIPTION, SKU, PRICE, TAGS, IGNORE, APPEND_TO_DESCRIPTION, CHARACTERISTIC
