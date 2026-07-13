# Catalog — REST API Contracts

## Client Product Endpoints
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /api/v1/products | No | Search/list products |
| GET | /api/v1/products/{productOfferId} | No | Product detail |

## Business Admin Product Endpoints
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | /api/v1/business-admin/branches/{branchId}/products | OWNER/MANAGER/STAFF | List branch products |
| POST | /api/v1/business-admin/branches/{branchId}/products | OWNER/MANAGER/STAFF | Create product + offer |
| GET | /api/v1/business-admin/branches/{branchId}/products/{offerId} | OWNER/MANAGER/STAFF | Get product detail |
| PATCH | /api/v1/business-admin/branches/{branchId}/products/{offerId} | OWNER/MANAGER/STAFF | Update product |
| DELETE | /api/v1/business-admin/branches/{branchId}/products/{offerId} | OWNER/MANAGER/STAFF | Delete/disable product |

## Product Offer Model
- Product (business-owned SKU): name, description, category_id, category_label, tags, sku, characteristics_json, status
- ProductOffer (branch-level): product_id, branch_id, price, enabled, status
- product_branch M2M: one product can appear in multiple branches

## Key DTOs
- BusinessProductRowResponse: productId, productOfferId, branchId, categoryId, name, description, sku, tags, price, enabled, updatedAt (11 fields, no status)
- CreateProductRequest: name, description, categoryId, categoryLabel, tags, sku, characteristics, price
- UpdateProductRequest: partial update, only non-null fields
