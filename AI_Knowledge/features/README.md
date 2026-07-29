# ASK Backend — Features

Each feature gets a folder with 4 files:

```
features/{name}/
├── README.md        # Why it exists, key decisions
├── contracts.md     # REST API contracts (endpoints, DTOs, errors)
├── ux-ui-flow.md    # How frontend uses this feature
└── locks.md         # Feature invariants
```

## Add a feature
1. Create folder with all 4 files
2. Fill README.md first (purpose, decisions)
3. Fill contracts.md (endpoints, request/response shapes)
4. Fill ux-ui-flow.md (frontend expectations)
5. Add locks to locks.md
6. Update Feature Index in CLAUDE.md and AGENTS.md

## Remove a feature
1. Move to `features/_archived/{name}/`
2. Add Changelog entry
3. Update Feature Index

## Tracked features
Run a codebase scan to discover existing features from `src/main/java/kz/ask/` packages.
