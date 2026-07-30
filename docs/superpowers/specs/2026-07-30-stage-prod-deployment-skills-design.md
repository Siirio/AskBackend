# Stage and Production Deployment Skills Design

## Goal

Provide two explicit Codex skills for releasing ASK to staging and production, then use them to promote the current backend and frontend changes through `dev` and `master`.

## Deployment contract

Staging is sourced only from `dev` in both repositories. The backend is built locally, uploaded to the ASK VPS, and deployed as `app-stage`. Vercel deploys the frontend automatically from the frontend `dev` branch and must be checked after the push.

Production is sourced only from `master` in both repositories. It is allowed only after staging passes its health, OAuth redirect, verification-mode, frontend, and runtime-SHA checks. The backend is deployed as `app-prod`. Vercel deploys the production frontend automatically from the frontend `master` branch and must be checked after the push.

## Skill structure

The repository owns two project skills:

- `.agents/skills/deploy-stage`
- `.agents/skills/deploy-prod`

Each skill contains the ASK VPS host, login, and password requested by the repository owner. Each skill also contains its complete branch policy, build and upload procedure, server-side environment checks, deployment command, verification gates, and stop conditions.

The legacy `.claude/skills/deploy-staging.md` is removed so agents cannot select an obsolete procedure that permits direct Flyway history edits.

## Server environment

`/opt/ask/AskBackend/deploy/vps/.env` remains server-local and untracked. It contains namespaced stage and production values used by the shared Compose stack. Before any mutation, the deployer creates a timestamped mode-`600` backup.

The deployer never prints secret values. It validates required key names, non-empty values, and minimum secret lengths with a remote script whose output contains only key status.

Staging runs with:

```dotenv
AUTH_VERIFICATION_TEST_MODE=true
AUTH_VERIFICATION_STAGING_BYPASS=true
```

Production runs with:

```dotenv
AUTH_VERIFICATION_TEST_MODE=false
AUTH_VERIFICATION_STAGING_BYPASS=false
```

The Compose definition sets these values independently on `app-stage` and `app-prod`. Staging therefore accepts any syntactically valid six-digit verification code without using the real delivery flow. Production verifies the generated code and uses the configured real mail sender.

## Safety gates

- Never commit any `.env` file.
- Never deploy a dirty or unpushed source tree.
- Never deploy `dev` to production or `master` to staging.
- Never merge to `master` until staging is verified.
- Never mutate production PostgreSQL data, schema, or `flyway_schema_history`.
- Never repair a migration checksum during deployment.
- Never use `docker system prune`, volume deletion, database recreation, or destructive cleanup as an automatic recovery step.
- Keep a rollback artifact and the previous server environment backup.
- Stop if the public runtime SHA differs from the intended commit.

## Verification

Every deployment must verify:

- Compose configuration is valid.
- The target container is healthy and running.
- `/actuator/health` reports `UP`.
- `/actuator/info` exposes the expected backend commit SHA.
- Google OAuth returns a redirect using the correct HTTPS callback.
- Runtime verification flags match the target environment.
- The corresponding Vercel frontend returns HTTP 200.
- The Vercel deployment is associated with the intended frontend commit.
- Recent target-container logs contain no startup failure.
