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
| POST | /api/v1/business-admin/branches/{branchId}/autodump-sessions/files | OWNER/WORKER | Upload .txt/.md/.pdf for AI processing |

## Import Statuses
CatalogImportStatus: UPLOADED → MAPPING_REQUIRED → PREVIEW_READY → IMPORTED / FAILED / CANCELLED
RawRowStatus: PENDING, VALID, WARNING, INVALID
TargetField: NAME, CATEGORY_LABEL, DESCRIPTION, SKU, PRICE, TAGS, IGNORE, APPEND_TO_DESCRIPTION, CHARACTERISTIC
