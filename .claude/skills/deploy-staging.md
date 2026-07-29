---
name: deploy-staging
description: Deploy AskBackend + AskFrontend dev branches to staging. Use when the user says "deploy to staging", "deploy to VPS", "update staging", "push to staging", "deploy dev", "staging deploy", or wants to ship the latest dev code to the staging environment. Commits uncommitted changes, pushes to dev, builds JAR locally, uploads to VPS via chunked resumable SFTP, rebuilds Docker container, and verifies Vercel frontend deploy.
---

# Deploy Staging — single entry point for VPS + Vercel

Pipeline: commit + push both repos to `dev` → build JAR locally → chunked upload to VPS → sync compose.yml → rebuild `app-stage` → health checks. Frontend deploys itself via Vercel on push.

## CRITICAL: Server identity

| Server | IP | User | Password | Used for |
|--------|-----|------|----------|----------|
| **ASK VPS** | **212.19.134.80** | choki_ai_deployer | 1234567890 | ASK prod + stage (hostname `ask.kz`) |
| Shared dev | 194.238.42.177 | ubuntu | in AskBackend/.env | timetrack, sales-bot, drama — NOT ASK |

`.env` keys for the ASK VPS: `ASK_VPS_STAGE_HOST/USER/PASSWORD` (the plain `ASK_VPS_HOST` is the WRONG shared server). Verify: `dig +short api-stage.ask.com.kz` → 212.19.134.80.

## Architecture facts (verified 2026-07-17)

- `/opt/ask/AskBackend` on the VPS is a **plain copied tree, NOT a git repo**. Config changes (compose.yml, Caddyfile) reach it only by explicit upload.
- Compose project name is `vps` → containers `vps-app-stage-1`, `vps-db-stage-1`, `vps-meili-stage-1`, `vps-app-prod-1`, `vps-caddy-1`.
- `app-stage` builds from `Dockerfile.stage-light` (VPS-only file, never committed; compose.yml on the VPS is sed-patched to point at it). The committed compose.yml keeps `dockerfile: Dockerfile` (Maven multi-stage, for CI/prod).
- **JAR must run on Java 21** (`pom.xml` targets 21). `Dockerfile.stage-light` = `FROM eclipse-temurin:21-jre`. 17-jre → `UnsupportedClassVersionError`.
- Spring runs behind Caddy TLS termination → `SERVER_FORWARD_HEADERS_STRATEGY: framework` is set in compose.yml env. Without it OAuth redirect_uri is generated as `http://` and Google rejects it.
- App requires exactly these no-default env vars (application-prod.yml): `ASK_DB_URL/USERNAME/PASSWORD`, `AUTH_JWT_SECRET`, `AUTH_STAFF_TEMP_PASSWORD_KEY`, `ASK_CONTACT_HMAC_SECRET`, `ASK_CONTACT_ENCRYPTION_KEY`. When adding a new required placeholder, add compose passthrough or boot fails with "Could not resolve placeholder".
- Vercel auto-deploys `stage.ask.com.kz` on push to `dev` (~2 min).

## Deploy tools (committed, battle-tested)

| Tool | Purpose |
|------|---------|
| `tools/deploy/vps_run.py "<cmd>" [timeout_s]` | Run a command on the ASK VPS (fresh connection each call) |
| `tools/deploy/vps_upload.py <local> <remote>` | Chunked resumable upload: 4MB chunks, auto-reconnect, skips done chunks, md5-verified |

Both read `ASK_VPS_STAGE_*` from `.env`. **Never use inline `python -c` on Windows** — shell escaping corrupts it. **Never use plain `sftp.put` for big files** — the link runs ~0.05 MB/s and resets mid-transfer; the chunked uploader does ~800 KB/s and survives drops.

## Pipeline

### Step 0: Pre-flight
Check last 10 commits on BOTH repos for `Co-Authored-By` trailers. Found → STOP, ask user to rebase them out.

```bash
git log --oneline -10 --format="%H %s%(trailers:key=Co-Authored-By,valueonly)"
```

### Step 1: Commit and push both repos to dev
```bash
git add -A && git reset HEAD .env   # NEVER commit .env
git commit -m "<descriptive message>"
git push origin dev
```
Same in `../Ask_Frontend`. Vercel picks up the frontend push automatically.

### Step 2: Build JAR locally (never Maven on the VPS)
```bash
mvn package -DskipTests -q   # → target/ask-backend-*.jar (~76MB)
```

### Step 3: Upload JAR
```bash
cp target/ask-backend-*.jar /tmp/ask-stage-app.jar
python tools/deploy/vps_upload.py "$(cygpath -w /tmp/ask-stage-app.jar)" /opt/ask/AskBackend/ask-stage-app.jar
```

