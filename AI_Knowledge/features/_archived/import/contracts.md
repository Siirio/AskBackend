# Import — REST API Contracts

## Excel Import
Base: /api/v1/businesses/{businessId}/item-imports. `branchId` is optional and `type` is the canonical `ITEM` or `SERVICE`.

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /item-imports | OWNER/MANAGER, branch WORKER, or assigned importer | Upload .xlsx → parse → auto-map → MAPPING_REQUIRED |
| POST | /item-imports/{importId}/mapping | uploader | Save mapping → normalize → PREVIEW_READY |
| GET | /item-imports/{importId}/preview | uploader | Get preview state |
| POST | /item-imports/{importId}/approve | uploader with current access | Create Items or Services and search outbox events → IMPORTED |
| POST | /item-imports/{importId}/cancel | uploader | Cancel → CANCELLED |

## AI Autodump Import
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /api/v1/business-admin/branches/{branchId}/autodump-sessions/files | Assigned platform importer | Upload .txt/.md/.pdf for AI item or service processing |

## Managed Import
- `POST /api/v1/businesses/{businessId}/managed-imports` creates a PENDING request without a grant or chat. Request follows the entity fields: `businessScope` = `ITEM`, `SERVICE`, or `BOTH`, optional `selectedSourceTypes`, `sourceLinks`, and `sourceNotes`, plus required `preferredContactChannel` and `preferredContactValue`. Contact format must match EMAIL, TELEGRAM, or WHATSAPP. It has no country, locale, or legal-acceptance transport fields.
- `POST /api/v1/platform/managed-imports/{requestId}/activate` assigns the request, immediately creates the seven-day Business-scoped catalog entitlement for the request's `businessScope`, and starts the managed-import chat.
- `GET /api/v1/platform/managed-imports/businesses/{businessId}/items-services-access` returns `allowed` and the active `businessScope`; the assigned importer can edit items and/or services inside that scope.
- There is no manual completion endpoint. Expiry records item count, revokes the grant, and deletes chat attachments.

## Import Statuses
CatalogImportStatus: UPLOADED → MAPPING_REQUIRED → PREVIEW_READY → IMPORTED / FAILED / CANCELLED
RawRowStatus: PENDING, VALID, WARNING, INVALID
TargetField: NAME, CATEGORY_LABEL, DESCRIPTION, SKU, PRICE, TAGS, IGNORE, APPEND_TO_DESCRIPTION, CHARACTERISTIC
