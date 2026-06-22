# Foundation Changelog

## 2026-06-22 - Role Simplification: Owner/Staff Only

Removed MANAGER and OPERATOR roles from all backend contracts and task files. Business roles are now only OWNER and STAFF.

- `UX_UI_BACKEND_CONTRACT.md`: Entry point model updated (Staff replaces Manager/Operator). Added explicit prohibition paragraph.
- `AUTH_BACKEND_CONTRACT.md`: Authority table updated to ROLE_BUSINESS_OWNER and ROLE_BUSINESS_STAFF only. startRoute updated to OWNER_BRANCHES/BRANCH_WORKSPACE.
- `FRONTEND_API_TASKS.md`: All MANAGER/OPERATOR authority references replaced with STAFF. AuthBusinessContextResponse memberRole now OWNER or STAFF. Start routes updated.
- `PRODUCT_SERVICE_FOUNDATION_ERD.md`: branch_member role column narrowed to STAFF only. branch_invite role always STAFF.
- `03_business_admin_product_endpoints.md`: Access rules updated to Owner+Staff, no Manager/Operator split.
- `04_business_admin_service_endpoints.md`: Access rules updated to Owner+Staff, no Manager/Operator split.
- Business authorities: only ROLE_BUSINESS_OWNER and ROLE_BUSINESS_STAFF. ROLE_BUSINESS_MANAGER and ROLE_BUSINESS_OPERATOR removed.

## 2026-06-22 - Automatic Supplier Check Contract

Updated backend contracts and search task direction:

- Submitted search sessions now have locked scope: `PRODUCT` or `SERVICE`.
- Product search can automatically create supplier check/request linked to `searchSessionId`.
- Auto supplier check selects business/branch targets using city, category, tags, branch profile, and similar product evidence.
- Auto supplier check is not standalone business search.
- Auto supplier check is business-facing as Activity/request item and customer-facing as `Подходящие магазины`.
- Sending auto supplier check must not create a customer-visible outgoing chat message or customer unread notification.
- Customer `Чаты` tab appears only after real chat interaction.

## 2026-06-21 - Staff Management Implementation

Added branch-level staff management with temporary password activation, invite codes, and granular authority.

### Membership Model

- `business_member` now represents business-level OWNER role only.
- New `branch_member` table for branch-level MANAGER and OPERATOR roles.
- New `branch_invite` table for invite code-based staff onboarding.
- Authority stored as `String authority` in `auth_session` (replaces `AppRole role` enum): ROLE_BUSINESS_OWNER, ROLE_BUSINESS_MANAGER, ROLE_BUSINESS_OPERATOR, ROLE_CUSTOMER.

### Staff Activation Flow

- Owner creates staff via `POST /staff` — system generates temporary BCrypt-hashed password.
- Temp password plain text is AES-encrypted for owner visibility until activation.
- Staff logs in via unified `POST /auth/login` — system returns `activationRequired: true`.
- Staff changes password via `POST /auth/change-temporary-password` — account becomes ACTIVE.
- Staff statuses: PENDING_ACTIVATION, ACTIVE, PASSWORD_RESET_REQUIRED, DISABLED.

### Schema Changes

- `auth_session`: added `authority VARCHAR(50)`, `activation_required BOOLEAN DEFAULT FALSE`; dropped `role`.
- `app_user`: added `must_change_password`, `temp_password_encrypted`, `activated_at`.
- Created `branch_member` table with UNIQUE(branch_id, user_id).
- Created `branch_invite` table with UNIQUE code and expiry fields.
- Indexes on branch_member branch/user, branch_invite branch/code, app_user role.

### Service Naming Convention

- `IdentityDomainService` → `IdentityService` (interface) + `IdentityServiceImpl`.
- `BusinessDomainService` → `BusinessService` (interface) + `BusinessServiceImpl`.

### Endpoints Added

- `POST /api/v1/auth/login` — unified password login for all roles.
- `POST /api/v1/auth/change-temporary-password` — first-login password change.
- `POST /api/v1/businesses/{bId}/branches/{brId}/staff` — create staff.
- `GET /api/v1/businesses/{bId}/branches/{brId}/staff` — list staff.
- `POST /api/v1/businesses/{bId}/branches/{brId}/staff/{id}/update` — update staff (role, disable).
- `POST /api/v1/businesses/{bId}/branches/{brId}/staff/{id}/reset-password` — reset staff password.
- `POST /api/v1/businesses/{bId}/branches/{brId}/invites` — create invite.
- `GET /api/v1/businesses/{bId}/branches/{brId}/invites` — list invites.
- `DELETE /api/v1/businesses/{bId}/branches/{brId}/invites/{id}` — revoke invite.

## 2026-06-21 - Data Model Normalization After Task 05

Synchronized the implemented entities, `V1__init.sql`, and first-read documentation with the current frontend-backed UX direction.

