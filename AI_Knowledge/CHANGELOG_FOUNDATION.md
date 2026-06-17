# Foundation Changelog

## 2026-06-17 - Goal Actualization

Updated the product architecture idea:

- AskBackend is one backend for Android, iOS, and any future website.
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
- Manual MVP direction.
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
