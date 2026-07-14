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

## Excel Import
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /api/v1/business-admin/branches/{branchId}/product-imports | OWNER/WORKER | Upload .xlsx |
| POST | /api/v1/business-admin/branches/{branchId}/product-imports/{importId}/mapping | OWNER/WORKER | Save column mapping |
| GET | /api/v1/business-admin/branches/{branchId}/product-imports/{importId}/preview | OWNER/WORKER | Get preview |
| POST | /api/v1/business-admin/branches/{branchId}/product-imports/{importId}/approve | OWNER/WORKER | Approve and create |
| POST | /api/v1/business-admin/branches/{branchId}/product-imports/{importId}/cancel | OWNER/WORKER | Cancel import |
