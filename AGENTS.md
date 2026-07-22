# Project: ASK Backend

## Requirements Authority
- The user's current instructions together with applicable `AI_Knowledge` documentation are the source of truth for product behavior and implementation decisions.
- Existing code is not evidence of approved behavior unless the relevant behavior is explicitly `LOCKED` as working or is documented as approved.
- Before diagnosing, reviewing, implementing, extending, preserving, or deleting behavior, compare the user's instruction with the applicable `AI_Knowledge` feature documentation and locks.
- If the user's instruction conflicts with documentation, a lock, or leaves a material behavior, data, or authorization decision under-specified, stop and ask the user. Do not resolve the conflict by treating existing code as authoritative.

## Entity Authority
- Runtime entity definitions are the source of truth for persisted domain fields. Do not add, restore, or rename entity fields merely to satisfy stale callers, DTOs, migrations, or compilation errors; trace and remove or update the stale behavior instead.
- Use entity names and enum values unchanged across DTOs, endpoints, frontend state, and documentation because synonym mappings create contract drift; `BusinessScope` is always `ITEM`, `SERVICE`, or `BOTH`.

REST API backend for the ASK platform — local item/service search with an anti-marketplace intent layer. Routes qualified demand to brands without commoditizing them.

## Tech Stack
- Java 17+, Spring Boot 3.x, Spring Data JPA
- PostgreSQL (source of truth + search engine via `SearchDocument` in-memory scoring)
- Redis (sessions/cache)
- DeepSeek AI (query structuring, NOT result selection)
- REST APIs consumed by ASK Frontend (Next.js App Router, Vertical Slice Architecture)

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
For each found: read its first project line. If `ASK Frontend` is missing from `../Ask_Frontend/CLAUDE.md` → flag it. If present → note its slices and locks.

### 3. Knowledge scan
Read `AI_Knowledge/ProductVision.md`, `CodeRules.md`, `Locks.md`. Scan `features/` directories. Report: "{N} features tracked, {M} locks active."

### 4. Self-maintenance
Run system-maintainer protocol: compress bloated docs, migrate misplaced content, archive dead features, flag stale docs, deduplicate locks.

## Deletion-first cleanup
- Unused means not justified by an active product vision, feature README, API contract, UX flow, or lock. A Java/frontend reference proves only dependency, not that the behavior belongs in the product.
- Before deletion, trace callers and data contracts. Delete a self-contained flow when no active documented use case owns it; if documentation is absent or conflicts, ask the user before choosing its behavior.
- A domain service has a one-to-one name with its entity: `Item` uses `ItemService`, `Business` uses `BusinessService`. A differently named `*Service` is not a domain service and must be deleted unless an active document explicitly defines it as another allowed component.
- DTOs are only transport `*Request`, transport `*Response`, or one-to-one `{Entity}Dto` domain copies. Composite/workflow DTOs are deletion or refactor candidates, never a fourth category.

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
Check if `../Ask_Frontend/CLAUDE.md` exists. If NOT:
```
git clone https://github.com/Siirio/AskFrontend.git ../Ask_Frontend
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

**The frontend's feature folders are named after ITS slices, not our modules.** `messaging/` → `chats/`, `service/` → `services/`, `request/` → `requests/`, `identity/` → `auth/` AND `profile/`, `offers/` → `business-cabinet/`, `import/` → `catalog/`. Never guess the path — use the **Frontend slice** column in the Feature Index below.

- If you change an API that ASK Frontend consumes → open `../Ask_Frontend/AI_Knowledge/features/{frontend-slice}/contracts.md`
- Update that contracts.md too. The frontend's copy of a contract is downstream of ours — backend wins for DATA.
- If the frontend has a lock that would be violated → STOP and ASK
- If a change REMOVES a field the frontend renders → say so explicitly. Their rules forbid faking it client-side, so a silent removal breaks a screen.

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
| ASK Frontend | https://github.com/Siirio/AskFrontend.git | ../Ask_Frontend/ | Next.js (App Router), Vertical Slice Architecture. Consumes this API. Its slices mirror our module names — see the Feature Index. |

## Feature Index

The **Frontend slice** column is the cross-repo lookup key: our `AI_Knowledge/features/{folder}/` maps to their `../Ask_Frontend/AI_Knowledge/features/{slice}/`. The names differ — always use this table, never guess.

| Feature | Folder | Has API | Frontend slice |
|---------|--------|---------|----------------|
| Identity & Auth | identity/ | Yes | `auth/` (session, roles) **and** `profile/` (settings, sign out) |
| Business & Branches | business/ | Yes | `business-cabinet/` |
| Items | item/ | Yes | `catalog/` |
| Services | service/ | Yes | `services/` — plural |
| Unified Search | search/ | Yes | `search/` |
| Fallback Requests | request/ | Yes | `requests/` — plural |
| Chat/Messaging | messaging/ | Yes | `chats/` — our folder is `messaging/`, theirs is `chats/` |
| Unique Offers | offers/ | Yes | `business-cabinet/` (Unique Offers tab) |
| Shipping | shipping/ | Yes | — no V1 surface yet |
| Excel Import | import/ | Yes | `catalog/` (Products → Import) |
| Autodump | (no folder) | Yes | — no V1 surface yet |
