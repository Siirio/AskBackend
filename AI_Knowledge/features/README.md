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

## Current feature index

| Knowledge folder | Java package(s) |
| --- | --- |
| business | `business` including branch, category, invitation, member, onboarding, profile, verification |
| identity | `identity` |
| item | `offer.item`, `offer.media`, `offer.purchase` |
| legal | `legal` |
| messaging | `chat` |
| offers | `business.uniqueoffer` |
| platform | `platform`, `moderation`, `audit`, `ai` |
| search | `search`, `catalog` |
| service | `offer.service`, `offer.media`, `offer.purchase` |

`importing` and `managedimport` are current packages whose contracts are recorded with the consuming item/service/business flows. Removed features live only under `_archived`; `request` was archived on 2026-08-03 because its Java package and endpoints no longer exist.