### Step 4: Sync deploy config to VPS (only when compose.yml/Caddyfile changed)
Upload compose.yml via a small SFTP put (use vps_upload.py), then on the VPS:
```bash
python tools/deploy/vps_run.py "cd /opt/ask/AskBackend/deploy/vps && cp compose.yml compose.yml.bak.\$(date +%Y%m%d_%H%M%S) && mv compose.yml.new compose.yml && sed -i 's/\r\$//' compose.yml && sed -i '/app-stage:/,/dockerfile:/ s|dockerfile: Dockerfile\$|dockerfile: Dockerfile.stage-light|' compose.yml && grep -n dockerfile: compose.yml"
```
**CRLF WARNING**: every file uploaded from the Windows working copy carries CRLF. Always `sed -i 's/\r$//'` on the VPS copy first — `$`-anchored sed patterns and bash scripts silently fail on CRLF.

Ensure `Dockerfile.stage-light` exists on the VPS (`/opt/ask/AskBackend/Dockerfile.stage-light`):
```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY ask-stage-app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Step 5: Validate config, rebuild, start
```bash
python tools/deploy/vps_run.py "cd /opt/ask/AskBackend && docker compose --env-file deploy/vps/.env -f deploy/vps/compose.yml config --quiet && echo VALID" 60
python tools/deploy/vps_run.py "cd /opt/ask/AskBackend && nohup docker compose --env-file deploy/vps/.env -f deploy/vps/compose.yml up -d --build app-stage > /opt/ask/build.log 2>&1 < /dev/null & exit" 30
```
`nohup` keeps the SSH channel from holding the exec open; poll the result:
```bash
python tools/deploy/vps_run.py "tail -15 /opt/ask/build.log; docker ps --format '{{.Names}}: {{.Status}}' | grep stage"
```

### Step 6: Health checks
```bash
curl -s https://api-stage.ask.com.kz/actuator/health                      # {"status":"UP"}
curl -s -o /dev/null -w '%{http_code} -> %{redirect_url}\n' https://api-stage.ask.com.kz/oauth2/authorization/google
# expect 302 with redirect_uri=https://... (http:// means forward-headers regression)
curl -s -o /dev/null -w '%{http_code}\n' https://stage.ask.com.kz         # 200
python tools/deploy/vps_run.py "docker logs vps-app-stage-1 --tail 30 2>&1 | grep -c ERROR"
```

## Required VPS env vars

In `/opt/ask/AskBackend/deploy/vps/.env` (compose `:?` fails fast if missing):
```
ASK_PROD_DB_PASSWORD / ASK_STAGE_DB_PASSWORD
AUTH_PROD_JWT_SECRET / AUTH_STAGE_JWT_SECRET
AUTH_PROD_STAFF_TEMP_PASSWORD_KEY / AUTH_STAGE_STAFF_TEMP_PASSWORD_KEY
ASK_CONTACT_HMAC_SECRET, ASK_CONTACT_ENCRYPTION_KEY   (shared prod+stage)
MEILI_PROD_MASTER_KEY, MEILI_STAGE_MASTER_KEY
OAUTH2_STAGE_GOOGLE_CLIENT_ID, OAUTH2_STAGE_GOOGLE_CLIENT_SECRET
DEEPSEEK_API_KEY
```
Append-if-missing pattern: `grep -q '^KEY=' .env || echo 'KEY=value' >> .env`.

## Failure recovery (all hit and solved on 2026-07-17)

- **Upload slow/resets**: never plain SFTP/stream; `tools/deploy/vps_upload.py` resumes automatically — just re-run it.
- **sed didn't change anything on VPS**: CRLF from Windows upload. `sed -i 's/\r$//' <file>` first.
- **`UnsupportedClassVersionError`**: Dockerfile.stage-light must be `eclipse-temurin:21-jre`.
- **meili unhealthy, wget "Connection refused" while curl works**: busybox wget resolves `localhost` → IPv6 `::1`; Meilisearch binds IPv4 only. Healthcheck must use `http://127.0.0.1:7700/health` (fixed in committed compose.yml — don't regress).
- **Flyway "Migration checksum mismatch for version N"**: a committed migration was edited after staging applied it. V4 is written with IF EXISTS guards → safe to re-run: upload the file, `sed -i 's/\r$//'`, `docker exec -i vps-db-stage-1 psql -U ask_stage -d ask_stage -v ON_ERROR_STOP=1 < V4.sql`, then `UPDATE flyway_schema_history SET checksum = <resolved-locally-value> WHERE version = 'N';` (the "Resolved locally" number from the app log). **app-prod will hit the same mismatch on its next deploy — repeat there.**
- **"Could not resolve placeholder 'X'"**: application-prod.yml gained a no-default var; add passthrough to BOTH app services in compose.yml, upload, `up -d app-stage`.
- **OAuth redirect_uri is http://**: `SERVER_FORWARD_HEADERS_STRATEGY: framework` missing from container env.
- **compose `up` says "dependency failed to start" right after meili creation**: first meili boot is slower than the healthcheck window; if meili shows healthy afterwards just re-run `up -d app-stage`.
- **SSH exec hangs after nohup**: append `< /dev/null` and `& exit`; poll build.log with a new connection instead of holding the channel.
- **Wrong server (194.238.42.177)**: shared dev server. Stop. Use 212.19.134.80.
- **Disk full**: `docker system prune -f` on the VPS.
- **Vercel didn't deploy**: check GitHub→Vercel integration or run `vercel deploy` in AskFrontend.
