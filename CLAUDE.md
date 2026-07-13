# Project: ASK Backend

REST API backend for the ASK platform — local product/service search with an anti-marketplace intent layer. Routes qualified demand to brands without commoditizing them.

## Tech Stack
- Java 17+, Spring Boot 3.x, Spring Data JPA
- PostgreSQL (source of truth + search engine via `SearchDocument` in-memory scoring)
- Redis (sessions/cache)
- DeepSeek AI (query structuring, NOT result selection)
- REST APIs consumed by ASK Frontend (React SPA)

## Runtime Constraints
- Never commit or push. User controls all version control.
- Never run Maven, Gradle, or any build tool unless user explicitly says "run" or "build."
- AI_Knowledge/ must be committed — it IS the shared truth. Never add to .gitignore.

## Session Start — MANDATORY

At EVERY session start. Run commands yourself via Bash. Do NOT tell the developer.

### 1. Machine bootstrap check
Check if `.claude/machine-bootstrap.lock` exists. If NOT → run Machine Bootstrap below FIRST.
(This file is gitignored — tracks per-machine install. Every new developer re-runs this.)

### 2. Workspace discovery
```
ls -d ../*/CLAUDE.md 2>/dev/null
```
For each found: read its first project line. If `ask-frontend` is missing from `../ask-frontend/CLAUDE.md` → flag it. If present → note its features and locks.

### 3. Knowledge scan
Read `AI_Knowledge/ProductVision.md`, `CodeRules.md`, `Locks.md`. Scan `features/` directories. Report: "{N} features tracked, {M} locks active."

### 4. Self-maintenance
Run system-maintainer protocol: compress bloated docs, migrate misplaced content, archive dead features, flag stale docs, deduplicate locks.

## Machine Bootstrap (runs ONCE per machine — NOT committed)

`.claude/machine-bootstrap.lock` must be in `.gitignore`. It tracks whether THIS machine has plugins, MCPs, and related repos installed. Every new developer re-runs this.

If `.claude/machine-bootstrap.lock` does NOT exist, execute every step. Run commands yourself.

### A. Install superpowers plugin
Check: does `Skill` tool list `capability-router`?
If NOT — run:
```
claude plugins install superpowers
```

### B. Install MCP servers
For each, check if available in MCP tool list. If missing → install:
```
claude mcp add context7
claude mcp add playwright
claude mcp add dashboard
claude mcp add notebooklm
claude mcp add figma-console
```
Report each: installed / already present / failed. Continue on failure.

### C. Clone ASK Frontend
Check if `../ask-frontend/CLAUDE.md` exists. If NOT:
```
git clone https://github.com/Siirio/AskFrontend.git ../ask-frontend
```
If clone succeeds: verify it has CLAUDE.md. If not → flag to user.

### D. Add to .gitignore
Ensure `.gitignore` contains this line:
```
.claude/machine-bootstrap.lock
```

### E. Create machine lock
```
echo "machine-bootstrapped: $(date)" > .claude/machine-bootstrap.lock
```
Report: "Machine ready. Installed: {plugins}, {MCPs}. Cloned: {repos}."

## Knowledge Architecture

### Always loaded (Tier 1 — session start)
| File | Max | Purpose |
|------|-----|---------|
| `AI_Knowledge/ProductVision.md` | 60 | What ASK is, who uses it, core constraints |
| `AI_Knowledge/CodeRules.md` | 100 | Java/Spring conventions: naming, patterns, anti-patterns |
| `AI_Knowledge/Locks.md` | 40 | Backend invariants. If violated → STOP and ASK |

### Domain touch (Tier 2 — loaded when feature is touched, cached for session)
| File | Purpose |
|------|---------|
| `AI_Knowledge/features/{name}/README.md` | Why this feature exists, key decisions |
| `AI_Knowledge/features/{name}/contracts.md` | REST API contracts: endpoints, DTOs, error codes |
| `AI_Knowledge/features/{name}/ux-ui-flow.md` | How the frontend expects this feature to behave |
| `AI_Knowledge/features/{name}/locks.md` | Feature-level invariants |