### Removed Old Model Fields

- Removed product stock tracking from MVP schema and entities: no `stockStatus`, `stockQuantity`, `stock_status`, or `stock_quantity`.
- Removed availability scoring from MVP schema and entities: no `AvailabilityConfidence`, `availabilityConfidence`, or `availability_confidence`.
- Removed freshness tracking from MVP schema and entities: no `freshnessAt` or `freshness_at`.
- Removed standalone business search indexing fields from search documents and counters from snapshots.
- Removed service `ConfirmationPolicy`; MVP services are request-to-book and business-confirmed, not automatic booking truth.

### Current Implemented Model

- `ProductOffer.enabled` controls whether a branch-level product offer appears in live product search.
- `ServiceBranchOffer.active` controls whether a branch-level service offer appears in live service search.
- `ServiceBranchOffer.scheduleText` stores display schedule/conditions text only.
- `SearchDocument` indexes product offers and service branch offers only.
- `SearchResultSnapshot.business` and `SearchResultSnapshot.branch` are context for product/service rows, not standalone business results.
- `BusinessContact` now uses `contactValue` and `primaryContact`, mapped to `contact_value` and `is_primary`.
- `SupplierResponseStatus` now uses product/service-specific values: `HAS_ITEM`, `NO_ITEM`, `NEED_CLARIFICATION`, `HAS_ANALOG`, `CAN_PROVIDE`, `CANNOT_PROVIDE`, `SUGGEST_OTHER_TIME`.

### Documentation Updated

- Updated `README.md` to remove old "business search and availability layer" wording and replace it with product/service search wording.
- Updated `ARCHITECTURE_NARRATIVE.md` with the current data model alignment and explicit removed-field list.
- Updated `PRODUCT_SERVICE_FOUNDATION_ERD.md` with `enabled`, `active`, `schedule_text`, `contact_value`, `is_primary`, and current search document shape.
- Updated `UX_UI_BACKEND_CONTRACT.md` to make the frontend flow the source to refresh from before backend task/entity changes.
- Updated `AUTH_BACKEND_CONTRACT.md` to state that email is the real MVP verification channel and SMS is disabled until a real provider is connected.

## 2026-06-21 - Identity Auth Implementation (Task 05)

Implemented identity authentication and business branch onboarding endpoints for Ask: customer login/register, business login/register, code verification, current session, and logout.

### Auth Model
- Email-or-phone login/registration: exactly one primary identifier is used for the auth challenge.
- Verification codes are 6 digits and stored hashed in the database.
- Passwords are stored through Spring Security `PasswordEncoder` with BCrypt.
- Bearer session tokens are random opaque tokens; only token hashes are stored.
- Remember-me extends session TTL through backend config.
- Business registration data is persisted only after successful contact verification.

### Business Onboarding
- Business registration creates `Business`, `BusinessBranch`, `BusinessMember`, and initial `BusinessContact` records.
- Registration contact becomes the first public branch contact.
- Online-only branches may omit physical address fields.

### Email and SMS
- Email verification is the real MVP channel and uses `SmtpEmailCodeSender` with JavaMailSender.
- Email delivery failure stops the auth challenge instead of pretending success.
- SMS interfaces exist for future provider integration, but SMS is disabled by default and current logging/noop implementations return delivery-unavailable errors.

### Security And Configuration
- `SecurityConfig` uses stateless sessions and permits only auth start/register/verify endpoints without authentication.
- `JwtAuthFilter` validates Bearer tokens through `IdentityDomainService.findSessionByToken`.
- Auth settings live under `auth.*`; SMTP settings use `spring.mail.*` and environment overrides.

### Schema
- `V1__init.sql` creates the initial schema for identity, business, catalog, service, request, messaging, booking, and search domains.
- The schema is production-facing for persistent business onboarding data, not throwaway mock data.

## 2026-06-21 - Auth, UX Contract, And Backend Task Actualization

Added `AI_Knowledge/client_contracts/AUTH_BACKEND_CONTRACT.md` as the backend-facing source for customer and business auth: email-or-phone login and registration, real email verification, optional SMS verification, 6-digit confirmation, remember-me sessions, current session restore, logout, and branch/store onboarding.

Synchronized `UX_UI_BACKEND_CONTRACT.md`, architecture narrative, ERD, and Tasks 00-05 with the current frontend expected UX: product/service search only, no standalone business search, no `availabilityConfidence`, no `stockQuantity`, no `freshnessAt`, no `chatAvailable`, distance calculated only from customer coordinates to branch coordinates, and production-facing business onboarding with persistent branch, product, and service data.

## 2026-06-19 - Search Session Snapshot Foundation

Added Task 00 implementation for search history foundation: `SearchSession`, `SearchSnapshot`, `SearchResultSnapshot`, and related search enums. Updated the product/service ERD so customer history reopens fixed saved result state instead of pretending live search documents still represent the original result set.

