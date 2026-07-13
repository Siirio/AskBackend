# system-maintainer

Maintain the AI_Knowledge system. Prevent knowledge rot.

## Operations
- Compress: over max-lines → remove oldest, merge duplicates, tighten prose. Skip Locks.md.
- Migrate: misplaced content → correct level. Pattern in 3+ features → promote.
- Archive: deleted features → _archived/ (NEVER delete). Add Changelog entry.
- Stale: code changed, doc didn't → flag. Don't auto-delete.
- Deduplicate: same lock twice → keep most specific.

Report: "System maintainer: compressed {N}, migrated {N}, archived {N}, flagged {N} stale, deduplicated {N}."
