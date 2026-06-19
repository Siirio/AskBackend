# Task 01: Client Product Search And Request Endpoints

## Goal

Create client-facing product endpoints that support mobile search-first UX, product result details, and fallback request creation when product data is missing, stale, low-confidence, or confirmation-needed.

## Required Foundation

This task depends on:

- `Product`
- `ProductOffer`
- `Business`
- `BusinessBranch`
- `BusinessContact`
- `SearchDocument`
- `SearchSession`
- `SearchSnapshot`
- `SearchResultSnapshot`
- `CustomerRequest`
- `RequestTarget`
- `SupplierResponse`
- `ConversationLink`

Do not implement search history behavior until Task 00 is complete.

## Endpoints

### Search Products

`POST /api/v1/client/products/search`

Request: `ClientProductSearchRequest`

- `searchSessionId`
- `rawQuery`
- `cityId`
- `categoryId`
- `latitude`
- `longitude`
- `filters`
- `page`
- `size`

Response: `ClientProductSearchResponse`

- `searchSessionId`
- `rawQuery`
- `results`
- `resultCount`
- `fallbackAvailable`
- `fallbackReason`
- `snapshotRequired`

`ClientProductResultResponse`:

- `productOfferId`
- `productId`
- `businessId`
- `branchId`
- `businessName`
- `branchName`
- `productName`
- `description`
- `imageUrl`
- `price`
- `stockStatus`
- `stockQuantity`
- `availabilityConfidence`
- `freshnessAt`
- `distanceMeters`
- `address`
- `mapAvailable`
- `contactActions`
- `sourceType`

Rules:

- Return only active products, active offers, active businesses, and active branches.
- `stockQuantity` is nullable and must be returned only when supplier or integration data provides it.
- `mapAvailable` is true only when branch address or coordinates exist.
- `fallbackAvailable` is true when no result matches, data is stale, confidence is low, stock is unknown, or customer asks for confirmation.
- Do not force the user to choose a concrete SKU before searching.

### Product Offer Details

`GET /api/v1/client/product-offers/{productOfferId}`

Response: `ClientProductOfferDetailsResponse`

- `productOfferId`
- `product`
- `business`
- `branch`
- `price`
- `stockStatus`
- `stockQuantity`
- `availabilityConfidence`
- `freshnessAt`
- `contactActions`
- `chatAvailable`
- `fallbackRequestAvailable`

Rules:

- Details may show a concrete product offer, but the primary search flow must still accept raw natural-language input.
- Do not show exact stock if `stockQuantity` is null.
- Do not invent delivery, courier, or SLA fields.

### Create Product Fallback Request

`POST /api/v1/client/product-requests`

Request: `CreateClientProductRequestRequest`

- `searchSessionId`
- `rawQuery`
- `cityId`
- `categoryId`
- `productOfferId`
- `customerNote`
- `targetBranchIds`
- `idempotencyKey`

Response: `ClientProductRequestResponse`

- `requestId`
- `searchSessionId`
- `rawQuery`
- `status`
- `recipientCount`
- `responseCount`
- `expiresAt`
- `progress`

Rules:

- Preserve `rawQuery`.
- `productOfferId` is optional context, not mandatory SKU selection.
- Dispatch must be idempotent by user and `idempotencyKey`.
- Dispatch targets active eligible branches only.
- Suspended or inactive businesses must not receive dispatch.
- A branch must not receive duplicate request targets for the same request.

### Product Request Status

`GET /api/v1/client/product-requests/{requestId}`

Response: `ClientProductRequestResponse`

- `requestId`
- `rawQuery`
- `status`
- `recipientCount`
- `responseCount`
- `expiresAt`
- `progress`
- `responseFilters`

Rules:

- Use stable machine-readable statuses.
- Frontend owns localization.
- Loading ends after dispatch is sent; waiting for human replies is represented by status and counts.

### Product Response Feed

`GET /api/v1/client/product-requests/{requestId}/responses`

Query:

- `status`
- `page`
- `size`

Response: `ClientProductResponseFeedResponse`

- `requestId`
- `filters`
- `items`
- `page`

`ClientProductResponseRowResponse`:

- `supplierResponseId`
- `businessId`
- `branchId`
- `businessName`
- `status`
- `price`
- `productHint`
- `distanceMeters`
- `messageCount`
- `updatedAt`
- `details`

Expanded `details`:

- `imageUrl`
- `comment`
- `address`
- `mapAvailable`
- `contactActions`
- `chatThreadId`

Rules:

- New supplier responses appear chronologically after earlier responses.
- Updating one supplier response keeps the same response row.
- Do not duplicate response rows for the same request target.
- Compact rows must not duplicate long address or comment.

## Clarifying Logic

- `AVAILABLE`: supplier can offer the item or a sufficiently exact match.
- `UNAVAILABLE`: supplier explicitly cannot offer it.
- `NEED_CLARIFICATION`: supplier needs model, size, flavor, year, article, or other details.
- `ALTERNATIVE_OFFERED`: exact item is unavailable but a similar option exists.

If clarification is needed:

- keep the original request active;
- allow chat linked to the request and branch;
- do not create a second product request automatically.

## Implementation Boundaries

- Use `kz.ask.catalog`, `kz.ask.search`, `kz.ask.request`, and `kz.ask.messaging` through processors.
- Controller validates transport shape only.
- Processor orchestrates search, fallback, dispatch, feed, and chat link context.
- Domain services persist only their own aggregate.
- Do not expose entities.
- Do not create product variant tables.
- Do not create tests or run Maven unless explicitly requested.
