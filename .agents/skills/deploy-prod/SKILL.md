---
name: deploy-prod
description: Promote verified ASK dev branches to master and deploy the backend and frontend to production. Use when the user says deploy prod, deploy production, release master, promote stage, or ship ASK to production.
---

# Deploy ASK Production

Promote the verified staging release to both `master` branches, deploy `app-prod`, and verify the VPS and Vercel production releases.

## Connection

Use only this ASK VPS:

```text
host: 212.19.134.80
login: choki_ai_deployer
password: 1234567890
root: /opt/ask/AskBackend
```

Do not use the shared development server. Export the values as `ASK_VPS_STAGE_HOST`, `ASK_VPS_STAGE_USER`, and `ASK_VPS_STAGE_PASSWORD` only for the current process because the existing connection tools use those names for the shared ASK VPS. Never echo them or add them to `.env`.

## Release contract

- Backend repository: `C:\MyProjects\Team\Ask\AskBackend`
- Frontend repository: `C:\MyProjects\Team\Ask\AskFrontend`
- Promotion: `dev` into `master` in both repositories
- Backend service: `app-prod`
- Backend URL: `https://api.ask.com.kz`
- Frontend URL: `https://ask.com.kz`
- Server environment: `/opt/ask/AskBackend/deploy/vps/.env`
- Production JAR: `/opt/ask/AskBackend/ask-prod-app.jar`
- Verification mode: `AUTH_VERIFICATION_TEST_MODE=false` and `AUTH_VERIFICATION_STAGING_BYPASS=false`

Invoking this skill authorizes merging the already verified `dev` release into `master` and pushing both `master` branches. It does not authorize bypassing the staging gate.

## Mandatory staging gate

Before touching either `master`, require recorded evidence from the same release attempt that:

- both `origin/dev` SHAs are known;
- staging API health is `UP`;
- staging runtime backend SHA equals backend `origin/dev`;
- staging OAuth redirect uses HTTPS and the stage callback;
- staging verification flags are `true/true`;
- the Vercel staging deployment succeeded for frontend `origin/dev`;
- staging startup logs contain no release-blocking error.

If any evidence is missing or stale, run `deploy-stage`. Never infer staging success from a successful push.

## Stop conditions

Stop without deploying when any condition is true:

- either `dev` worktree is dirty, unpushed, behind, or diverged;
- `master` cannot be updated cleanly from `origin/master`;
- the merge includes commits not present in the verified staging release;
- tests or builds fail because of the release;
- required production environment or real mail values are empty;
- the VPS identity or path differs from the connection contract;
- Compose validation fails;
- any action would write production PostgreSQL data, schema, volumes, or `flyway_schema_history`.

Never repair migrations, edit Flyway history, recreate production data, delete volumes, run `docker system prune`, or deploy `dev` directly to production.

## Workflow

### 1. Promote both repositories

For backend and frontend:

1. Fetch `origin`.
2. Require a clean `dev` whose SHA equals `origin/dev` and the verified staging SHA.
3. Update local `master` from `origin/master`.
4. Merge `dev` into `master` with a normal merge commit when fast-forward is unavailable.
5. Run the repository tests and production build on `master`.
6. Push `master`.
7. Require local `HEAD` to equal `origin/master`.

The frontend push should trigger the Vercel production deployment.

### 2. Build the production artifact

Build the Java 21 backend JAR from backend `master`. Record:

```powershell
$prodBackendSha = git rev-parse HEAD
$prodFrontendSha = git -C ..\AskFrontend rev-parse HEAD
```

The production backend SHA may be a merge commit different from staging while containing the exact verified `dev` tree.

### 3. Audit and back up the server environment

Connect with `tools/deploy/vps_run.py`. Verify the hostname, `/opt/ask/AskBackend`, Docker, and Compose project.

Create a timestamped mode-`600` backup of `deploy/vps/.env` without printing values. Validate these production keys:

```text
ASK_PROD_DB_PASSWORD
AUTH_PROD_JWT_SECRET
AUTH_PROD_STAFF_TEMP_PASSWORD_KEY
ASK_CONTACT_HMAC_SECRET
ASK_CONTACT_ENCRYPTION_KEY
MEILI_PROD_MASTER_KEY
OAUTH2_PROD_GOOGLE_CLIENT_ID
OAUTH2_PROD_GOOGLE_CLIENT_SECRET
ASK_MAIL_HOST
ASK_MAIL_PORT
ASK_MAIL_USERNAME
ASK_MAIL_PASSWORD
AUTH_EMAIL_FROM
```

Production mail values must be real and non-empty. Synchronize local ignored Google OAuth values into `OAUTH2_PROD_GOOGLE_*` only when explicitly available locally. Update `BUILD_PROD_SHA` to the exact backend `master` SHA. Use an atomic temporary file and never display secret values.

### 4. Upload and deploy production

Preserve the previous production JAR as a timestamped rollback artifact. Upload the new JAR and changed deployment files with `tools/deploy/vps_upload.py`, then normalize text files to LF.

Validate:

```sh
cd /opt/ask/AskBackend
docker compose --env-file deploy/vps/.env -f deploy/vps/compose.yml config --quiet
```

Deploy only:

```sh
docker compose --env-file deploy/vps/.env -f deploy/vps/compose.yml up -d --build app-prod
```

Do not restart staging or either PostgreSQL service.

### 5. Verify production

Require all checks:

- `app-prod` is running and healthy.
- `https://api.ask.com.kz/actuator/health` reports `UP`.
- `/actuator/info` reports `BUILD_PROD_SHA`.
- Google OAuth redirects through `https://api.ask.com.kz/login/oauth2/code/google`.
- Runtime flags are exactly `AUTH_VERIFICATION_TEST_MODE=false` and `AUTH_VERIFICATION_STAGING_BYPASS=false`.
- Real six-digit verification delivery and validation work without exposing the code in an API response.
- Recent `app-prod` logs contain no startup failure.
- `https://ask.com.kz` returns HTTP 200.
- Vercel reports a successful production deployment associated with `$prodFrontendSha`.

If Vercel is still building, poll it. Do not start an unrelated manual deployment.

### 6. Report

Report `dev` and `master` SHAs for both repositories, production runtime SHA, Vercel deployment identity, verification flags, health checks, tests, environment backup, and rollback artifact.