## 2026-06-19 - Documentation Structure Cleanup

Moved first-session guidance into `AI_Knowledge/first_steps`, moved large product/service architecture into `AI_Knowledge/data_architecture`, and added `AI_Knowledge/client_contracts/UX_UI_BACKEND_CONTRACT.md` as the backend-facing extraction from the UX/UI flow.

Separated agent workflow from backend code rules: `AGENTS.md` is now the short agent entrypoint and task router, while `AI_Knowledge/CODE_RULES.md` remains the code architecture rule file.

Moved Codex plugin and MCP expectations into `codex/CODEX_INFRASTRUCTURE.md` and removed project-local MCP, plugin, old skill, audit, origin, deprecated web-staging, and archive notes that were not useful for new backend programmers.

## 2026-06-18 - Project Verification Scope Correction

Updated project guidance to stop requiring or creating tests for AskBackend foundation work unless the project rule is explicitly reversed. Verification language now points to compile, migration, OpenAPI contract, review, provider-contract, and no-secret checks.

## 2026-06-18 - Product And Service ERD Strategy

Added `PRODUCT_SERVICE_FOUNDATION_ERD.md` with the MVP entity strategy and ERD. The product model now treats each concrete sellable variation as a `product` with tags for search, defers variant/attribute tables, adds `booking` for confirmed service lifecycle, uses universal `conversation` tables for messaging, and keeps `service_resource` as an optional abstract capacity resource rather than a specialist account.

## 2026-06-18 - Initial Backend Architecture And Entities

Initialized the Spring Boot Maven project skeleton, root `AGENTS.md`, `CODE_RULES.md`, feature package folders, UUIDv7 base entity, Lombok-backed JPA entity classes, and enum foundations. Only entities and architecture scaffolding were added; no controllers, processors, services, repositories, DTOs, migrations, or tests were created.

## 2026-06-18 - Search-First Strategy Actualization

Updated the product strategy:

- Ask is now described as local search for products and services across city businesses.
- Customers should see known products/services first when Ask has data.
- If a product or service exists in the database, Ask should show where it is available or which business provides it.
- ProductRequest/request routing remains as fallback for missing product/service results or customer-requested business confirmation.
- Catalog, service data, search indexing, Excel/CSV import, and persisted business-entered data are core product architecture, not distant optional additions.
- Data-truth rules still apply: do not invent stock, slots, logistics, delivery, or guaranteed availability without supplier or integration data.

## 2026-06-17 - Goal Actualization

Updated the product architecture idea:

- AskBackend is one backend for Android, iOS, and any future website.
- AskBackend should not copy frontend FSD literally; it should use package-by-feature, modular monolith/domain modules, and clean/hexagonal boundaries.
- Client implementations should use a shared design-independent API/client abstraction instead of duplicating heavy business logic per platform.
- Product catalogs should support Excel and CSV import workflows so sellers do not recreate existing data manually.
- Service-provider management should lean toward a web cabinet for larger service datasets, schedules, free windows, discounts, conditions, specialists, and branches.
- Mobile application direction is customer side plus seller/supplier side; website direction is primarily service-provider administration.

## 2026-06-17 - Restored Portable Foundation

Restored the foundation document set after an accidental over-revert.

### Created Or Restored

- `README.md`
- `ARCHITECTURE_NARRATIVE.md`
- `FIRST_READ_THIS.md`
- `IMPLEMENTATION_PIPELINE.md`
- `AGENTS.md`
- `SELF_AWARE_ORIGIN.md`
- `FOUNDATION_AUDIT.md`
- `DEPRECATED_WEB_STAGING_NOTES.md`
- `CHANGELOG_FOUNDATION.md`
- `.gitignore`
- `archive/README.md`
- `mcp/README.md`
- `plugins/README.md`
- `skills/README.md`
- `skills/backend-spring-ask.md`
- `skills/frontend-ask.md`
- `skills/architecture-system-analysis.md`
- `skills/catalog-integration.md`
- `skills/services-search.md`
- `skills/ai-workflow-consistency.md`
- `skills/mcp-dashboard-usage.md`

### Preserved

- Product vision.
- Manual request fallback direction.
- Catalog/import/search direction.
- Services/scheduling analysis direction.
- Backend architecture rules.
- AI workflow consistency.
- MCP/dashboard guidance.
- Old browser-prototype lessons as archive-only context.

### Adjusted

- Removed the idea that future users or Codex agents need access to any old local project path.
- Reframed origin as local foundation knowledge for the new project.
- Kept the required docs and skills from the initial prompt.

### Excluded

- Old application business code.
- Local Codex configs and secrets.
- Generated runtime state.
- Machine-specific setup.
- Web-staging-only mechanics as active backend requirements.
