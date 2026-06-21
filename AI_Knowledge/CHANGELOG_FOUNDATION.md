# Foundation Changelog

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
