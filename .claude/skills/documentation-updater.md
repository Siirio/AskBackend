# documentation-updater

Sync AI_Knowledge with code changes. Docs are part of the change.

## Execution
1. Review diff → map changes to doc impact:
   - API/DTO → contracts.md
   - Behavior → README.md
   - UX impact → ux-ui-flow.md
   - New invariant → locks.md
   - Decision → Changelog.md
2. Read current doc, merge changes, compress if over max-lines
3. Cross-project: if ../ask-frontend/ has matching feature → update its contracts.md too
4. NEVER delete entries unless feature is removed. NEVER modify locks.
