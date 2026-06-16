# MCP Guidance

MCP tools are useful when they improve correctness, external-state inspection, browser verification, current documentation lookup, or durable lifecycle records.

## Dashboard Or Lifecycle MCP

Use for:

- substantial task start checkpoints;
- major decisions;
- meaningful pivots;
- completion records;
- workflow concepts when useful.

Do not use it for every prompt or every file edit.

## Documentation Lookup

Use current documentation tools for libraries, frameworks, SDKs, CLIs, and cloud services where behavior may have changed.

## Browser Or Playwright

Use for visible frontend behavior, local app flows, mobile viewport checks, screenshots, and regression verification.

## Render, Supabase, GitHub, And Provider Tools

Use these only when authenticated and in scope. Spring Boot should remain the backend API boundary; frontend/mobile clients should not bypass backend business logic by talking directly to the database.

## Do Not Copy

Do not copy local MCP configs, tokens, auth files, sqlite state, generated caches, sandbox state, runtime binaries, or machine-specific paths into the repo.

Document portable intent and let each developer configure their own tools.
