# Foundation Audit

This audit explains what belongs in the Ask foundation and what should be excluded.

## Keep In Foundation

- Product vision and MVP boundaries.
- Backend architecture rules.
- Data truth rules.
- Catalog/import/search strategy.
- Services/scheduling analysis rules.
- AI-agent behavior rules.
- Implementation pipeline.
- MCP/plugin usage guidance.
- Changelog of foundation changes.

## Keep Only As Archive Context

- Browser prototype lessons.
- Old web-staging validation flow.
- Historical UI experiment constraints.
- Previous current-behavior snapshots.
- Deployment notes that are useful historically but not mandatory architecture.

## Exclude

- Application business code dumps.
- Local Codex configs.
- Tokens, auth files, API keys, bearer env vars, sqlite state, generated caches, sandbox state, runtime binaries, and plugin caches.
- Machine-specific absolute paths as required setup.
- Old screenshots or generated assets unless intentionally archived.
- Browser-only mechanics as backend rules.

## Transfer Principle

Transfer meaning, not noise.

If an old rule preserves product direction, architecture quality, data truth, or team workflow, rewrite it as a clean project-local rule.

If an old rule only made sense for a prototype, local machine, temporary deployment, or one developer's setup, archive or exclude it.
