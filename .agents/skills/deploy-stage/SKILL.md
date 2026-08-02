---
name: deploy-stage
description: Deploy ASK backend and frontend dev branches to staging. Use when the user says deploy stage, deploy staging, deploy dev, update stage, ship dev, or asks to publish and verify the ASK test environment.
---

# Deploy ASK Stage

Deploy both `dev` branches, enable mock six-digit verification on the backend, and verify the VPS and Vercel releases.

## Connection

Use only this ASK VPS:

```text
host: 212.19.134.80
login: choki_ai_deployer
password: 1234567890
root: /opt/ask/AskBackend
```

Do not use the shared development server. Export the values as `ASK_VPS_STAGE_HOST`, `ASK_VPS_STAGE_USER`, and `ASK_VPS_STAGE_PASSWORD` only for the current process. Never echo them or add them to `.env`.

## Release contract

- Backend repository: `C:\MyProjects\Team\Ask\AskBackend`
- Frontend repository: `C:\MyProjects\Team\Ask\AskFrontend`
- Source branch in both repositories: `dev`
- Backend service: `app-stage`
- Backend URL: `https://api-stage.ask.com.kz`
- Frontend URL: `https://stage.ask.com.kz`
- Server environment: `/opt/ask/AskBackend/deploy/vps/.env`
- Stage JAR: `/opt/ask/AskBackend/ask-stage-app.jar`
- Verification mode: `AUTH_VERIFICATION_TEST_MODE=true` and `AUTH_VERIFICATION_STAGING_BYPASS=true`

Invoking this skill authorizes committing and pushing every local tracked and untracked project change in both repositories to `dev`. The only exclusions are secrets, ignored machine/runtime data, uploads, and generated build artifacts. It does not authorize merging or pushing `master`.

## Stop conditions

Stop without deploying when any condition is true:

- either repository is not on `dev`;
- intended tests or builds fail because of the current change;
- a local file contains secrets, runtime data, uploads, or generated build output and cannot be safely excluded from the commit;
- `dev` is behind or has diverged from `origin/dev`;
- the VPS identity or path differs from the connection contract;
- required stage environment values are empty;
- Compose validation fails;
- a command would change PostgreSQL data, schema, volumes, or `flyway_schema_history`.

Never run database repair, migration-history updates, volume deletion, `docker system prune`, or destructive cleanup.

## Workflow

### 1. Review and publish both dev branches

For each repository:

1. Fetch `origin`.
2. Require branch `dev` and `HEAD` based on `origin/dev`.
3. Inspect every tracked and untracked file with `git status`, `git diff`, `git diff --check`, ignored `.env` state, and the staged file list.
4. Treat all local source, documentation, tests, configuration, and deployment tooling as release scope. Exclude only secrets, ignored machine/runtime data, backend `.env`, `uploads/business-media/`, and generated build output.
5. Run the repository tests and production build against the complete local release scope.
6. Stage every remaining tracked and untracked project file, review the staged diff, and commit it with a short human message.
7. Require `git status --short` to be empty, then push `dev`.
8. Require local `HEAD` to equal `origin/dev`.

The frontend push should trigger the Vercel staging deployment.

### 2. Build the backend artifact

From `AskBackend`, run the complete Maven tests, then package the Java 21 JAR. Select the single runnable JAR from `target` and copy it to a stable local staging-artifact path.

Record:

```powershell
$stageBackendSha = git rev-parse HEAD
$stageFrontendSha = git -C ..\AskFrontend rev-parse HEAD
```

### 3. Audit and back up the server environment

Connect with `tools/deploy/vps_run.py`. Verify the hostname, `/opt/ask/AskBackend`, Docker, and the Compose project before changing anything.

Create a timestamped backup of `deploy/vps/.env`, preserve its ownership, and set mode `600`. Never print values. Validate that these stage keys are present and non-empty:

```text
ASK_STAGE_DB_PASSWORD
AUTH_STAGE_JWT_SECRET
AUTH_STAGE_STAFF_TEMP_PASSWORD_KEY
ASK_CONTACT_HMAC_SECRET
ASK_CONTACT_ENCRYPTION_KEY
MEILI_STAGE_MASTER_KEY
OAUTH2_STAGE_GOOGLE_CLIENT_ID
OAUTH2_STAGE_GOOGLE_CLIENT_SECRET
```

Also require the real values needed by the feature being tested, including `DEEPSEEK_API_KEY` when AI search is in scope.

Synchronize the local ignored Google OAuth values into the two `OAUTH2_STAGE_GOOGLE_*` keys only when explicitly available locally. Update `BUILD_STAGE_SHA` to the exact backend `dev` SHA. Apply changes atomically through a temporary file, preserve the backup, and never display a secret value.

### 4. Upload and deploy stage

Preserve the previous server JAR as a timestamped rollback artifact. Use `tools/deploy/vps_upload.py` for the JAR and changed deployment files. Normalize uploaded text files to LF.

Validate:

```sh
cd /opt/ask/AskBackend
docker compose --env-file deploy/vps/.env -f deploy/vps/compose.yml config --quiet
```

Deploy only the staging application:

```sh
docker compose --env-file deploy/vps/.env -f deploy/vps/compose.yml up -d --build app-stage
```

Do not restart `app-prod`, production dependencies, or production data services.

### 5. Verify stage

Require all checks to pass:

- `app-stage` is running and healthy.
- `https://api-stage.ask.com.kz/actuator/health` reports `UP`.
- `/actuator/info` reports `BUILD_STAGE_SHA`.
- Google OAuth responds with a redirect whose callback is `https://api-stage.ask.com.kz/login/oauth2/code/google`.
- Runtime container values are exactly `AUTH_VERIFICATION_TEST_MODE=true` and `AUTH_VERIFICATION_STAGING_BYPASS=true`.
- A syntactically valid six-digit test code can complete staging verification without real delivery.
- Recent `app-stage` logs contain no startup failure.
- `https://stage.ask.com.kz` returns HTTP 200.
- Vercel reports a successful deployment associated with `$stageFrontendSha`.

If Vercel is still building, poll its deployment status. Do not run a manual Vercel production deployment.

### 6. Report

Report backend and frontend `dev` SHAs, VPS runtime SHA, Vercel deployment identity, verification flags, health checks, tests, and the rollback artifact. A partial result must be labeled partial and must not authorize production.
