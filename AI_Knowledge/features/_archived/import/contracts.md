# Import — REST API Contracts

## Excel Import
Base: /api/v1/businesses/{businessId}/item-imports. `branchId` is optional.

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /item-imports | OWNER/WORKER | Upload .xlsx → parse → auto-map → MAPPING_REQUIRED |
| POST | /item-imports/{importId}/mapping | OWNER/WORKER | Save mapping → normalize → PREVIEW_READY |
| GET | /item-imports/{importId}/preview | OWNER/WORKER | Get preview state |
| POST | /item-imports/{importId}/approve | OWNER/WORKER | Create Product + Offer + SearchDocument → IMPORTED |
| POST | /item-imports/{importId}/cancel | OWNER/WORKER | Cancel → CANCELLED |

## AI Autodump Import
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /api/v1/business-admin/branches/{branchId}/autodump-sessions/files | Assigned platform importer | Upload .txt/.md/.pdf for AI item or service processing |

## Managed Import
- `POST /api/v1/businesses/{businessId}/managed-imports` creates a PENDING request without a grant or chat. Request follows the entity fields: `businessScope` = `ITEM`, `SERVICE`, or `BOTH`, optional `selectedSourceTypes`, `sourceLinks`, and `sourceNotes`, plus required `preferredContactChannel` and `preferredContactValue`. Contact format must match EMAIL, TELEGRAM, or WHATSAPP. It has no country, locale, or legal-acceptance transport fields.
- `POST /api/v1/platform/managed-imports/{requestId}/activate` assigns the request, creates the seven-day grant for the request's `businessScope`, and starts the managed-import chat.
- `GET /api/v1/platform/managed-imports/businesses/{businessId}/items-services-access` returns `allowed` and the active `businessScope`; the assigned importer can edit items and/or services inside that scope.
- There is no manual completion endpoint. Expiry records item count, revokes the grant, and deletes chat attachments.

## Import Statuses
CatalogImportStatus: UPLOADED → MAPPING_REQUIRED → PREVIEW_READY → IMPORTED / FAILED / CANCELLED
RawRowStatus: PENDING, VALID, WARNING, INVALID
TargetField: NAME, CATEGORY_LABEL, DESCRIPTION, SKU, PRICE, TAGS, IGNORE, APPEND_TO_DESCRIPTION, CHARACTERISTIC