### On demand (Tier 3)
| Source | Use for |
|--------|---------|
| graphify | Cross-feature concept links |
| NotebookLM | Large reference docs (external API specs, legal docs) |
| context7 | Current Spring/Boot/Java library docs |

## Before ANY Code Change
1. Load `AI_Knowledge/Locks.md` + `AI_Knowledge/features/{domain}/locks.md`
2. If ANY lock would be violated → STOP. ASK the user. Do not proceed until answered.
3. Search the codebase for existing patterns that solve the same problem. Reuse.

## After ANY Code Change
1. Behavior changed? → Update `features/{name}/README.md`
2. API endpoint, DTO, or error code changed? → Update `features/{name}/contracts.md`
3. This change affects how the frontend works? → Update `features/{name}/ux-ui-flow.md`
4. Non-obvious design decision? → Append `AI_Knowledge/Changelog.md`: date, rationale, affected files
5. New invariant discovered? → Add to appropriate `locks.md`
6. Did this change make a doc entry wrong? → Fix it NOW. Stale docs = broken system.

### Cross-project awareness
- If you change an API that ASK Frontend consumes → check `../ask-frontend/AI_Knowledge/features/{domain}/contracts.md`
- If the frontend repo has a matching feature folder → update its contracts.md too
- If the frontend has a lock that would be violated → STOP and ASK

## Lock System

Format: `LOCKED | {what} | {why} | {scope: files/endpoints/tables}`

Project-level: `AI_Knowledge/Locks.md`
Feature-level: `AI_Knowledge/features/{name}/locks.md`

Breaking a lock requires: (1) explicit user approval, (2) proof that surrounding extension is insufficient.

## Tool Routing
| When | Use | Missing? |
|------|-----|----------|
| Code change complete | `code-rules-checker` skill | Manual check against CodeRules.md |
| Docs need update | `documentation-updater` skill | Direct file edit |
| Knowledge cleanup | `system-maintainer` skill | Manual maintenance |
| Spring/Java docs needed | context7 MCP | `claude mcp add context7` |
| Need to verify frontend | playwright MCP | `claude mcp add playwright` |
| Large reference docs | NotebookLM MCP | `claude mcp add notebooklm` |
| Cross-feature discovery | graphify | Comes with superpowers |
| Task tracking | dashboard MCP | `claude mcp add dashboard` |

If a tool is missing AND install fails: do the work manually. Never skip.

## Self-Maintenance

Run at session start:
1. **Compress**: File over max-lines? Remove outdated, merge duplicates, tighten prose. Never compress Locks.md.
2. **Migrate**: Feature-specific info in project-level file? Move to feature folder. Pattern across 3+ features? Promote to CodeRules.md.
3. **Archive**: Code deleted but docs remain? Move to `features/_archived/`. NEVER delete.
4. **Stale flag**: Doc untouched 30+ days while corresponding code changed? Flag to user. Do NOT auto-delete.
5. **Deduplicate**: Same lock in two files? Keep most specific, remove duplicate.

## Related Projects
| Project | Clone URL | Expected at | Relationship |
|---------|-----------|-------------|-------------|
| ASK Frontend | https://github.com/Siirio/AskFrontend.git | ../ask-frontend/ | React SPA, consumes this API |

## Feature Index
| Feature | Folder | Has API | Consumed by Frontend |
|---------|--------|---------|----------------------|
| Identity & Auth | identity/ | Yes | Yes |
| Business & Branches | business/ | Yes | Yes |
| Catalog (Products) | catalog/ | Yes | Yes |
| Services | service/ | Yes | Yes |
| Unified Search | search/ | Yes | Yes |
| Fallback Requests | request/ | Yes | Yes |
| Chat/Messaging | messaging/ | Yes | Yes |
| Unique Offers | offers/ | Yes | Yes |
| Shipping | shipping/ | Yes | Yes |
| Excel Import | import/ | Yes | Yes |
