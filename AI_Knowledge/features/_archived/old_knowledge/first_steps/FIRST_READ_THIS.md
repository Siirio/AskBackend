# First Read This

Read this first when opening AskBackend in a fresh Codex session or onboarding a programmer.

## Required Read Order

1. `README.md`
2. `AGENTS.md`
3. `codex/CODEX_INFRASTRUCTURE.md`
4. `AI_Knowledge/CODE_RULES.md`
5. `AI_Knowledge/first_steps/IMPLEMENTATION_PIPELINE.md`
6. `AI_Knowledge/data_architecture/ARCHITECTURE_NARRATIVE.md`
7. `AI_Knowledge/data_architecture/PRODUCT_SERVICE_FOUNDATION_ERD.md`
8. `AI_Knowledge/client_contracts/UX_UI_BACKEND_CONTRACT.md`
9. `AI_Knowledge/client_contracts/AUTH_BACKEND_CONTRACT.md`
10. `AI_Knowledge/CHANGELOG_FOUNDATION.md`

When backend product behavior, DTOs, entities, or task contracts depend on UX, refresh the backend contract from the sibling frontend document `AskFrontend/AI_Knowledge/product_ux/EXPECTED_UX_UI_FLOW.md` before editing backend tasks or schema.

## What Belongs Here

This repository should give a new programmer enough context to work on the backend without importing old project history.

Keep:

- current product direction;
- current backend code rules;
- data architecture;
- client/API contract expectations;
- Codex infrastructure requirements.

Do not keep:

- old prototype migration notes;
- deprecated browser-staging behavior;
- local MCP folders or local plugin state;
- local Codex configs, tokens, auth files, sqlite state, generated caches, runtime binaries, or machine-specific paths.

## First Codex Setup

Codex infrastructure is not configured from project-local MCP folders. The project lists the expected plugins, MCP servers, and skills in `codex/CODEX_INFRASTRUCTURE.md`; Codex-level tooling installs and routes them.

The project-level rule is simple: use the smallest relevant tool set, and do not copy local tool state into the repository.

Expected MCP servers:

- `context7`
- `playwright`
- `node_repl`
- `render`
- `openai_api_key_local_confirmation`

Expected plugin families:

- OpenAI Developers
- Build Web Apps
- Vercel
- Render
- Supabase
- GitHub
- CircleCI
- CodeRabbit
- Figma
- HyperFrames
- Remotion
- Documents
- Spreadsheets
- Presentations
- PDF
- Browser
- Chrome
- Computer Use

## If The Chat Is Interrupted

Resume by reading:

1. this file;
2. `AGENTS.md`;
3. `AI_Knowledge/first_steps/IMPLEMENTATION_PIPELINE.md`;
4. `AI_Knowledge/CHANGELOG_FOUNDATION.md`;
5. current git status or file diff.

Continue from the last completed stage. Do not restart by overwriting files.
