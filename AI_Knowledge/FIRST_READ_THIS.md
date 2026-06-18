# First Read This

This is the first document to read when opening Ask with Codex or onboarding a developer.

## Read Order

1. `README.md`
2. `ARCHITECTURE_NARRATIVE.md`
3. `AGENTS.md`
4. `CODEX_PLAYBOOK.md`
5. `CODE_RULES.md`
6. `IMPLEMENTATION_PIPELINE.md`
7. `PRODUCT_SERVICE_FOUNDATION_ERD.md`
8. `SELF_AWARE_ORIGIN.md`
9. `skills/README.md`
10. `mcp/README.md`
11. `plugins/README.md`
12. `DEPRECATED_WEB_STAGING_NOTES.md` only when deciding whether old browser-prototype logic should be kept
13. `CHANGELOG_FOUNDATION.md`

## Immediate Rules

- Do not edit application business code before reading the relevant project rules.
- Do not overwrite existing `AGENTS.md`, skill docs, MCP config, plugin config, or workflow files without reading them.
- Do not copy local Codex configs, auth files, tokens, sqlite state, generated caches, plugin caches, runtime paths, or machine-specific setup into the repo.
- Do not make old browser-staging behavior a backend requirement.
- Preserve the product direction: local search first, known products/services before request creation, request fallback when exact data is missing, and catalog/service depth over time.
- Use system analysis before coding catalog, services, schedules, integrations, data ownership, scaling, or API contract changes.

## If The Chat Is Interrupted

Resume by reading:

1. this file;
2. `IMPLEMENTATION_PIPELINE.md`;
3. `CHANGELOG_FOUNDATION.md`;
4. current git status or file diff;
5. the specific docs for the task route in `CODEX_PLAYBOOK.md`.

Continue from the last completed stage. Do not restart by overwriting files.

## What A Good Codex Agent Should Do

- Load the foundation before coding.
- Search existing files and patterns.
- Keep the task scoped.
- Separate product decisions from temporary prototype details.
- Warn when a request conflicts with vision or architecture.
- Offer compatible alternatives.
- Verify work with the right level of compile, migration, contract, review, or no-secret checks.
