# Task 04: Business Admin Service Offer And Booking Endpoints

## Goal

Create business-admin-facing service endpoints for managing service definitions, branch-level service offers, optional scheduling resources, and booking triage.

## Required Foundation

This task depends on:

- `Business`
- `BusinessMember`
- `BusinessBranch`
- `Category`
- `ServiceOffering`
- `ServiceBranchOffer`
- `ServiceResource`
- `ResourceServiceAssignment`
- `ServiceSchedule`
- `ServiceWindow`
- `Booking`
- `SearchDocument`
- `ConversationLink`

## Authorization Rules

- Caller must be an active `BusinessMember` of the target business.
- Role must allow service or booking management.
- Business and branch must be active.
- Admin endpoints must never allow managing another business by guessing IDs.

## Endpoints

### List Business Services

`GET /api/v1/business-admin/businesses/{businessId}/services`

Query:

- `categoryId`
- `status`
- `query`
- `page`
- `size`

Response: `BusinessAdminServiceListResponse`

- `items`
- `page`

`BusinessAdminServiceRowResponse`:

- `serviceOfferingId`
- `categoryId`
- `name`
- `description`
- `status`
- `branchOfferCount`
- `activeBranchOfferCount`
- `updatedAt`

Rules:

- Return service definitions owned by the business only.
- Services are not products.
- Do not return JPA entities.

### Create Service

`POST /api/v1/business-admin/businesses/{businessId}/services`

Request: `CreateBusinessAdminServiceRequest`

- `categoryId`
- `name`
- `description`
- `status`

Response: `BusinessAdminServiceResponse`

- `serviceOfferingId`
- `businessId`
- `categoryId`
- `name`
- `description`
- `status`
- `createdAt`
- `updatedAt`

Rules:

- `ServiceOffering` is a business-owned definition.
- It is not directly searchable until at least one active `ServiceBranchOffer` exists.

### Update Service

`PATCH /api/v1/business-admin/businesses/{businessId}/services/{serviceOfferingId}`

Request: `UpdateBusinessAdminServiceRequest`

- `categoryId`
- `name`
- `description`
- `status`

Response: `BusinessAdminServiceResponse`

Rules:

- Updating service search fields must refresh search documents for related active branch offers.
- Deactivating a service must deactivate related searchable branch offers or prevent them from appearing in client search.

### List Service Branch Offers

`GET /api/v1/business-admin/businesses/{businessId}/service-offers`

Query:

- `branchId`
- `serviceOfferingId`
- `serviceMode`
- `status`
- `page`
- `size`

Response: `BusinessAdminServiceOfferListResponse`

- `items`
- `page`

`BusinessAdminServiceOfferRowResponse`:

- `serviceBranchOfferId`
- `serviceOfferingId`
- `serviceName`
- `branchId`
- `branchName`
- `serviceMode`
- `basePrice`
- `durationMinutes`
- `availabilityConfidence`
- `confirmationPolicy`
- `status`
- `updatedAt`

### Create Service Branch Offer

`POST /api/v1/business-admin/businesses/{businessId}/service-offers`

Request: `CreateBusinessAdminServiceOfferRequest`

- `serviceOfferingId`
- `branchId`
- `serviceMode`
- `basePrice`
- `durationMinutes`
- `availabilityConfidence`
- `confirmationPolicy`
- `status`

Response: `BusinessAdminServiceOfferResponse`

- `serviceBranchOfferId`
- `serviceOfferingId`
- `branchId`
- `serviceMode`
- `basePrice`
- `durationMinutes`
- `availabilityConfidence`
- `confirmationPolicy`
- `status`
- `createdAt`
- `updatedAt`

Rules:

- `serviceOfferingId` must belong to the same business.
- `branchId` must belong to the same business.
- `ON_DEMAND` offers must not require resources, schedules, or windows.
- `SCHEDULED` offers may use resources, schedules, and windows.
- Creating or activating an offer must create or refresh its `SearchDocument`.

### Update Service Branch Offer

`PATCH /api/v1/business-admin/businesses/{businessId}/service-offers/{serviceBranchOfferId}`

Request: `UpdateBusinessAdminServiceOfferRequest`

- `serviceMode`
- `basePrice`
- `durationMinutes`
- `availabilityConfidence`
- `confirmationPolicy`
- `status`

Response: `BusinessAdminServiceOfferResponse`

Rules:

- Updating service mode or confirmation policy must refresh search document summary and availability confidence.
- Deactivating an offer must deactivate its search document.
- Do not promise booking availability unless schedule or manual confirmation rules support it.

### Manage Service Resources

`POST /api/v1/business-admin/businesses/{businessId}/service-resources`

Request: `CreateBusinessAdminServiceResourceRequest`

- `branchId`
- `name`
- `resourceType`
- `status`

Response: `BusinessAdminServiceResourceResponse`

- `serviceResourceId`
- `branchId`
- `name`
- `resourceType`
- `status`

Rules:

- Resource is abstract capacity only.
- Do not create specialist accounts, payroll, staff login, or specialist UI assumptions.

### Manage Service Windows

`POST /api/v1/business-admin/businesses/{businessId}/service-schedules/{serviceScheduleId}/windows`

Request: `CreateBusinessAdminServiceWindowRequest`

- `startsAt`
- `endsAt`
- `windowType`
- `status`

Response: `BusinessAdminServiceWindowResponse`

- `serviceWindowId`
- `serviceScheduleId`
- `startsAt`
- `endsAt`
- `windowType`
- `status`

Rules:

- Windows represent scheduling truth only when maintained by business admin or trusted integration.
- Blocked windows must not appear as bookable client slots.

### List Bookings

`GET /api/v1/business-admin/businesses/{businessId}/bookings`

Query:

- `branchId`
- `status`
- `from`
- `to`
- `page`
- `size`

Response: `BusinessAdminBookingListResponse`

- `items`
- `page`

`BusinessAdminBookingRowResponse`:

- `bookingId`
- `serviceBranchOfferId`
- `serviceName`
- `branchId`
- `customerId`
- `status`
- `requestedStartAt`
- `confirmedStartAt`
- `confirmedEndAt`
- `messageCount`
- `updatedAt`

### Update Booking Status

`PATCH /api/v1/business-admin/businesses/{businessId}/bookings/{bookingId}`

Request: `UpdateBusinessAdminBookingRequest`

- `status`
- `confirmedStartAt`
- `confirmedEndAt`
- `providerNote`

Response: `BusinessAdminBookingResponse`

Rules:

- Allowed lifecycle is pending to confirmed, pending to cancelled, confirmed to completed, confirmed to cancelled.
- Business confirmation can set confirmed start and end.
- Do not convert fallback request into booking without explicit confirmation action.

## Clarifying Logic

- Use `ON_DEMAND` when the service can be handled without slot selection.
- Use `SCHEDULED` when the service needs a requested or confirmed time.
- Use `confirmationPolicy` to tell clients whether they can book directly, request confirmation, or only contact the business.
- If availability is uncertain, lower confidence and require confirmation rather than inventing a slot.

## Implementation Boundaries

- Keep service management in `kz.ask.service`.
- Use `kz.ask.search` only through search document refresh.
- Keep booking lifecycle separate from fallback request lifecycle.
- Do not add specialist user accounts.
- Do not expose entities.
- Do not create tests or run Maven unless explicitly requested.
