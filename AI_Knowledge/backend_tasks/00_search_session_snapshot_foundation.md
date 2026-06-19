# Task 00: Search Session And Snapshot Foundation

## Goal

Create the missing backend foundation required by the full UX/UI flow before product and service endpoints expose search history, current search state, or snapshot reopening.

## Why This Is Required

The current data architecture supports searchable surfaces through `SearchDocument`, but the UX/UI flow requires a user-owned search lifecycle:

- current search state;
- raw query preservation per search;
- search history;
- fixed result snapshots when a search stops being current;
- reopening old product, service, business, request, booking, and chat context without re-running search against changed data.

Do not implement client history endpoints by querying live `SearchDocument` rows only. That would break snapshot semantics.

## Data Model To Add

### `search_session`

Owned by `kz.ask.search`.

Fields:

- `id`
- `user_id`
- `city_id`
- `category_id`
- `raw_query`
- `scope`
- `status`
- `started_at`
- `last_active_at`
- `snapshot_expires_at`
- `created_at`
- `updated_at`

Rules:

- `raw_query` must preserve the exact user input.
- `scope` distinguishes all, product-first, service-first, and business-first search intent.
- `status` should distinguish active, snapshotted, expired, cancelled, and failed sessions.
- Starting a new current search for the same user must snapshot or close the previous active search.

### `search_snapshot`

Owned by `kz.ask.search`.

Fields:

- `id`
- `search_session_id`
- `user_id`
- `raw_query`
- `scope`
- `city_id`
- `category_id`
- `status`
- `result_count`
- `product_result_count`
- `service_result_count`
- `business_result_count`
- `request_id`
- `booking_id`
- `expires_at`
- `created_at`
- `updated_at`

Rules:

- Snapshot rows are immutable except for expiry-related state.
- Snapshot data must not imply current stock, current schedule, or current price after creation.
- `request_id` and `booking_id` are optional context links.

### `search_result_snapshot`

Owned by `kz.ask.search`.

Fields:

- `id`
- `search_snapshot_id`
- `result_type`
- `result_rank`
- `product_offer_id`
- `service_branch_offer_id`
- `business_id`
- `branch_id`
- `title`
- `summary`
- `price`
- `status_label_key`
- `availability_confidence`
- `source_type`
- `distance_meters`
- `created_at`
- `updated_at`

Rules:

- Store enough display data to reopen the old result list without live re-search.
- Use `resultRank` in Java and `result_rank` in the database.
- Keep foreign keys optional because old offers or businesses may later become inactive.
- Do not add product availability history snapshots; this is a user search result snapshot, not stock history.

## Endpoint Foundation

Create endpoints only after the entities exist:

- `POST /api/v1/client/search-sessions`
- `GET /api/v1/client/search-sessions/current`
- `POST /api/v1/client/search-sessions/{searchSessionId}/snapshot`
- `GET /api/v1/client/search-snapshots`
- `GET /api/v1/client/search-snapshots/{searchSnapshotId}`

## Request Rules

`CreateSearchSessionRequest`:

- `rawQuery`
- `scope`
- `cityId`
- `categoryId`
- `filters`

Rules:

- `rawQuery` is required for typed search.
- `scope` must not erase or rewrite `rawQuery`.
- `filters` are client intent, not database truth.

## Response Rules

`SearchSessionResponse`:

- `id`
- `rawQuery`
- `scope`
- `status`
- `city`
- `category`
- `startedAt`
- `lastActiveAt`

`SearchSnapshotResponse`:

- `id`
- `rawQuery`
- `scope`
- `status`
- `counts`
- `products`
- `services`
- `businesses`
- `linkedRequest`
- `linkedBooking`
- `expiresAt`

## Clarifying Logic

- If live search confidence is high, return live results and keep fallback request optional.
- If live search confidence is low, stale, missing, or confirmation-needed, expose fallback request creation.
- If an old snapshot is opened, return snapshot labels and saved display data, not live availability claims.
- If a linked live offer is inactive, mark it as unavailable in the response while preserving the historical snapshot row.

## Implementation Boundaries

- Use `kz.ask.search` feature-first packages.
- Use Controller -> Processor -> DomainService -> Repository.
- Do not expose JPA entities.
- Do not create tests unless the project rule is explicitly reversed.
- Do not run Maven unless explicitly requested.
- Do not add product variant tables.
- Do not add specialist accounts.
