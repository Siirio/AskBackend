# Task 03: Business Admin Product Catalog And Offer Endpoints

## Goal

Create business-admin-facing product endpoints for managing concrete products and branch-level product offers without exposing client search internals as admin write APIs.

## Required Foundation

This task depends on:

- `Business`
- `BusinessMember`
- `BusinessBranch`
- `Category`
- `DataSource`
- `Product`
- `ProductOffer`
- `CatalogImport`
- `RawCatalogRow`
- `SearchDocument`

## Authorization Rules

- Caller must be an active `BusinessMember` of the target business.
- Role must allow product or catalog management.
- Business and branch must be active.
- Admin endpoints must never allow managing another business by guessing IDs.

## Endpoints

### List Business Products

`GET /api/v1/business-admin/businesses/{businessId}/products`

Query:

- `categoryId`
- `status`
- `query`
- `page`
- `size`

Response: `BusinessAdminProductListResponse`

- `items`
- `page`

`BusinessAdminProductRowResponse`:

- `productId`
- `categoryId`
- `name`
- `description`
- `sku`
- `tags`
- `status`
- `offerCount`
- `activeOfferCount`
- `updatedAt`

Rules:

- Return products owned by the business only.
- Search by product name, SKU, and tags.
- Do not return JPA entities.

### Create Product

`POST /api/v1/business-admin/businesses/{businessId}/products`

Request: `CreateBusinessAdminProductRequest`

- `categoryId`
- `name`
- `description`
- `sku`
- `tags`
- `status`

Response: `BusinessAdminProductResponse`

- `productId`
- `businessId`
- `categoryId`
- `name`
- `description`
- `sku`
- `tags`
- `status`
- `createdAt`
- `updatedAt`

Rules:

- One concrete sellable variation is one `Product`.
- Do not create product variant tables.
- `tags` support grouping and search language.
- `sku` is optional unless the business uses it.

### Update Product

`PATCH /api/v1/business-admin/businesses/{businessId}/products/{productId}`

Request: `UpdateBusinessAdminProductRequest`

- `categoryId`
- `name`
- `description`
- `sku`
- `tags`
- `status`

Response: `BusinessAdminProductResponse`

Rules:

- Updating product search fields must trigger search document refresh for related active offers.
- Deactivating a product must remove or deactivate related search documents.
- Do not delete product rows for normal admin archive behavior.

### List Product Offers

`GET /api/v1/business-admin/businesses/{businessId}/product-offers`

Query:

- `branchId`
- `productId`
- `status`
- `stockStatus`
- `page`
- `size`

Response: `BusinessAdminProductOfferListResponse`

- `items`
- `page`

`BusinessAdminProductOfferRowResponse`:

- `productOfferId`
- `productId`
- `productName`
- `branchId`
- `branchName`
- `price`
- `stockStatus`
- `stockQuantity`
- `availabilityConfidence`
- `freshnessAt`
- `status`
- `updatedAt`

### Create Product Offer

`POST /api/v1/business-admin/businesses/{businessId}/product-offers`

Request: `CreateBusinessAdminProductOfferRequest`

- `productId`
- `branchId`
- `dataSourceId`
- `price`
- `stockStatus`
- `stockQuantity`
- `availabilityConfidence`
- `freshnessAt`
- `status`

Response: `BusinessAdminProductOfferResponse`

- `productOfferId`
- `productId`
- `branchId`
- `dataSourceId`
- `price`
- `stockStatus`
- `stockQuantity`
- `availabilityConfidence`
- `freshnessAt`
- `status`
- `createdAt`
- `updatedAt`

Rules:

- `productId` must belong to the same business.
- `branchId` must belong to the same business.
- `dataSourceId` must belong to the same business when provided.
- `stockQuantity` is nullable.
- Exact stock can be exposed to clients only when `stockQuantity` was supplied by manual or integration data.
- Creating or activating an offer must create or refresh its `SearchDocument`.

### Update Product Offer

`PATCH /api/v1/business-admin/businesses/{businessId}/product-offers/{productOfferId}`

Request: `UpdateBusinessAdminProductOfferRequest`

- `price`
- `stockStatus`
- `stockQuantity`
- `availabilityConfidence`
- `freshnessAt`
- `status`

Response: `BusinessAdminProductOfferResponse`

Rules:

- Updating offer availability fields must refresh search document confidence and summary.
- Deactivating an offer must deactivate its search document.
- Do not create stock history snapshots in MVP.

## Clarifying Logic

- If a business wants one parent product with flavors or sizes, model each sellable concrete variation as a separate `Product`.
- If stock is unknown, use unknown stock status and lower or confirmation-needed confidence.
- If price is unknown, keep price null and avoid client promises.
- If admin updates only branch-level availability, update `ProductOffer`, not `Product`.

## Implementation Boundaries

- Use `kz.ask.catalog` for products and offers.
- Use `kz.ask.search` only through an indexing processor or search document refresh service.
- Do not expose import internals as the only way to create products.
- Keep catalog import endpoints separate from manual admin endpoints.
- Do not create tests or run Maven unless explicitly requested.
