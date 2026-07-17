# Catalog — REST API Contracts

## Client Product Endpoints
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /api/v1/products | No | Search/list products |
| GET | /api/v1/products/{productOfferId} | No | Product detail |

## Business Admin Product Endpoints
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /api/v1/business-admin/branches/{branchId}/products | OWNER/MANAGER/WORKER | List branch products |
| POST | /api/v1/business-admin/branches/{branchId}/products | OWNER/MANAGER/WORKER | Create product + offer |
| PATCH | /api/v1/business-admin/branches/{branchId}/products/{productId} | OWNER/MANAGER/WORKER | Update product |
| DELETE | /api/v1/business-admin/branches/{branchId}/products/{productId} | OWNER/MANAGER/WORKER | Delete/disable product |

## Product Offer Model
- Product (business-owned SKU): name, description, category_id, tags, sku, attributes (JSONB), status
- ProductOffer (branch-level): product_id, branch_id, price, enabled, status
- product_branch M2M: one product can appear in multiple branches

## Key DTOs
- BusinessProductRowResponse: productId, productOfferId, branchId, categoryId, categoryLabel, name, description, sku, tags, price, enabled, updatedAt, imageUrl (12 fields)
- BusinessProductCreateRequest: categoryId, name, description, sku, tags, price, enabled, imageUrl
- BusinessProductUpdateRequest: partial update, only non-null fields

## Capabilities
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /api/v1/businesses/{businessId}/catalog/capabilities | Bearer | Capability set for current user in this business |

- CatalogCapability: MANUAL_PRODUCT_EDIT, EXCEL_IMPORT, AI_DUMPER, AI_ENRICHER, SOURCE_PARSER
- Active business member → MANUAL_PRODUCT_EDIT + EXCEL_IMPORT only
- Platform member with EDIT_CATALOG_DURING_IMPORT + active managed-import grant for the business → all 5
- Everyone else → empty set (endpoint itself is not gated)

## Access Rules
- Product CRUD: manager-or-above of business OR branch staff OR platform-with-grant
- Excel import: business owner OR branch staff OR platform-with-grant
- Autodump (AI_DUMPER): platform-with-grant ONLY — business members lost direct access per spec §11

## Excel Import
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /api/v1/business-admin/branches/{branchId}/product-imports | OWNER/WORKER/PLATFORM | Upload .xlsx |
| POST | /api/v1/business-admin/branches/{branchId}/product-imports/{importId}/mapping | OWNER/WORKER/PLATFORM | Save column mapping |
| GET | /api/v1/business-admin/branches/{branchId}/product-imports/{importId}/preview | OWNER/WORKER/PLATFORM | Get preview |
| POST | /api/v1/business-admin/branches/{branchId}/product-imports/{importId}/approve | OWNER/WORKER/PLATFORM | Approve and create |
| POST | /api/v1/business-admin/branches/{branchId}/product-imports/{importId}/cancel | OWNER/WORKER/PLATFORM | Cancel import |
