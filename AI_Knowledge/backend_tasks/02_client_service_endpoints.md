# Task 02: Client Service Search, Booking, And Request Endpoints

## Goal

Create client-facing service endpoints that support search-first service discovery, service details, booking when truth exists, and fallback service requests when availability is unknown or confirmation-needed.

## Required Foundation

This task depends on:

- `ServiceOffering`
- `ServiceBranchOffer`
- `ServiceResource`
- `ServiceSchedule`
- `ServiceWindow`
- `Booking`
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

### Search Services

`POST /api/v1/client/services/search`

Request: `ClientServiceSearchRequest`

- `searchSessionId`
- `rawQuery`
- `cityId`
- `categoryId`
- `latitude`
- `longitude`
- `desiredStartAt`
- `filters`
- `page`
- `size`

Response: `ClientServiceSearchResponse`

- `searchSessionId`
- `rawQuery`
- `results`
- `resultCount`
- `fallbackAvailable`
- `fallbackReason`
- `snapshotRequired`

`ClientServiceResultResponse`:

- `serviceBranchOfferId`
- `serviceOfferingId`
- `businessId`
- `branchId`
- `businessName`
- `branchName`
- `serviceName`
- `description`
- `basePrice`
- `durationMinutes`
- `serviceMode`
- `availabilityConfidence`
- `confirmationPolicy`
- `bookingAvailable`
- `distanceMeters`
- `address`
- `mapAvailable`
- `contactActions`

Rules:

- Return only active services, active branch offers, active businesses, and active branches.
- `bookingAvailable` is true only when the service branch offer supports booking and real schedule or manual confirmation flow exists.
- Do not promise exact slots unless `ServiceWindow` or trusted integration data supports it.
- `ON_DEMAND` service offers must work without resources, schedules, or windows.

### Service Offer Details

`GET /api/v1/client/service-offers/{serviceBranchOfferId}`

Response: `ClientServiceOfferDetailsResponse`

- `serviceBranchOfferId`
- `service`
- `business`
- `branch`
- `basePrice`
- `durationMinutes`
- `serviceMode`
- `availabilityConfidence`
- `confirmationPolicy`
- `bookingAvailable`
- `nextWindows`
- `contactActions`
- `chatAvailable`
- `fallbackRequestAvailable`

Rules:

- `nextWindows` can be empty.
- Empty windows do not mean the service is unavailable unless the data source says so.
- If schedule truth is missing, show confirmation-needed behavior.

### Create Service Booking

`POST /api/v1/client/service-bookings`

Request: `CreateClientServiceBookingRequest`

- `searchSessionId`
- `serviceBranchOfferId`
- `requestedStartAt`
- `customerNote`
- `idempotencyKey`

Response: `ClientServiceBookingResponse`

- `bookingId`
- `searchSessionId`
- `serviceBranchOfferId`
- `branchId`
- `status`
- `requestedStartAt`
- `confirmedStartAt`
- `confirmedEndAt`
- `conversationId`

Rules:

- Booking is separate from `CustomerRequest`.
- Booking starts as pending unless trusted schedule/integration confirms the time.
- Booking must not reserve time without branch confirmation or trusted schedule/integration data.
- Idempotency is required by user, service offer, requested time, and `idempotencyKey`.

### Create Service Fallback Request

`POST /api/v1/client/service-requests`

Request: `CreateClientServiceRequestRequest`

- `searchSessionId`
- `rawQuery`
- `cityId`
- `categoryId`
- `serviceBranchOfferId`
- `desiredStartAt`
- `customerNote`
- `targetBranchIds`
- `idempotencyKey`

Response: `ClientServiceRequestResponse`

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
- `serviceBranchOfferId` is optional context.
- Use fallback request when the user needs human confirmation, search confidence is low, or slot truth is missing.
- Do not turn service request into booking unless the business confirms.

### Service Request Response Feed

`GET /api/v1/client/service-requests/{requestId}/responses`

Query:

- `status`
- `page`
- `size`

Response: `ClientServiceResponseFeedResponse`

- `requestId`
- `filters`
- `items`
- `page`

`ClientServiceResponseRowResponse`:

- `supplierResponseId`
- `businessId`
- `branchId`
- `businessName`
- `status`
- `price`
- `serviceHint`
- `distanceMeters`
- `messageCount`
- `updatedAt`
- `details`

Expanded `details`:

- `comment`
- `address`
- `mapAvailable`
- `contactActions`
- `chatThreadId`

Rules:

- Updating one supplier response keeps the same response row.
- Do not duplicate rows for the same request target.
- Chat is scoped to request and branch.

## Clarifying Logic

- `AVAILABLE`: business can provide the requested service or a sufficiently exact match.
- `UNAVAILABLE`: business explicitly cannot provide it.
- `NEED_CLARIFICATION`: business needs date, time, service type, address, specialist preference, or other details.
- `ALTERNATIVE_OFFERED`: exact service/time is unavailable but another option exists.

If clarification is needed:

- keep the request active;
- link chat to the request and branch;
- do not create booking automatically.

## Implementation Boundaries

- Keep service logic explicit and separate from product logic.
- Do not add specialist user accounts.
- Do not add specialist-facing UI assumptions.
- Do not force schedules for `ON_DEMAND` services.
- Do not expose entities.
- Do not create tests or run Maven unless explicitly requested.
